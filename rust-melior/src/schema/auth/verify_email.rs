use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct VerifyEmailMutation;

/// Result of verifying a user's email
struct VerifyEmailResult {
    user_id: i64,
}
#[Object]
impl VerifyEmailResult {
    /// Verified user
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self.user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }
}

#[Object]
impl VerifyEmailMutation {
    /// Verify user's email after registration or deanonymizing
    async fn verify_email(
        &self,
        ctx: &Context<'_>,
        token: String,
    ) -> Result<VerifyEmailResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id = req
            .auth
            .verify_email(context::current(), token, Some(req.user_context.clone()))
            .await??;

        Ok(VerifyEmailResult { user_id })
    }
}
