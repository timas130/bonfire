use crate::{db, storage, VideoServer};
use c_core::services::video::{VideoError, VideoStatus};

impl VideoServer {
    pub(crate) async fn _cancel_upload(
        &self,
        user_id: i64,
        video_id: String,
    ) -> Result<(), VideoError> {
        let cancelled = sqlx::query_scalar!(
            "update videos set status = 6, updated_at = now() \
             where video_id = $1 and user_id = $2 and status = 1 \
             returning id",
            video_id,
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        if cancelled.is_none() {
            let video = db::get_by_video_id(&self.base.pool, &video_id)
                .await?
                .ok_or(VideoError::NotFound)?;
            if video.user_id != user_id {
                return Err(VideoError::NotOwner);
            }
            // cancelling twice is fine (clients retry this best-effort)
            if video.status == VideoStatus::Deleted {
                return Ok(());
            }
            return Err(VideoError::WrongStatus);
        }

        if let Some((s3, bucket)) = &self.s3 {
            storage::delete_object_quiet(s3, bucket, &storage::upload_key(&video_id)).await;
        }
        Ok(())
    }
}
