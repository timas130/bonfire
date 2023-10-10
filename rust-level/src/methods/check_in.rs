use crate::LevelServer;
use c_core::services::level::LevelError;
use chrono::Utc;

impl LevelServer {
    pub(crate) async fn _check_in(&self, user_id: i64) -> Result<(), LevelError> {
        sqlx::query!(
            "update daily_tasks set checked_in = true where account_id = $1 and date = $2",
            user_id,
            Utc::now().date_naive(),
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
