use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct RecoverPasswordMutation;

#[Object]
impl RecoverPasswordMutation {
    /// Use a recovery token to reset the password
    ///
    /// The recovery token is received from the email send
    /// by `send_password_recovery`
    async fn recover_password(
        &self,
        ctx: &Context<'_>,
        token: String,
        new_password: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.auth
            .recover_password(context::current(), token, new_password)
            .await??;

        Ok(OkResp)
    }
}
