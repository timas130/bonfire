use crate::LevelServer;
use c_core::prelude::sqlx;
use c_core::services::level::{DailyTask, LevelError};
use chrono::{Days, NaiveDate, Utc};
use futures_util::TryStreamExt;
use tracing::warn;

pub mod fandom;
pub mod progress;
mod seed;
mod task;

pub const BASE_DT_LVL_REWARD: i64 = 3;

impl LevelServer {
    fn get_new_combo_multiplier(previous_multiplier: f64, progress: f64) -> f64 {
        let progress = progress.clamp(0., 2.);
        let change = -progress.powi(2) + 2. * progress - 0.75;
        let result = previous_multiplier + change;
        result.clamp(1., 2.)
    }

    fn get_level_multiplier(level: i64) -> f64 {
        // note that this multiplier is added, not multiplied
        if level >= 500 {
            0.
        } else if level >= 400 {
            0.5
        } else if level >= 300 {
            1.
        } else {
            1.5
        }
    }

    pub(crate) async fn get_dt_level(&self, user_id: i64) -> Result<i64, LevelError> {
        let mut tx = self.base.pool.begin().await?;

        struct Task {
            date: NaiveDate,
            task: DailyTask,
            progress: f64,
            combo_multiplier: f64,
        }

        // we get the tasks for the last 4 days (incl. today).
        let mut task_history = sqlx::query!(
            "select date, json_db, progress, combo_multiplier
             from daily_tasks
             where account_id = $1 and
                   date <= $2 and date >= $3
             order by date",
            user_id,
            Utc::now().date_naive(),
            Utc::now().date_naive() - Days::new(3),
        )
        .fetch(&mut *tx)
        .try_filter_map(|record| async move {
            // basically we forward any errors from sqlx
            // and filter out corrupted/outdated records
            Ok(Some(Task {
                date: record.date,
                progress: record.progress,
                combo_multiplier: record.combo_multiplier,
                task: match serde_json::from_value(record.json_db) {
                    Ok(task) => task,
                    Err(err) => {
                        warn!("couldn't deserialize archived daily task for {user_id}: {err:?}");
                        return Ok(None);
                    }
                },
            }))
        })
        .try_collect::<Vec<Task>>()
        .await?;

        let today = Utc::now().date_naive();

        let today_task_generated = task_history
            .last()
            .map(|task| task.date == today)
            .unwrap_or(false);

        if !today_task_generated {
            let level = sqlx::query_scalar!("select lvl from accounts where id = $1", user_id)
                .fetch_optional(&mut *tx)
                .await?
                .unwrap_or(100);

            let task = self.determine_task(user_id, level, &today).await?;

            // if you think about it, the combo multiplier can be
            // cheated if a user cheats through the previous level
            // and triggers the recount only once.
            // too bad! (it'll fix itself after a single recount anyway)
            let combo_multiplier = match task_history.last() {
                Some(task) => Self::get_new_combo_multiplier(task.combo_multiplier, task.progress),
                None => 1.,
            };
            let level_multiplier = Self::get_level_multiplier(level);

            sqlx::query!(
                "insert into daily_tasks (
                     account_id, date, json_db, progress,
                     combo_multiplier, level_multiplier
                 ) values (
                     $1, $2, $3, $4, $5, $6
                 )",
                user_id,
                today,
                serde_json::to_value(&task).map_err(|err| sqlx::Error::Decode(Box::new(err)))?,
                0.0,
                combo_multiplier,
                level_multiplier,
            )
            .execute(&mut *tx)
            .await?;

            task_history.push(Task {
                date: today,
                task,
                progress: 0.,
                combo_multiplier,
            });
        }

        for task in &task_history {
            let progress = self
                .get_task_progress(&task.task, user_id, &task.date)
                .await?;
            let progress_f = progress as f64 / task.task.get_amount() as f64;

            if task.progress != progress_f {
                sqlx::query!(
                    "update daily_tasks
                     set progress = $1
                     where account_id = $2 and date = $3",
                    progress_f,
                    user_id,
                    task.date,
                )
                .execute(&mut *tx)
                .await?;
            }
        }

        let level = sqlx::query_scalar!(
            "select sum(((level_multiplier + combo_multiplier) * $1)::int)
             from daily_tasks
             where account_id = $2 and progress >= 1",
            BASE_DT_LVL_REWARD as f64,
            user_id,
        )
        .fetch_one(&mut *tx)
        .await?
        .unwrap_or(0);

        tx.commit().await?;

        Ok(level)
    }
}
