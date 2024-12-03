use crate::context::GlobalContext;
use crate::error::RespError;
use axum::response::IntoResponse;
use axum::{Extension, Form, Json};
use axum_extra::headers::authorization::Basic;
use axum_extra::headers::Authorization;
use axum_extra::TypedHeader;
use c_core::prelude::tarpc::context;
use std::collections::HashMap;

pub async fn oauth2_token(
    Extension(context): Extension<GlobalContext>,
    authorization: Option<TypedHeader<Authorization<Basic>>>,
    Form(params): Form<HashMap<String, String>>,
) -> Result<impl IntoResponse, RespError> {
    let resp = context
        .auth
        .get_oauth2_token(
            context::current(),
            params,
            authorization.map(|TypedHeader(Authorization(auth))| {
                (auth.username().to_string(), auth.password().to_string())
            }),
        )
        .await??;

    Ok(Json(resp))
}
