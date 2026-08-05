//! The encoding pipeline

mod encode;
mod loudness;
mod probe;
mod thumbnail;

use crate::{db, storage, VideoServer};
use c_core::prelude::anyhow::{self, anyhow, bail};
use c_core::prelude::aws_sdk_s3::Client;
use c_core::prelude::tokio;
use c_core::prelude::tokio::task::spawn_blocking;
use c_core::services::video::VideoStatus;
pub use encode::encode;
use gstreamer as gst;
use gstreamer::prelude::*;
pub use loudness::{analyze_gain, clamp_gain, AudioGain};
pub use probe::{probe, MediaInfo};
use std::path::{Path, PathBuf};
use std::time::{Duration, Instant};
pub use thumbnail::{thumbnail, Thumbnail};
use tracing::{error, info, warn};

const MAX_ERROR_LEN: usize = 300;
const CLAIM_RETRY_DELAY: Duration = Duration::from_secs(10);

// just in case?
// I don't think the lib cleans up for us.
pub(crate) struct StateGuard<'a>(pub &'a gst::Element);
impl Drop for StateGuard<'_> {
    fn drop(&mut self) {
        let _ = self.0.set_state(gst::State::Null);
    }
}

pub(crate) fn timeout_for_duration(duration_ms: i64) -> Duration {
    Duration::from_secs(30.max(duration_ms.max(0) as u64 / 1000 * 5))
}

pub(crate) fn quoted(path: &Path) -> anyhow::Result<String> {
    let path = path
        .to_str()
        .ok_or_else(|| anyhow!("non-utf8 path {path:?}"))?;
    if path.contains('"') || path.contains('\\') {
        bail!("unrepresentable path {path:?}");
    }
    Ok(format!("\"{path}\""))
}

pub(crate) fn run_pipeline_to_eos(
    pipeline: &gst::Element,
    timeout: Duration,
    mut on_tags: impl FnMut(&gst::TagList),
) -> anyhow::Result<()> {
    pipeline.set_state(gst::State::Playing)?;
    let bus = pipeline
        .bus()
        .ok_or_else(|| anyhow!("pipeline has no bus"))?;

    let deadline = Instant::now() + timeout;
    loop {
        let remaining = deadline.saturating_duration_since(Instant::now());
        if remaining.is_zero() {
            bail!("pipeline hit the {}s watchdog", timeout.as_secs());
        }
        let message = bus.timed_pop_filtered(
            gst::ClockTime::from_mseconds(remaining.as_millis() as u64),
            &[
                gst::MessageType::Eos,
                gst::MessageType::Error,
                gst::MessageType::Tag,
            ],
        );
        let Some(message) = message else {
            bail!("pipeline hit the {}s watchdog", timeout.as_secs());
        };
        match message.view() {
            gst::MessageView::Eos(_) => return Ok(()),
            gst::MessageView::Error(err) => {
                bail!("pipeline error: {} ({:?})", err.error(), err.debug())
            }
            gst::MessageView::Tag(tag) => on_tags(&tag.tags()),
            _ => {}
        }
    }
}

/// Temp dir that is removed on Drop
struct JobDir(PathBuf);
impl JobDir {
    async fn create(work_dir: &str, video_id: &str) -> anyhow::Result<Self> {
        let path = Path::new(work_dir).join(video_id);
        tokio::fs::create_dir_all(&path).await?;
        Ok(Self(path))
    }

    fn file(&self, name: &str) -> PathBuf {
        self.0.join(name)
    }
}
impl Drop for JobDir {
    fn drop(&mut self) {
        if let Err(err) = std::fs::remove_dir_all(&self.0) {
            warn!("failed to remove job dir {:?}: {err:?}", self.0);
        }
    }
}

struct JobOutput {
    duration_ms: i64,
    width: i32,
    height: i32,
    gain_db: Option<f32>,
}

impl VideoServer {
    pub(crate) async fn process_one(&self, video_id: &str) {
        let Some((s3, bucket)) = self.s3.clone() else {
            error!("video worker running without s3 configured");
            return;
        };

        // claim the job
        let claimed = db::cas_status(
            &self.base.pool,
            video_id,
            &[VideoStatus::Uploaded.into()],
            VideoStatus::Processing,
        )
        .await;
        match claimed {
            Ok(Some(_)) => {}
            Ok(None) => return,
            Err(err) => {
                error!("failed to claim video for processing, requeueing: {err:?}");
                let queue = self.queue.clone();
                let video_id = video_id.to_string();
                tokio::spawn(async move {
                    tokio::time::sleep(CLAIM_RETRY_DELAY).await;
                    queue.push(video_id);
                });
                return;
            }
        }

        let job = match JobDir::create(&self.base.config.video.work_dir, video_id).await {
            Ok(job) => job,
            Err(err) => {
                error!("failed to create a job dir: {err:?}");
                self.fail(&s3, &bucket, video_id, &format!("job dir: {err:#}"))
                    .await;
                return;
            }
        };

        match self.run_job(&s3, &bucket, video_id, &job).await {
            Ok(output) => self.succeed(&s3, &bucket, video_id, output).await,
            Err(err) => {
                warn!("video processing failed: {err:?}");
                self.fail(&s3, &bucket, video_id, &format!("{err:#}")).await;
            }
        }
    }

