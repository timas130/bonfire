use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;

#[derive(Default)]
pub struct LogoutMutation;

#[Object]
impl LogoutMutation {
    /// Log out. Terminate the current session
    async fn logout(&self, ctx: &Context<'_>) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(access_token) = req.access_token.clone() else {
            return Ok(OkResp);
        };

        req.auth
            .terminate_session(context::current(), access_token, req.session_id.unwrap())
            .await??;

        Ok(OkResp)
    }
}
