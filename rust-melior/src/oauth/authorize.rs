use crate::context::GlobalContext;
use axum::extract::Query;
use axum::response::{IntoResponse, Redirect};
use axum::Extension;
use std::collections::HashMap;
use std::str::FromStr;
use url::Url;

pub async fn oauth2_authorize(
    Extension(context): Extension<GlobalContext>,
    Query(params): Query<HashMap<String, String>>,
) -> impl IntoResponse {
    let mut url = Url::from_str(&context.base.config.urls.oauth_authorize_frontend_link)
        .expect("invalid config urls");

    url.query_pairs_mut().extend_pairs(params.into_iter());

    Redirect::to(url.as_str())
}
