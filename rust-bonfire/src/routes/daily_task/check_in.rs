use crate::context::GlobalContext;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::Extension;
use chrono::Utc;

pub async fn dt_check_in(
    Extension(context): Extension<GlobalContext>,
    Path(user_id): Path<i64>,
) -> Result<(), StatusCode> {
    sqlx::query!(
        "update daily_tasks set checked_in = true where account_id = $1 and date = $2",
        user_id,
        Utc::now().date_naive(),
    )
    .execute(&context.pool)
    .await
    .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    Ok(())
}
