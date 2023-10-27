use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::schema::auth::login_oauth::OAuthLoginInput;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct BindOAuthMutation;

#[Object]
impl BindOAuthMutation {
    /// Bind an OAuth account to the current user
    async fn bind_oauth(
        &self,
        ctx: &Context<'_>,
        input: OAuthLoginInput,
    ) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(user) = &req.user else {
            return Err(AuthError::Unauthenticated.into());
        };

        req.auth
            .bind_oauth(
                context::current(),
                req.access_token.clone().unwrap(),
                input.provider.into(),
                input.nonce,
                input.code,
            )
            .await??;

        User::by_id(ctx, user.id).await?.ok_or(RespError::OutOfSync)
    }
}
