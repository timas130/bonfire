use crate::{db, VideoServer};
use c_core::services::video::VideoError;

impl VideoServer {
    pub(crate) async fn _set_publication(
        &self,
        video_id: String,
        publication_id: i64,
    ) -> Result<(), VideoError> {
        let claimed = sqlx::query_scalar!(
            "update videos set publication_id = $2, updated_at = now() \
             where video_id = $1 and (publication_id is null or publication_id = $2) \
             returning id",
            video_id,
            publication_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        if claimed.is_some() {
            return Ok(());
        }

        // 0 rows: either claimed by another publication or plain missing
        if db::get_by_video_id(&self.base.pool, &video_id)
            .await?
            .is_some()
        {
            Err(VideoError::AlreadyClaimed)
        } else {
            Err(VideoError::NotFound)
        }
    }
}
