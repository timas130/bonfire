//! # The video service
//!
//! This service handles the lifecycle of natively-hosted videos:
//! presigned S3 uploads, server-side re-encoding with GStreamer,
//! and claiming/releasing videos by publications.

use crate::client_tcp;
use crate::services::level::LevelError;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use chrono::{DateTime, Utc};
use educe::Educe;
use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde::{Deserialize, Serialize};
use serde_repr::{Deserialize_repr, Serialize_repr};
use thiserror::Error;

/// Error from the video service
#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum VideoError {
    #[error("NotFound: This video doesn't exist")]
    NotFound,
    #[error("LevelTooLow: Your level is too low to upload videos")]
    LevelTooLow,
    #[error("TooLarge: The video can be at most {max_bytes} bytes large")]
    TooLarge { max_bytes: i64 },
    #[error("TooLong: The video can be at most {max_ms} milliseconds long")]
    TooLong { max_ms: i64 },
    #[error("DailyLimit: You have uploaded too many videos today")]
    DailyLimitReached,
    #[error("NotUploaded: The video file has not been uploaded yet")]
    NotUploaded,
    #[error("SizeMismatch: The uploaded file's size doesn't match the declared size")]
    SizeMismatch,
    #[error("WrongStatus: The video is in the wrong status for this operation")]
    WrongStatus,
    #[error("NotOwner: This video belongs to another user")]
    NotOwner,
    #[error("AlreadyClaimed: This video is already attached to another publication")]
    AlreadyClaimed,
    #[error("NotReady: This video has not finished encoding")]
    NotReady,
    #[error("Misconfigured: The video service is not configured for S3 storage")]
    Misconfigured,

    #[error("{0}")]
    Level(
        #[from]
        #[source]
        LevelError,
    ),
    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        // rust macros moment
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// Processing state of a video
#[derive(
    Hash,
    Debug,
    Copy,
    Clone,
    Eq,
    PartialEq,
    IntoPrimitive,
    TryFromPrimitive,
    Serialize_repr,
    Deserialize_repr,
)]
#[repr(i32)]
pub enum VideoStatus {
    /// The row exists, but the source file *may* not have
    /// been uploaded to S3 yet
    AwaitingUpload = 1,
    /// The source file is in S3, waiting for encoding
    Uploaded = 2,
    /// An encoding worker is processing the video right now
    Processing = 3,
    /// The encoded video and its thumbnail are available
    Ready = 4,
    /// Encoding failed
    Failed = 5,
    /// The video was canceled or released
    Deleted = 6,
}

/// A video row from the video service
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Video {
    /// Internal numeric ID of the row
    pub id: i64,
    /// Public identifier of the video (a 21-char nanoid);
    /// also the S3 key suffix
    pub video_id: String,
    /// The user who uploaded the video
    pub user_id: i64,
    /// The publication that claimed this video, if any
    pub publication_id: Option<i64>,
    /// Current processing status
    pub status: VideoStatus,
    /// Failure reason, if `status` is [`VideoStatus::Failed`]
    // yea this should've been an enum etc etc
    // I'm tired boss
    pub error: Option<String>,
    /// Duration of the encoded video in milliseconds
    pub duration_ms: Option<i64>,
    /// Width of the encoded video in pixels
    pub width: Option<i32>,
    /// Height of the encoded video in pixels
    pub height: Option<i32>,
    /// Declared size of the source file in bytes
    pub size_bytes: Option<i64>,
    /// Loudness-normalization gain (baked into the encode)
    pub audio_gain: Option<f32>,
    /// Version of the encoding pipeline that produced the output
    pub encoding_version: i32,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// Everything a client needs to upload a video source file
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UploadTicket {
    /// The newly created video row
    pub video: Video,
    /// Presigned S3 PUT URL for the source file
    pub upload_url: String,
}

/// The video service
#[tarpc::service]
pub trait VideoService {
    /// Start a video upload
    ///
    /// Checks user's level and returns a presigned S3 URL.
    async fn start_upload(user_id: i64, size_bytes: i64) -> Result<UploadTicket, VideoError>;

    /// Finish a video upload
    ///
    /// Verifies that the source file landed in S3 with exactly the
    /// declared size, then queues the video for encoding.
    async fn finish_upload(user_id: i64, video_id: String) -> Result<Video, VideoError>;

    /// Cancel a video upload that has not been finished yet (or failed)
    async fn cancel_upload(user_id: i64, video_id: String) -> Result<(), VideoError>;

    /// Get a single video by its public ID
    async fn get_video(video_id: String) -> Result<Option<Video>, VideoError>;

    /// Get multiple videos by IDs
    async fn get_videos(video_ids: Vec<String>) -> Result<Vec<Video>, VideoError>;

    /// Attach a video to a publication
    async fn set_publication(video_id: String, publication_id: i64) -> Result<(), VideoError>;

    /// Detach a video from its publication and mark it for deletion
    async fn release(video_id: String) -> Result<(), VideoError>;

    /// Copy a [`VideoStatus::Ready`] video into a new row claimed
    /// by `publication_id`
    ///
    /// For duplicating drafts
    async fn duplicate(video_id: String, publication_id: i64) -> Result<Video, VideoError>;
}

pub struct Videos;
impl Videos {
    client_tcp!(VideoServiceClient);
}
