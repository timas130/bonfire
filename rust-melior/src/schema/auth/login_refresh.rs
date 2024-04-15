use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::auth::login_email::LoginResultSuccess;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct LoginRefreshMutation;

#[Object]
impl LoginRefreshMutation {
    /// Refresh the access token using the refresh token
    async fn login_refresh(
        &self,
        ctx: &Context<'_>,
        #[graphql(secret)] refresh_token: String,
    ) -> Result<LoginResultSuccess, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let access_token = req
            .auth
            .login_refresh(
                context::current(),
                refresh_token.clone(),
                Some(req.user_context.clone()),
            )
            .await??;

        Ok(LoginResultSuccess {
            access_token,
            refresh_token,
        })
    }
}
