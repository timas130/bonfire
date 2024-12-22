use crate::context::GlobalContext;
use crate::error::RespError;
use axum::response::IntoResponse;
use axum::{Extension, Json};
use c_core::prelude::tarpc::context;

pub async fn oauth2_jwk_set(
    Extension(context): Extension<GlobalContext>,
) -> Result<impl IntoResponse, RespError> {
    let jwk_set = context.auth.get_jwk_set(context::current()).await??;

    Ok(Json(jwk_set))
}
