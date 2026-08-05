use crate::context::{ContextExt, ReqContext};
use crate::error::RespError;
use crate::models::video::GVideo;
use async_graphql::{Context, Object, ID};
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel;
use c_core::services::video::{Video, VideoStatus};

const MAX_BATCH_IDS: usize = 100;

fn is_visible(video: &Video, is_system: bool) -> bool {
    is_system || video.status != VideoStatus::Deleted
}

#[derive(Default)]
pub struct VideoByIdQuery;

#[Object]
impl VideoByIdQuery {
    /// Get a natively-hosted video by its ID
    async fn video_by_id(&self, ctx: &Context<'_>, id: ID) -> Result<Option<GVideo>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let is_system = ctx.has_permission_level(PermissionLevel::System);

        Ok(req
            .video
            .get_video(context::current(), id.to_string())
            .await??
            .filter(|video| is_visible(video, is_system))
            .map(Into::into))
    }

    /// Batch-get natively-hosted videos by their IDs (at most 100)
    async fn videos_by_ids(
        &self,
        ctx: &Context<'_>,
        ids: Vec<ID>,
    ) -> Result<Vec<GVideo>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let is_system = ctx.has_permission_level(PermissionLevel::System);

        if ids.len() > MAX_BATCH_IDS {
            return Err(anyhow!("too many ids: {} (max {MAX_BATCH_IDS})", ids.len()).into());
        }

        Ok(req
            .video
            .get_videos(
                context::current(),
                ids.into_iter().map(|id| id.to_string()).collect(),
            )
            .await??
            .into_iter()
            .filter(|video| is_visible(video, is_system))
            .map(Into::into)
            .collect())
    }
}
