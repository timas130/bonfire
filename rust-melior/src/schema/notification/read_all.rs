use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct NotificationReadAllMutation;

#[Object]
impl NotificationReadAllMutation {
    /// Mark every notification received by you as read
    async fn read_all_notifications(&self, ctx: &Context<'_>) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        req.notification
            .read_all(context::current(), user.id)
            .await??;

        Ok(OkResp)
    }
}
