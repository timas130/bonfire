use crate::{storage, VideoServer};
use c_core::services::video::VideoError;

impl VideoServer {
    pub(crate) async fn _release(&self, video_id: String) -> Result<(), VideoError> {
        let released = sqlx::query_scalar!(
            "update videos set status = 6, publication_id = null, updated_at = now() \
             where video_id = $1 and status in (1, 2, 3, 4, 5) \
             returning id",
            video_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        if released.is_some() {
            if let Some((s3, bucket)) = &self.s3 {
                storage::delete_object_quiet(s3, bucket, &storage::upload_key(&video_id)).await;
            }
        }
        Ok(())
    }
}
