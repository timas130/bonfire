use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, SimpleObject};
use aws_sdk_s3::presigning::PresigningConfig;
use c_core::prelude::anyhow::anyhow;
use std::time::Duration;
use tracing::error;

/// Information for downloading an image
#[derive(SimpleObject)]
pub struct ImageLink {
    /// Temporary URL for downloading the image
    u: String,
    /// Width, if available
    w: Option<i64>,
    /// Height, if available
    h: Option<i64>,
    /// Unique image ID
    i: i64,
}

impl ImageLink {
    /// Presign an arbitrary S3 object as an image link, valid for an hour
    pub async fn by_key(
        ctx: &Context<'_>,
        key: &str,
        id: i64,
        w: Option<i64>,
        h: Option<i64>,
    ) -> Result<ImageLink, RespError> {
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

        Ok(ImageLink {
            u: presigned.uri().to_string(),
            w,
            h,
            i: id,
        })
    }

    /// Presign the `res/{id}` object of a regular uploaded image
    pub async fn by_id(ctx: &Context<'_>, id: i64) -> Result<ImageLink, RespError> {
        Self::by_key(ctx, &format!("res/{id}"), id, None, None).await
    }
}
