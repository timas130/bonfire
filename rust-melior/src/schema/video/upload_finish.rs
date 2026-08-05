use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::video::GVideo;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct VideoUploadFinishMutation;

#[Object]
impl VideoUploadFinishMutation {
    /// Verify an uploaded video source landed in S3 and queue it for encoding
    async fn video_upload_finish(&self, ctx: &Context<'_>, id: ID) -> Result<GVideo, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let video = req
            .video
            .finish_upload(context::current(), user.id, id.to_string())
            .await??;

        Ok(video.into())
    }
}
