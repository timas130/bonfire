use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::video::{Video, VideoError, VideoStatus};
use c_core::DBPool;

pub(crate) struct RawVideo {
    pub id: i64,
    pub video_id: String,
    pub user_id: i64,
    pub publication_id: Option<i64>,
    pub status: i32,
    pub error: Option<String>,
    pub duration_ms: Option<i64>,
    pub width: Option<i32>,
    pub height: Option<i32>,
    pub size_bytes: Option<i64>,
    pub audio_gain: Option<f32>,
    pub encoding_version: i32,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

impl TryFrom<RawVideo> for Video {
    type Error = VideoError;

    fn try_from(row: RawVideo) -> Result<Self, Self::Error> {
        let status = VideoStatus::try_from(row.status)
            .map_err(|_| anyhow!("out of sync: unknown video status {}", row.status))?;
        Ok(Video {
            id: row.id,
            video_id: row.video_id,
            user_id: row.user_id,
            publication_id: row.publication_id,
            status,
            error: row.error,
            duration_ms: row.duration_ms,
            width: row.width,
            height: row.height,
            size_bytes: row.size_bytes,
            audio_gain: row.audio_gain,
            encoding_version: row.encoding_version,
            created_at: row.created_at,
            updated_at: row.updated_at,
        })
    }
}

/// Get a video by its public ID
pub(crate) async fn get_by_video_id(
    pool: &DBPool,
    video_id: &str,
) -> Result<Option<Video>, VideoError> {
    sqlx::query_as!(
        RawVideo,
        "select * from videos where video_id = $1",
        video_id
    )
    .fetch_optional(pool)
    .await?
    .map(TryInto::try_into)
    .transpose()
}

/// Compare-and-swap the status of a video
pub(crate) async fn cas_status(
    pool: &DBPool,
    video_id: &str,
    from: &[i32],
    to: VideoStatus,
) -> Result<Option<Video>, VideoError> {
    sqlx::query_as!(
        RawVideo,
        "update videos set status = $3, updated_at = now() \
         where video_id = $1 and status = any($2) \
         returning *",
        video_id,
        from,
        i32::from(to),
    )
    .fetch_optional(pool)
    .await?
    .map(TryInto::try_into)
    .transpose()
}
