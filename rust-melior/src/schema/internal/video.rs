use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::video::GVideo;
use crate::utils::ok::OkResp;
use crate::utils::permissions::PermissionLevelGuard;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel::System;

#[derive(Default)]
pub struct InternalVideoMutation;

#[Object]
impl InternalVideoMutation {
    /// Attach a video to a publication
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn internal_video_set_publication(
        &self,
        ctx: &Context<'_>,
        id: ID,
        publication_id: ID,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let publication_id = publication_id
            .try_into()
            .map_err(|_| RespError::InvalidId)?;

        req.video
            .set_publication(context::current(), id.to_string(), publication_id)
            .await??;

        Ok(OkResp)
    }

    /// Detach a video and mark it for deletion. Idempotent
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn internal_video_release(&self, ctx: &Context<'_>, id: ID) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.video
            .release(context::current(), id.to_string())
            .await??;

        Ok(OkResp)
    }

    /// Copy a READY video into a new record for a new publication
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn internal_video_duplicate(
        &self,
        ctx: &Context<'_>,
        id: ID,
        publication_id: ID,
    ) -> Result<GVideo, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let publication_id = publication_id
            .try_into()
            .map_err(|_| RespError::InvalidId)?;

        Ok(req
            .video
            .duplicate(context::current(), id.to_string(), publication_id)
            .await??
            .into())
    }
}