    // actual full pipeline
    async fn run_job(
        &self,
        s3: &Client,
        bucket: &str,
        video_id: &str,
        job: &JobDir,
    ) -> anyhow::Result<JobOutput> {
        let config = &self.base.config.video;
        let source = job.file("source");
        let encoded = job.file("out.mp4");
        let faststart_tmp = job.file("faststart.tmp");

        // -1. download the video
        storage::spool_to_file(s3, bucket, &storage::upload_key(video_id), &source).await?;

        // 0. probe video metadata
        let probe_source = source.clone();
        let info = spawn_blocking(move || probe(&probe_source)).await??;
        if info.duration_ms <= 0 {
            bail!("the video has no known duration");
        }
        if info.duration_ms > config.max_duration_ms {
            bail!(
                "the video is too long: {} ms (max {} ms)",
                info.duration_ms,
                config.max_duration_ms
            );
        }

        // 1. loudness analysis
        let gain = if info.has_audio {
            let gain_source = source.clone();
            let gain =
                spawn_blocking(move || analyze_gain(&gain_source, info.duration_ms)).await??;
            Some(gain)
        } else {
            None
        };

        // 2. actual encode
        let (encode_src, encode_out, encode_tmp) =
            (source.clone(), encoded.clone(), faststart_tmp.clone());
        spawn_blocking(move || {
            encode(
                &encode_src,
                &encode_out,
                &encode_tmp,
                &info,
                gain.map(|gain| gain.linear),
            )
        })
        .await??;

        // 3. gen thumbnail and final metadata
        let thumb_source = encoded.clone();
        let final_info = spawn_blocking(move || probe(&thumb_source)).await??;
        // re-check the length gate against what will actually be served
        if final_info.duration_ms > config.max_duration_ms {
            bail!(
                "the encoded video is too long: {} ms (max {} ms)",
                final_info.duration_ms,
                config.max_duration_ms
            );
        }
        let thumb_encoded = encoded.clone();
        let thumb =
            spawn_blocking(move || thumbnail(&thumb_encoded, final_info.duration_ms)).await??;

        // 4. upload
        storage::multipart_upload_file(
            s3,
            bucket,
            &storage::encoded_key(video_id),
            &encoded,
            "video/mp4",
        )
        .await?;
        storage::put_bytes(
            s3,
            bucket,
            &storage::thumb_key(video_id),
            thumb.jpeg,
            "image/jpeg",
        )
        .await?;

        Ok(JobOutput {
            duration_ms: final_info.duration_ms,
            width: final_info.width as i32,
            height: final_info.height as i32,
            gain_db: gain.map(|gain| gain.gain_db as f32),
        })
    }

    async fn succeed(&self, s3: &Client, bucket: &str, video_id: &str, output: JobOutput) {
        let updated = sqlx::query_scalar!(
            "update videos \
             set status = 4, error = null, duration_ms = $2, width = $3, height = $4, \
                 audio_gain = $5, updated_at = now() \
             where video_id = $1 and status = 3 \
             returning id",
            video_id,
            output.duration_ms,
            output.width,
            output.height,
            output.gain_db,
        )
        .fetch_optional(&self.base.pool)
        .await;

        match updated {
            Ok(Some(_)) => {
                storage::delete_object_quiet(s3, bucket, &storage::upload_key(video_id)).await;
                info!(
                    video_id,
                    duration_ms = output.duration_ms,
                    width = output.width,
                    height = output.height,
                    "video encoded"
                );
            }
            Ok(None) => {
                warn!(video_id, "video was released mid-encode");
                storage::delete_object_quiet(s3, bucket, &storage::encoded_key(video_id)).await;
                storage::delete_object_quiet(s3, bucket, &storage::thumb_key(video_id)).await;
            }
            Err(err) => error!("failed to finish video processing: {err:?}"),
        }
    }

    async fn fail(&self, s3: &Client, bucket: &str, video_id: &str, error: &str) {
        let error = error.chars().take(MAX_ERROR_LEN).collect::<String>();
        let result = sqlx::query!(
            "update videos set status = 5, error = $2, updated_at = now() \
             where video_id = $1 and status = 3",
            video_id,
            error,
        )
        .execute(&self.base.pool)
        .await;
        if let Err(err) = result {
            error!("failed to mark video as failed: {err:?}");
        }
        storage::delete_object_quiet(s3, bucket, &storage::upload_key(video_id)).await;
    }
}
