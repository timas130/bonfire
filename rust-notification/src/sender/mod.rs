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

const MAX_SEND_ATTEMPTS: u32 = 10;
const MAX_SEND_ATTEMPTS_EPHEMERAL: u32 = 5;
const FIRST_RETRY_DELAY: Duration = Duration::from_secs(3);

struct QueueItem {
    notification: Notification,
    session_id: i64,
    online_only: bool,
    failed_attempts: u32,
}

#[derive(Clone)]
pub(crate) struct SenderQueue {
    queue: Arc<Queue<QueueItem>>,
}
impl SenderQueue {
    /// Enqueue a new notification
    pub fn send(&self, notification: Notification, session_id: i64, online_only: bool) {
        self.queue.push(QueueItem {
            notification,
            session_id,
            online_only,
            failed_attempts: 0,
        });
    }
}

/// Notification queue & sender service
#[derive(Clone)]
pub(crate) struct NotificationSender {
    base: ServiceBase,
    // generics: increasing compilation time since 1973
    fcm_hub: Arc<FirebaseCloudMessaging<HttpsConnector<HttpConnector>>>,
    queue: Arc<Queue<QueueItem>>,
}
impl NotificationSender {
    /// Construct a new [`SenderQueue`] using config from [`ServiceBase`]
    pub async fn new(base: ServiceBase) -> Result<Self, anyhow::Error> {
        let hyper_client = google_fcm1::hyper::Client::builder().build(
            HttpsConnectorBuilder::new()
                .with_native_roots()?
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
            let queued = self.queue.pop().await;

            let span = span!(
                Level::INFO,
                "sending notification",
                notification_id = queued.notification.id,
                session_id = queued.session_id,
            );
            self.process_one(queued).instrument(span).await;
        }
    }

    async fn process_one(&self, queued: QueueItem) {
        let item = &queued.notification;
        let session_id = queued.session_id;

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
                self.retry_later(queued);
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
            self.retry_later(queued);
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
            NotificationTokenType::Fcm => {
                self.send_fcm(item, &token.token, queued.online_only).await
            }
        };

        // try again if failed
        if let Err(error) = result {
            warn!(user_id = item.user_id, ?error, "error sending notification");
            self.retry_later(queued);
        }
    }

    /// Requeue a notification after a delay, unless it has failed too many times
    fn retry_later(&self, mut queued: QueueItem) {
        let item = &queued.notification;
        let max_attempts = if item.id == 0 {
            MAX_SEND_ATTEMPTS_EPHEMERAL
        } else {
            MAX_SEND_ATTEMPTS
        };
        if queued.failed_attempts + 1 >= max_attempts {
            warn!(
                user_id = item.user_id,
                "sending failed {max_attempts} times, giving up"
            );
            return;
        }

        let delay = FIRST_RETRY_DELAY * 2u32.pow(queued.failed_attempts);
        warn!(
            user_id = item.user_id,
            "sending failed, retrying in {delay:?}"
        );
        queued.failed_attempts += 1;

        let queue = self.queue.clone();
        tokio::spawn(async move {
            sleep(delay).await;
            queue.push(queued);
        });
    }
}
