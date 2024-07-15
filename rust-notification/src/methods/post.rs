use crate::NotificationServer;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::prelude::tracing::error;
use c_core::services::notification::{
    Notification, NotificationError, NotificationInput, NotificationPayload, NotificationRecipient,
};
use serde_json::Value;
use std::time::Duration;

impl NotificationServer {
    async fn _get_last_online_time(
        &self,
        user_id: i64,
    ) -> Result<DateTime<Utc>, NotificationError> {
        Ok(sqlx::query_scalar!(
            "select last_online_time from accounts where id = $1",
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await
        .map(|time| time.unwrap_or(Utc::now().timestamp_millis()))
        .map(DateTime::<Utc>::from_timestamp_millis)
        .map_err(anyhow::Error::from)?
        .ok_or(anyhow!("out of sync"))?)
    }

    pub(crate) async fn _post(
        &self,
        input: NotificationInput,
    ) -> Result<Vec<Notification>, NotificationError> {
        let mut tx = self.base.pool.begin().await?;

        let payload = serde_json::to_value(&input.payload).map_err(anyhow::Error::from)?;

        let mut notifications = Vec::with_capacity(input.recipients.len());
        for recipient in input.recipients {
            let user_id = match recipient {
                NotificationRecipient::User(user_id) => user_id,
                NotificationRecipient::Session(session_id) => {
                    let session = self
                        .auth
                        .get_session(context::current(), session_id)
                        .await
                        .map_err(anyhow::Error::from)??;
                    session.user_id
                }
            };

            struct RawNotification {
                id: i64,
                created_at: DateTime<Utc>,
            }

            let raw_notification = if !input.ephemeral {
                let result = sqlx::query_as!(
                    RawNotification,
                    "insert into notifications (user_id, payload, notification_type) \
                     values ($1, $2, $3) \
                     returning id, created_at",
                    user_id,
                    payload,
                    input.payload.get_type()?,
                )
                .fetch_one(&mut *tx)
                .await;

                if let Err(err) = result {
                    error!(
                        ?err,
                        user_id,
                        payload_type = ?input.payload.get_type()?,
                        "could not create notification in db"
                    );
                    continue;
                }

                result.unwrap()
            } else {
                RawNotification {
                    id: 0,
                    created_at: Utc::now(),
                }
            };

            let payload = match input.payload.clone() {
                NotificationPayload::Legacy(mut value) => {
                    value["J_N_ID"] = Value::from(raw_notification.id);
                    value["J_N_DATE_CREATE"] =
                        Value::from(raw_notification.created_at.timestamp_millis());
                    NotificationPayload::Legacy(value)
                }
                #[allow(unreachable_patterns)]
                payload => payload,
            };

            let notification = Notification {
                id: raw_notification.id,
                user_id,
                payload,
                created_at: raw_notification.created_at,
                read: false,
            };

            let sessions = match recipient {
                NotificationRecipient::Session(session_id) => vec![self
                    .auth
                    .get_session(context::current(), session_id)
                    .await
                    .map_err(anyhow::Error::from)??],
                NotificationRecipient::User(user_id) => self
                    .auth
                    .admin_get_sessions(context::current(), user_id, 0)
                    .await
                    .map_err(anyhow::Error::from)??,
            };

            for session in sessions {
                if input.online_only && session.last_online < Utc::now() - Duration::from_secs(600)
                {
                    continue;
                }
                self.queue.send(notification.clone(), session.id);
            }

            notifications.push(notification);
        }

        tx.commit().await?;

        Ok(notifications)
    }
}
