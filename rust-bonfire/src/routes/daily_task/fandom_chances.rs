use crate::context::GlobalContext;
use crate::mechanics::daily_task::fandom::{get_possible_dt_fandoms, DTFandom};
use axum::extract::Path;
use axum::http::StatusCode;
use axum::{Extension, Json};
use chrono::Utc;

pub async fn get_dt_fandom_choices(
    Extension(context): Extension<GlobalContext>,
    Path(user_id): Path<i64>,
) -> Result<Json<Vec<DTFandom>>, StatusCode> {
    Ok(Json(
        get_possible_dt_fandoms(&context, user_id, &Utc::now().date_naive())
            .await
            .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?,
    ))
}
