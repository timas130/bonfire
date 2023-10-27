use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Enum, Object, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::auth;
use o2o::o2o;

#[derive(Default)]
pub struct OAuthUrlQuery;

/// An OAuth auth provider
#[derive(Enum, Copy, Clone, Eq, PartialEq)]
pub enum OAuthProvider {
    /// Old Firebase authentication method. Not actually OAuth
    LegacyFirebase,
    /// `accounts.google.com`
    Google,
}
impl From<OAuthProvider> for auth::OAuthProvider {
    fn from(value: OAuthProvider) -> Self {
        match value {
            OAuthProvider::LegacyFirebase => Self::LegacyFirebase,
            OAuthProvider::Google => Self::Google,
        }
    }
}

/// Client information to log in with an OAuth provider
///
/// Get this with `oauth_url`
#[derive(SimpleObject, o2o)]
#[from_owned(auth::OAuthUrl)]
pub struct OAuthUrl {
    /// Full URL for the browser
    url: String,
    /// Requested scopes
    scope: String,
    /// Unique CSRF token from the URL
    state: String,
    /// Unique nonce from the URL
    nonce: String,
}

#[Object]
impl OAuthUrlQuery {
    /// Get the necessary information to log in with an OAuth provider
    async fn oauth_url(
        &self,
        ctx: &Context<'_>,
        provider: OAuthProvider,
    ) -> Result<OAuthUrl, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let resp = req
            .auth
            .get_oauth_url(context::current(), provider.into())
            .await??;

        Ok(resp.into())
    }
}
