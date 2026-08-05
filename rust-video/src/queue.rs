use crate::{storage, VideoServer};
use c_core::prelude::tokio;
use c_core::prelude::tokio::time::sleep;
use c_core::prelude::tracing::{error, span, Instrument, Level};
use c_core::services::video::VideoError;
use std::time::Duration;

const GC_INTERVAL: Duration = Duration::from_secs(3600);

impl VideoServer {
    /// Spawn the encoding workers and the garbage collector
    pub(crate) fn spawn_workers(&self) {
        for _ in 0..self.base.config.video.threads.max(1) {
            tokio::spawn(self.clone().worker_loop());
        }
        tokio::spawn(self.clone().gc_loop());
    }

    async fn worker_loop(self) -> ! {
        loop {
            let video_id = self.queue.pop().await;
            let span = span!(
                Level::INFO,
                "processing video",
                video_id = video_id.as_str()
            );
            self.process_one(&video_id).instrument(span).await;
        }
    }

    pub(crate) async fn startup_sweep(&self) -> Result<(), VideoError> {
        sqlx::query!("update videos set status = 2, updated_at = now() where status = 3")
            .execute(&self.base.pool)
            .await?;
        let pending = sqlx::query_scalar!("select video_id from videos where status = 2")
            .fetch_all(&self.base.pool)
            .await?;
        for video_id in pending {
            self.queue.push(video_id);
        }
        Ok(())
    }

    async fn gc_loop(self) -> ! {
        loop {
            sleep(GC_INTERVAL).await;
            if let Err(err) = self.gc_pass().await {
                error!("video gc pass failed: {err:?}");
            }
        }
    }

    pub(crate) async fn gc_pass(&self) -> Result<(), VideoError> {
        let Some((s3, bucket)) = &self.s3 else {
            return Ok(());
        };

        let stale = sqlx::query_scalar!(
            "update videos set status = 6, updated_at = now() \
             where status = 1 and created_at < now() - interval '24 hours' \
             returning video_id"
        )
        .fetch_all(&self.base.pool)
        .await?;
        for video_id in stale {
            storage::delete_object_quiet(s3, bucket, &storage::upload_key(&video_id)).await;
        }

        let deleted = sqlx::query_scalar!("select video_id from videos where status = 6")
            .fetch_all(&self.base.pool)
            .await?;
        let mut purgeable = Vec::new();
        for video_id in deleted {
            let upload_gone =
                storage::delete_object_quiet(s3, bucket, &storage::upload_key(&video_id)).await;
            let encoded_gone =
                storage::delete_object_quiet(s3, bucket, &storage::encoded_key(&video_id)).await;
            let thumb_gone =
                storage::delete_object_quiet(s3, bucket, &storage::thumb_key(&video_id)).await;
            if upload_gone && encoded_gone && thumb_gone {
                purgeable.push(video_id);
            }
        }
        // hard-delete only the rows whose objects were all removed above
        sqlx::query!(
            "delete from videos where video_id = any($1) \
             and created_at < now() - interval '24 hours'",
            &purgeable,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
