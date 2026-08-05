//! # The video server
//!
//! Handles the lifecycle of natively-hosted videos: presigned S3
//! uploads, server-side re-encoding with GStreamer (H.264/AAC with
//! baked-in loudness normalization) and claiming/releasing videos
//! by publications.

mod db;
mod methods;
pub mod processing;
mod queue;
pub mod storage;

use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, aws_sdk_s3, tokio};
use c_core::services::level::{Level, LevelServiceClient};
use c_core::services::video::{UploadTicket, Video, VideoError, VideoService};
use c_core::{host_tcp, ServiceBase};
use deadqueue::unlimited::Queue;
use std::sync::Arc;

#[derive(Clone)]
pub struct VideoServer {
    base: ServiceBase,
    /// Queue of `video_id`s waiting for an encoding worker
    queue: Arc<Queue<String>>,
    /// S3 client and bucket name, built once at startup;
    /// `None` when the images config is not S3
    s3: Option<(aws_sdk_s3::Client, String)>,
    level: LevelServiceClient,
}
impl VideoServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        gstreamer::init()?;
        tokio::fs::create_dir_all(&base.config.video.work_dir).await?;

        let server = VideoServer {
            level: Level::client_tcp(base.config.ports.level).await?,
            s3: c_core::s3::build_s3(&base.config.images),
            queue: Arc::new(Queue::new()),
            base,
        };
        server.startup_sweep().await?;
        server.spawn_workers();

        Ok(server)
    }

    host_tcp!(video);
}

impl VideoService for VideoServer {
    async fn start_upload(
        self,
        _: Context,
        user_id: i64,
        size_bytes: i64,
    ) -> Result<UploadTicket, VideoError> {
        self._start_upload(user_id, size_bytes).await
    }

    async fn finish_upload(
        self,
        _: Context,
        user_id: i64,
        video_id: String,
    ) -> Result<Video, VideoError> {
        self._finish_upload(user_id, video_id).await
    }

    async fn cancel_upload(
        self,
        _: Context,
        user_id: i64,
        video_id: String,
    ) -> Result<(), VideoError> {
        self._cancel_upload(user_id, video_id).await
    }

    async fn get_video(self, _: Context, video_id: String) -> Result<Option<Video>, VideoError> {
        self._get_video(video_id).await
    }

    async fn get_videos(
        self,
        _: Context,
        video_ids: Vec<String>,
    ) -> Result<Vec<Video>, VideoError> {
        self._get_videos(video_ids).await
    }

    async fn set_publication(
        self,
        _: Context,
        video_id: String,
        publication_id: i64,
    ) -> Result<(), VideoError> {
        self._set_publication(video_id, publication_id).await
    }

    async fn release(self, _: Context, video_id: String) -> Result<(), VideoError> {
        self._release(video_id).await
    }

    async fn duplicate(
        self,
        _: Context,
        video_id: String,
        publication_id: i64,
    ) -> Result<Video, VideoError> {
        self._duplicate(video_id, publication_id).await
    }
}
