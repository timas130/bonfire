use crate::NotificationServer;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::notification::NotificationError;

impl NotificationServer {
    pub(crate) async fn _set_do_not_disturb(
        &self,
        user_id: i64,
        end_time: Option<DateTime<Utc>>,
    ) -> Result<(), NotificationError> {
        sqlx::query!(
            "insert into notification_profiles (user_id, dnd_end_time) \
             values ($1, $2) \
             on conflict (user_id) \
             do update set dnd_end_time = excluded.dnd_end_time",
            user_id,
            end_time,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
