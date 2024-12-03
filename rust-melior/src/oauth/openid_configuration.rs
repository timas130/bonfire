use crate::context::GlobalContext;
use crate::error::RespError;
use axum::response::IntoResponse;
use axum::{Extension, Json};
use c_core::prelude::tarpc::context;

pub async fn openid_configuration(
    Extension(context): Extension<GlobalContext>,
) -> Result<impl IntoResponse, RespError> {
    let openid_configuration = context
        .auth
        .get_openid_metadata(context::current())
        .await??;

    Ok(Json(openid_configuration))
}
