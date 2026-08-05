use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::image::ImageLink;
use crate::utils::permissions::PermissionLevelGuard;
use async_graphql::{ComplexObject, Context, Enum, SimpleObject, ID};
use aws_sdk_s3::presigning::PresigningConfig;
use b_video::storage::{encoded_key, thumb_key};
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::user::PermissionLevel::System;
use c_core::services::video::{Video, VideoStatus};
use std::time::Duration;
use tracing::error;

/// Processing state of a natively-hosted video
#[derive(Enum, Copy, Clone, Eq, PartialEq)]
#[graphql(name = "VideoStatus")]
pub enum GVideoStatus {
    AwaitingUpload,
    Uploaded,
    Processing,
    Ready,
    Failed,
    Deleted,
}
impl From<VideoStatus> for GVideoStatus {
    fn from(value: VideoStatus) -> Self {
        match value {
            VideoStatus::AwaitingUpload => Self::AwaitingUpload,
            VideoStatus::Uploaded => Self::Uploaded,
            VideoStatus::Processing => Self::Processing,
            VideoStatus::Ready => Self::Ready,
            VideoStatus::Failed => Self::Failed,
            VideoStatus::Deleted => Self::Deleted,
        }
    }
}

/// A natively-hosted video
#[derive(SimpleObject)]
#[graphql(complex, name = "Video")]
pub struct GVideo {
    pub id: ID,
    pub status: GVideoStatus,
    pub creator_id: ID,
    pub width: Option<i32>,
    pub height: Option<i32>,
    pub duration_ms: Option<i64>,

    #[graphql(skip)]
    pub publication_id: Option<i64>,
}
impl From<Video> for GVideo {
    fn from(video: Video) -> Self {
        Self {
            id: video.video_id.into(),
            status: video.status.into(),
            creator_id: video.user_id.into(),
            width: video.width,
            height: video.height,
            duration_ms: video.duration_ms,
            publication_id: video.publication_id,
        }
    }
}

#[ComplexObject]
impl GVideo {
    /// Presigned URL of the encoded MP4
    async fn url(&self, ctx: &Context<'_>) -> Result<Option<String>, RespError> {
        if self.status != GVideoStatus::Ready {
            return Ok(None);
        }
        Ok(Some(presign_get(ctx, &encoded_key(&self.id)).await?))
    }

    /// Thumbnail for this video (generally the first frame)
    async fn thumbnail(&self, ctx: &Context<'_>) -> Result<Option<ImageLink>, RespError> {
        if self.status != GVideoStatus::Ready {
            return Ok(None);
        }
        Ok(Some(
            ImageLink::by_key(
                ctx,
                &thumb_key(&self.id),
                -1,
                self.width.map(Into::into),
                self.height.map(Into::into),
            )
            .await?,
        ))
    }

    /// The publication that claimed this video, if any
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn publication_id(&self) -> Option<ID> {
        self.publication_id.map(Into::into)
    }
}

/// Everything a client needs to upload a video source file
#[derive(SimpleObject)]
#[graphql(name = "VideoUploadTicket")]
pub struct GVideoUploadTicket {
    /// The new video's ID
    pub id: ID,
    /// Presigned S3 PUT URL for the source file
    pub upload_url: String,
}

/// Presign a GET of an S3 object, valid for an hour
async fn presign_get(ctx: &Context<'_>, key: &str) -> Result<String, RespError> {
    let req = ctx.data_unchecked::<ReqContext>();
    let (s3, bucket) = req.get_s3()?;

    let presigned = s3
        .get_object()
        .bucket(bucket)
        .key(key)
        .presigned(
            PresigningConfig::builder()
                .expires_in(Duration::from_secs(3600))
                .build()
                .unwrap(),
        )
        .await
        .map_err(|err| {
            error!("failed to presign s3 request: {err:?}");
            anyhow!("signing failed")
        })?;

    Ok(presigned.uri().to_string())
}
