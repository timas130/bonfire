use crate::context::GlobalContext;
use crate::error::RespError;
use axum::response::IntoResponse;
use axum::{Extension, Json};
use axum_extra::headers::authorization::Bearer;
use axum_extra::headers::Authorization;
use axum_extra::TypedHeader;
use c_core::prelude::tarpc::context;

pub async fn oauth2_userinfo(
    Extension(context): Extension<GlobalContext>,
    TypedHeader(Authorization(auth)): TypedHeader<Authorization<Bearer>>,
) -> Result<impl IntoResponse, RespError> {
    let resp = context
        .auth
        .get_oauth2_userinfo(context::current(), auth.token().to_string())
        .await??;

    Ok(Json(resp))
}
