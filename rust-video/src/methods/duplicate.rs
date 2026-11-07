use crate::db::RawVideo;
use crate::{db, storage, VideoServer};
use c_core::services::video::{Video, VideoError, VideoStatus};
use nanoid::nanoid;

impl VideoServer {
    pub(crate) async fn _duplicate(
        &self,
        video_id: String,
        publication_id: i64,
    ) -> Result<Video, VideoError> {
        let Some((s3, bucket)) = &self.s3 else {
            return Err(VideoError::Misconfigured);
        };

        let source = db::get_by_video_id(&self.base.pool, &video_id)
            .await?
            .ok_or(VideoError::NotFound)?;
        if source.status != VideoStatus::Ready {
            return Err(VideoError::NotReady);
        }

        let new_id = nanoid!();
        storage::copy_object(
            s3,
            bucket,
            &storage::encoded_key(&video_id),
            &storage::encoded_key(&new_id),
        )
        .await?;
        if let Err(err) = storage::copy_object(
            s3,
            bucket,
            &storage::thumb_key(&video_id),
            &storage::thumb_key(&new_id),
        )
        .await
        {
            storage::delete_object_quiet(s3, bucket, &storage::encoded_key(&new_id)).await;
            return Err(err.into());
        }

        let inserted = sqlx::query_as!(
            RawVideo,
            "insert into videos \
                 (video_id, user_id, publication_id, status, duration_ms, width, height, \
                  audio_gain, encoding_version) \
             values ($1, $2, $3, 4, $4, $5, $6, $7, $8) \
             returning *",
            new_id,
            source.user_id,
            publication_id,
            source.duration_ms,
            source.width,
            source.height,
            source.audio_gain,
            source.encoding_version,
        )
        .fetch_one(&self.base.pool)
        .await;

        match inserted {
            Ok(row) => row.try_into(),
            Err(err) => {
                // don't leak the freshly copied objects
                storage::delete_object_quiet(s3, bucket, &storage::encoded_key(&new_id)).await;
                storage::delete_object_quiet(s3, bucket, &storage::thumb_key(&new_id)).await;
                Err(err.into())
            }
        }
    }
}
