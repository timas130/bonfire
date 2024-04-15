use crate::NotificationServer;
use c_core::services::notification::NotificationError;

impl NotificationServer {
    pub(crate) async fn _read(
        &self,
        user_id: i64,
        notification_id: i64,
    ) -> Result<(), NotificationError> {
        sqlx::query!(
            "update notifications set read = true where user_id = $1 and id = $2",
            user_id,
            notification_id,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
