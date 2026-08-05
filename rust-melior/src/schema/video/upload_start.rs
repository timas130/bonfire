use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::video::GVideoUploadTicket;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct VideoUploadStartMutation;

#[Object]
impl VideoUploadStartMutation {
    /// Start a video upload: create the video record and presign an S3 PUT
    async fn video_upload_start(
        &self,
        ctx: &Context<'_>,
        size: i64,
    ) -> Result<GVideoUploadTicket, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let ticket = req
            .video
            .start_upload(context::current(), user.id, size)
            .await??;

        Ok(GVideoUploadTicket {
            id: ticket.video.video_id.into(),
            upload_url: ticket.upload_url,
        })
    }
}
