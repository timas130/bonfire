use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object, SimpleObject};
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::LoginEmailResponse;

pub struct VerifyEmailMutation;

/// Result of verifying a user's email
#[derive(SimpleObject)]
struct VerifyEmailResult {
    #[graphql(skip)]
    user_id: i64,
}
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

        let resp = req
            .auth
            .verify_email(context::current(), token, Some(req.user_context.clone()))
            .await??;
        let LoginEmailResponse::Success {
            access_token,
            refresh_token,
        } = resp
        else {
            return Err(anyhow!("tfa required for email verification").into());
        };

        Ok(VerifyEmailResult {
            access_token,
            refresh_token,
        })
    }
}
