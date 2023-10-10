use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;

#[derive(Default)]
pub struct CheckRecoveryQuery;

#[Object]
impl CheckRecoveryQuery {
    /// Get the user that a recovery token changes password to
    async fn check_recovery_token(&self, ctx: &Context<'_>, token: String) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id = req.auth
            .check_recovery_token(context::current(), token)
            .await??;

        Ok(User::by_id(ctx, user_id).await?.ok_or(RespError::OutOfSync)?)
    }
}
