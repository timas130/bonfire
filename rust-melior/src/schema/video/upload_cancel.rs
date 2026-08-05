use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct VideoUploadCancelMutation;

#[Object]
impl VideoUploadCancelMutation {
    /// Cancel a video upload that has not been finished yet
    async fn video_upload_cancel(&self, ctx: &Context<'_>, id: ID) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        req.video
            .cancel_upload(context::current(), user.id, id.to_string())
            .await??;

        Ok(OkResp)
    }
}
