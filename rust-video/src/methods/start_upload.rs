use crate::db::RawVideo;
use crate::{storage, VideoServer};
use c_core::prelude::{anyhow, tarpc};
use c_core::services::level::LevelCategory;
use c_core::services::video::{UploadTicket, VideoError};
use nanoid::nanoid;
use std::time::Duration;

impl VideoServer {
    pub(crate) async fn _start_upload(
        &self,
        user_id: i64,
        size_bytes: i64,
    ) -> Result<UploadTicket, VideoError> {
        let Some((s3, bucket)) = &self.s3 else {
            return Err(VideoError::Misconfigured);
        };
        let config = &self.base.config.video;

        let (_, category) = self
            .level
            .get_level_cached(tarpc::context::current(), user_id)
            .await
            .map_err(anyhow::Error::from)??;
        if category < LevelCategory::Trusted {
            return Err(VideoError::LevelTooLow);
        }

        if size_bytes <= 0 || size_bytes > config.max_size_bytes {
            return Err(VideoError::TooLarge {
                max_bytes: config.max_size_bytes,
            });
        }

        let mut tx = self.base.pool.begin().await?;
        // I mean why not use obscure Postgres features?
        // It's a match!!
        sqlx::query!(
            "select pg_advisory_xact_lock(\
                 hashtextextended('video_upload:' || ($1::bigint)::text, 0))",
            user_id,
        )
        .execute(&mut *tx)
        .await?;

        // canceled and failed uploads intentionally count
        let uploaded_today = sqlx::query_scalar!(
            "select count(*) as \"count!\" from videos \
             where user_id = $1 and created_at > now() - interval '24 hours'",
            user_id,
        )
        .fetch_one(&mut *tx)
        .await?;
        if uploaded_today >= config.daily_limit {
            return Err(VideoError::DailyLimitReached);
        }

        let video_id = nanoid!();
        let row = sqlx::query_as!(
            RawVideo,
            "insert into videos (video_id, user_id, size_bytes) \
             values ($1, $2, $3) \
             returning *",
            video_id,
            user_id,
            size_bytes,
        )
        .fetch_one(&mut *tx)
        .await?;
        tx.commit().await?;

        let upload_url = storage::presign_put(
            s3,
            bucket,
            &storage::upload_key(&video_id),
            size_bytes,
            Duration::from_secs(config.upload_url_expiry_secs),
        )
        .await?;

        Ok(UploadTicket {
            video: row.try_into()?,
            upload_url,
        })
    }
}
