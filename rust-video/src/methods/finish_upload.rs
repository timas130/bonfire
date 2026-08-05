use crate::{db, storage, VideoServer};
use c_core::services::video::{Video, VideoError, VideoStatus};

impl VideoServer {
    pub(crate) async fn _finish_upload(
        &self,
        user_id: i64,
        video_id: String,
    ) -> Result<Video, VideoError> {
        let Some((s3, bucket)) = &self.s3 else {
            return Err(VideoError::Misconfigured);
        };

        let video = db::get_by_video_id(&self.base.pool, &video_id)
            .await?
            .ok_or(VideoError::NotFound)?;
        if video.user_id != user_id {
            return Err(VideoError::NotOwner);
        }
        if video.status != VideoStatus::AwaitingUpload {
            return Err(VideoError::WrongStatus);
        }

        let key = storage::upload_key(&video_id);
        let uploaded_size = storage::head_size(s3, bucket, &key).await?;
        let Some(uploaded_size) = uploaded_size else {
            return Err(VideoError::NotUploaded);
        };
        if Some(uploaded_size) != video.size_bytes {
            // the row stays in AwaitingUpload
            storage::delete_object_quiet(s3, bucket, &key).await;
            return Err(VideoError::SizeMismatch);
        }

        let video = db::cas_status(
            &self.base.pool,
            &video_id,
            &[VideoStatus::AwaitingUpload.into()],
            VideoStatus::Uploaded,
        )
        .await?
        .ok_or(VideoError::WrongStatus)?;

        self.queue.push(video_id);
        Ok(video)
    }
}
