use crate::LevelServer;
use c_core::services::level::{DailyTaskFandom, LevelError};
use chrono::Utc;

impl LevelServer {
    pub(crate) async fn _get_daily_task_fandoms(
        &self,
        user_id: i64,
    ) -> Result<Vec<DailyTaskFandom>, LevelError> {
        self.get_possible_dt_fandoms(user_id, &Utc::now().date_naive())
            .await
    }
}
