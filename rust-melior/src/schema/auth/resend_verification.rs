use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct ResendVerificationMutation;

#[Object]
impl ResendVerificationMutation {
    /// Resend verification email
    async fn resend_verification(
        &self,
        ctx: &Context<'_>,
        email: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.auth
            .resend_verification(context::current(), email)
            .await??;

        Ok(OkResp)
    }
}
