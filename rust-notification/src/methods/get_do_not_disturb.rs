use crate::NotificationServer;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::notification::NotificationError;

impl NotificationServer {
    pub(crate) async fn _get_do_not_disturb(
        &self,
        user_id: i64,
    ) -> Result<Option<DateTime<Utc>>, NotificationError> {
        sqlx::query_scalar!(
            "select dnd_end_time from notification_profiles where user_id = $1",
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await
        .map(|end_time| end_time.unwrap_or(None))
        .map_err(From::from)
    }
}
