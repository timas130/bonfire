use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;

#[derive(Default)]
pub struct CancelEmailChangeMutation;

#[Object]
impl CancelEmailChangeMutation {
    async fn cancel_email_change(&self, ctx: &Context<'_>, token: String)
        -> Result<OkResp, RespError>
    {
        let req = ctx.data_unchecked::<ReqContext>();

        req.auth
            .cancel_email_change(context::current(), token)
            .await??;

        Ok(OkResp)
    }
}
