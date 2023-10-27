use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::auth::login_email::LoginResultSuccess;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct LoginInternalMutation;

fn is_internal(ctx: &Context<'_>) -> bool {
    let req = ctx.data_unchecked::<ReqContext>();
    req.user_context.is_internal()
}

#[Object]
impl LoginInternalMutation {
    /// Login as an internal service
    #[graphql(visible = "is_internal")]
    async fn login_internal(
        &self,
        ctx: &Context<'_>,
        key: String,
    ) -> Result<LoginResultSuccess, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let internal = req.user_context.is_internal();
        if !internal {
            return Err(AuthError::AccessDenied.into());
        }

        let (access_token, refresh_token) =
            req.auth.login_internal(context::current(), key).await??;

        Ok(LoginResultSuccess {
            access_token,
            refresh_token,
        })
    }
}
