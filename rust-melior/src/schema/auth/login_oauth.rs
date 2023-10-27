use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::auth::login_email::LoginResultSuccess;
use crate::schema::auth::oauth_url::OAuthProvider;
use async_graphql::{Context, InputObject, Object, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::auth;

#[derive(Default)]
pub struct LoginOAuthMutation;

/// Parameters for logging in via OAuth
///
/// It is used in `login_oauth` and `bind_oauth`.
#[derive(InputObject)]
pub struct OAuthLoginInput {
    /// OAuth code issuer
    pub provider: OAuthProvider,
    /// Unique nonce from [`OAuthUrl`]
    ///
    /// [`OAuthUrl`]: super::oauth_url::OAuthUrl
    pub nonce: String,
    /// The authorization code
    pub code: String,
}

/// Result of logging in via an OAuth provider
#[derive(SimpleObject)]
pub struct OAuthResult {
    /// `true` if there's a user with the same email
    /// but not OAuth provider account
    email_already_bound: bool,
    /// If login is successful, tokens to log in
    tokens: Option<LoginResultSuccess>,
}
impl From<auth::OAuthResult> for OAuthResult {
    fn from(value: auth::OAuthResult) -> Self {
        match value {
            auth::OAuthResult::SameEmailDifferentAccount => Self {
                email_already_bound: true,
                tokens: None,
            },
            auth::OAuthResult::Success {
                access_token,
                refresh_token,
            } => Self {
                email_already_bound: false,
                tokens: Some(LoginResultSuccess {
                    access_token,
                    refresh_token,
                }),
            },
        }
    }
}

#[Object]
impl LoginOAuthMutation {
    /// Complete logging in via an external provider
    async fn login_oauth(
        &self,
        ctx: &Context<'_>,
        input: OAuthLoginInput,
    ) -> Result<OAuthResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let result = req
            .auth
            .get_oauth_result(
                context::current(),
                input.provider.into(),
                input.nonce,
                input.code,
                Some(req.user_context.clone()),
            )
            .await??;

        Ok(result.into())
    }
}
