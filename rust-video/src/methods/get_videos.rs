use crate::db::RawVideo;
use crate::VideoServer;
use c_core::services::video::{Video, VideoError};

impl VideoServer {
    pub(crate) async fn _get_videos(
        &self,
        video_ids: Vec<String>,
    ) -> Result<Vec<Video>, VideoError> {
        sqlx::query_as!(
            RawVideo,
            "select * from videos where video_id = any($1)",
            &video_ids,
        )
        .fetch_all(&self.base.pool)
        .await?
        .into_iter()
        .map(TryInto::try_into)
        .collect()
    }
}
