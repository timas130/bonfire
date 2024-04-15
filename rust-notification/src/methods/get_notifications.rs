use crate::NotificationServer;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::futures::TryStreamExt;
use c_core::services::notification::{Notification, NotificationError, NotificationPayload};
use sqlx::types::Json;

pub(crate) struct RawNotification {
    pub id: i64,
    pub user_id: i64,
    pub payload: Json<NotificationPayload>,
    pub created_at: DateTime<Utc>,
    pub read: bool,
}
impl From<RawNotification> for Notification {
    fn from(raw: RawNotification) -> Self {
        Self {
            id: raw.id,
            user_id: raw.user_id,
            payload: raw.payload.0,
            created_at: raw.created_at,
            read: raw.read,
        }
    }
}

impl NotificationServer {
    pub(crate) async fn _get_notifications(
        &self,
        user_id: i64,
        before: Option<DateTime<Utc>>,
        type_filter: Option<Vec<i32>>,
    ) -> Result<Vec<Notification>, NotificationError> {
        let type_filter = type_filter.as_deref();

        sqlx::query_as!(
            RawNotification,
            "select \
                 id, \
                 user_id, \
                 payload as \"payload: Json<NotificationPayload>\", \
                 created_at, \
                 read \
             from notifications \
             where \
                 user_id = $1 and \
                 ($2::timestamptz is null or created_at < $2) and \
                 ($3::int[] is null or notification_type = any($3)) \
             order by created_at desc
             limit 20",
            user_id,
            before,
            type_filter,
        )
        .fetch(&self.base.pool)
        .map_ok(From::from)
        .try_collect()
        .await
        .map_err(From::from)
    }
}
