use chrono::Utc;
use c_core::prelude::anyhow::anyhow;
use c_core::services::level::{DailyTask, DailyTaskInfo, LevelError};
use crate::daily_task::BASE_DT_LVL_REWARD;
use crate::LevelServer;

impl LevelServer {
    pub(crate) async fn _get_daily_task(&self, user_id: i64) -> Result<DailyTaskInfo, LevelError> {
        let total_levels = self.get_dt_level(user_id).await?;

        let today = Utc::now().date_naive();

        let task_record = sqlx::query!(
            "select json_db, combo_multiplier, level_multiplier from daily_tasks
             where account_id = $1 and date = $2",
            user_id,
            &today,
        )
        .fetch_one(&self.base.pool)
        .await?;
        let task = serde_json::from_value::<DailyTask>(task_record.json_db)
            .map_err(|e| anyhow!("dt deserialization failed: {e:?}"))?;

        let fandom_name = match task.get_fandom_id() {
            Some(fandom_id) => sqlx::query_scalar!("select name from fandoms where id = $1", fandom_id)
                .fetch_optional(&self.base.pool)
                .await?,
            None => None,
        };

        let progress = self.get_task_progress(&task, user_id, &today).await?;

        Ok(DailyTaskInfo {
            progress,
            total: task.get_amount(),
            total_levels,
            task,
            combo_multiplier: task_record.combo_multiplier,
            level_multiplier: task_record.level_multiplier,
            possible_reward: ((task_record.combo_multiplier + task_record.level_multiplier)
                * BASE_DT_LVL_REWARD as f64)
                .round() as u64,
            fandom_name,
        })
    }
}
