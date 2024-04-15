use crate::methods::get_notifications::RawNotification;
use crate::NotificationServer;
use c_core::services::notification::{Notification, NotificationError, NotificationPayload};
use sqlx::types::Json;

impl NotificationServer {
    pub(crate) async fn _get_by_id(
        &self,
        user_id: i64,
        notification_id: i64,
    ) -> Result<Notification, NotificationError> {
        sqlx::query_as!(
            RawNotification,
            "select \
                 id, \
                 user_id, \
                 payload as \"payload: Json<NotificationPayload>\", \
                 created_at, \
                 read \
             from notifications \
             where user_id = $1 and id = $2",
            user_id,
            notification_id,
        )
        .fetch_optional(&self.base.pool)
        .await?
        .map(From::from)
        .ok_or(NotificationError::NotificationNotFound)
    }
}
