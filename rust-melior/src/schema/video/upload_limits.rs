use crate::context::ReqContext;
use async_graphql::{Context, Object, SimpleObject};

/// Server-configured upload constraints, for client-side prechecks
#[derive(SimpleObject)]
pub struct VideoUploadLimits {
    pub max_size_bytes: i64,
    pub max_duration_ms: i64,
    pub daily_limit: i64,
}

#[derive(Default)]
pub struct VideoUploadLimitsQuery;

#[Object]
impl VideoUploadLimitsQuery {
    /// Get the server-configured video upload constraints
    async fn video_upload_limits(&self, ctx: &Context<'_>) -> VideoUploadLimits {
        let config = &ctx.data_unchecked::<ReqContext>().base.config.video;

        VideoUploadLimits {
            max_size_bytes: config.max_size_bytes,
            max_duration_ms: config.max_duration_ms,
            daily_limit: config.daily_limit,
        }
    }
}
