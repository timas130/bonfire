use crate::{db, VideoServer};
use c_core::services::video::{Video, VideoError};

impl VideoServer {
    pub(crate) async fn _get_video(&self, video_id: String) -> Result<Option<Video>, VideoError> {
        db::get_by_video_id(&self.base.pool, &video_id).await
    }
}
