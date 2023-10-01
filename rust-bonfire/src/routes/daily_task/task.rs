use crate::context::GlobalContext;
use crate::mechanics::daily_task::progress::get_task_progress;
use crate::mechanics::daily_task::{get_dt_level, BASE_DT_LVL_REWARD};
use crate::models::daily_task::DailyTask;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::{Extension, Json};
use chrono::Utc;
use serde::Serialize;

#[derive(Serialize)]
pub struct DailyTaskInfo {
    progress: i64,
    total: i64,
    total_levels: i64,
    level_multiplier: f64,
    combo_multiplier: f64,
    possible_reward: i64,
    fandom_name: Option<String>,
    task: DailyTask,
}

pub async fn get_daily_task(
    Extension(context): Extension<GlobalContext>,
    Path(user_id): Path<i64>,
) -> Result<Json<DailyTaskInfo>, StatusCode> {
    let total_levels = get_dt_level(&context, user_id)
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let today = Utc::now().date_naive();

    let task_record = sqlx::query!(
        "select json_db, combo_multiplier, level_multiplier from daily_tasks
         where account_id = $1 and date = $2",
        user_id,
        &today,
    )
    .fetch_one(&context.pool)
    .await
    .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    let task = serde_json::from_value::<DailyTask>(task_record.json_db)
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let fandom_name = match task.get_fandom_id() {
        Some(fandom_id) => sqlx::query_scalar!("select name from fandoms where id = $1", fandom_id)
            .fetch_optional(&context.pool)
            .await
            .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?,
        None => None,
    };

    let progress = get_task_progress(&context, &task, user_id, &today)
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    Ok(Json(DailyTaskInfo {
        progress,
        total: task.get_amount(),
        total_levels,
        task,
        combo_multiplier: task_record.combo_multiplier,
        level_multiplier: task_record.level_multiplier,
        possible_reward: ((task_record.combo_multiplier + task_record.level_multiplier)
            * BASE_DT_LVL_REWARD as f64)
            .round() as i64,
        fandom_name,
    }))
}
