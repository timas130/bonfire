mod fcm;

use c_core::prelude::tokio::time::sleep;
use c_core::prelude::tracing::{error, info, span, warn, Instrument, Level};
use c_core::prelude::{anyhow, tokio};
use c_core::services::notification::{Notification, NotificationTokenType};
use c_core::ServiceBase;
use deadqueue::unlimited::Queue;
use google_fcm1::hyper::client::HttpConnector;
use google_fcm1::hyper_rustls::{HttpsConnector, HttpsConnectorBuilder};
use google_fcm1::oauth2::ServiceAccountAuthenticator;
use google_fcm1::FirebaseCloudMessaging;
use std::sync::Arc;
use std::time::Duration;

#[derive(Clone)]
pub(crate) struct SenderQueue {
    queue: Arc<Queue<(Notification, i64)>>,
}
impl SenderQueue {
    /// Enqueue a new notification
    pub fn send(&self, notification: Notification, session_id: i64) {
        self.queue.push((notification, session_id));
    }
}

/// Notification queue & sender service
#[derive(Clone)]
pub(crate) struct NotificationSender {
    base: ServiceBase,
    // generics: increasing compilation time since 1973
    fcm_hub: Arc<FirebaseCloudMessaging<HttpsConnector<HttpConnector>>>,
    queue: Arc<Queue<(Notification, i64)>>,
}
impl NotificationSender {
    /// Construct a new [`SenderQueue`] using config from [`ServiceBase`]
    pub async fn new(base: ServiceBase) -> Result<Self, anyhow::Error> {
        let hyper_client = google_fcm1::hyper::Client::builder().build(
            HttpsConnectorBuilder::new()
                .with_native_roots()
                .https_only()
                .enable_http1()
                .build(),
        );
        let auth =
            ServiceAccountAuthenticator::builder(base.config.firebase.service_account.clone())
                .hyper_client(hyper_client.clone())
                .build()
                .await?;
        let hub = FirebaseCloudMessaging::new(hyper_client, auth);

        Ok(Self {
            base,
            fcm_hub: Arc::new(hub),
            queue: Arc::new(Queue::new()),
        })
    }

    /// Start processing queue items
    pub fn start(self) -> SenderQueue {
        for _ in 0..self.base.config.notification.threads {
            tokio::spawn(self.clone().start_one());
        }
        SenderQueue { queue: self.queue }
    }

    async fn start_one(self) -> ! {
        loop {
            let (item, session_id) = self.queue.pop().await;

            let span = span!(
                Level::INFO,
                "sending notification",
                notification_id = item.id,
                session_id = session_id,
            );
            self.process_one(item, session_id).instrument(span).await;
        }
    }

    async fn process_one(&self, item: Notification, session_id: i64) {
        // check that the notification still exists and is not read
        // unless it is ephemeral
        let read = item.id != 0 && {
            let read = sqlx::query_scalar!("select read from notifications where id = $1", item.id)
                .fetch_optional(&self.base.pool)
                .await;
            let Ok(read) = read else {
                error!(
                    "error getting notification when sending {:?}",
                    read.unwrap_err()
                );
                self.queue.push((item, session_id));
                return;
            };
            read.unwrap_or(false)
        };

        // "if read or deleted"
        if read {
            return;
        }

        // get upstream token
        let token = sqlx::query!(
            "select * from notification_tokens where session_id = $1",
            session_id,
        )
        .fetch_optional(&self.base.pool)
        .await;
        let Ok(token) = token else {
            error!("error getting notification token: {:?}", token.unwrap_err());
            self.queue.push((item, session_id));
            return;
        };
        let Some(token) = token else {
            info!("no token found to send notification");
            return;
        };

        // determine upstream service from token
        let service = NotificationTokenType::try_from(token.service);
        let Ok(service) = service else {
            error!("out of sync with database: unknown notification token service");
            return;
        };

        // send it!
        let result = match service {
            NotificationTokenType::Fcm => self.send_fcm(&item, &token.token).await,
        };

        // try again if failed
        if result.is_err() {
            let queue = self.queue.clone();
            warn!(
                user_id = item.user_id,
                error = ?result.unwrap_err(),
                "sending failed, retrying in 3 seconds"
            );
            tokio::spawn(async move {
                sleep(Duration::from_secs(3)).await;
                queue.push((item, session_id));
            });
        }
    }
}
