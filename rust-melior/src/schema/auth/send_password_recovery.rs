use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct SendPasswordRecoveryMutation;

#[Object]
impl SendPasswordRecoveryMutation {
    /// Send an email with a password recovery link
    async fn send_password_recovery(
        &self,
        ctx: &Context<'_>,
        email: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.auth
            .send_password_recovery(context::current(), email, Some(req.user_context.clone()))
            .await??;

        Ok(OkResp)
    }
}
