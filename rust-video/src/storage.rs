//! Some shorthand utilities for dealing with S3

use c_core::prelude::anyhow::{self, anyhow, Context as _};
use c_core::prelude::aws_sdk_s3::error::ProvideErrorMetadata;
use c_core::prelude::aws_sdk_s3::presigning::PresigningConfig;
use c_core::prelude::aws_sdk_s3::primitives::ByteStream;
use c_core::prelude::aws_sdk_s3::types::{CompletedMultipartUpload, CompletedPart};
use c_core::prelude::aws_sdk_s3::Client;
use c_core::prelude::tokio;
use c_core::prelude::tokio::io::{AsyncReadExt, AsyncWriteExt};
use std::path::Path;
use std::time::Duration;
use tracing::warn;

pub fn upload_key(video_id: &str) -> String {
    format!("video/uploads/{video_id}")
}

pub fn encoded_key(video_id: &str) -> String {
    format!("video/encoded/{video_id}")
}

pub fn thumb_key(video_id: &str) -> String {
    format!("video/thumbs/{video_id}")
}

pub(crate) async fn presign_put(
    s3: &Client,
    bucket: &str,
    key: &str,
    content_length: i64,
    expires_in: Duration,
) -> anyhow::Result<String> {
    let presigned = s3
        .put_object()
        .bucket(bucket)
        .key(key)
        .content_length(content_length)
        .presigned(PresigningConfig::builder().expires_in(expires_in).build()?)
        .await
        .context("failed to presign the upload")?;
    Ok(presigned.uri().to_string())
}

pub(crate) async fn head_size(s3: &Client, bucket: &str, key: &str) -> anyhow::Result<Option<i64>> {
    match s3.head_object().bucket(bucket).key(key).send().await {
        Ok(head) => {
            Ok(Some(head.content_length().ok_or_else(|| {
                anyhow!("no content length on object {key}")
            })?))
        }
        Err(err) => {
            let err = err.into_service_error();
            if err.is_not_found() {
                Ok(None)
            } else {
                Err(anyhow::Error::from(err).context(format!("failed to head object {key}")))
            }
        }
    }
}

pub(crate) async fn spool_to_file(
    s3: &Client,
    bucket: &str,
    key: &str,
    dest: &Path,
) -> anyhow::Result<u64> {
    let object = s3
        .get_object()
        .bucket(bucket)
        .key(key)
        .send()
        .await
        .with_context(|| format!("failed to download object {key}"))?;

    let mut reader = object.body.into_async_read();
    let mut file = tokio::fs::File::create(dest).await?;
    let bytes = tokio::io::copy(&mut reader, &mut file).await?;
    file.flush().await?;
    Ok(bytes)
}

pub(crate) async fn multipart_upload_file(
    s3: &Client,
    bucket: &str,
    key: &str,
    path: &Path,
    content_type: &str,
) -> anyhow::Result<()> {
    let created = s3
        .create_multipart_upload()
        .bucket(bucket)
        .key(key)
        .content_type(content_type)
        .send()
        .await
        .context("failed to create a multipart upload")?;
    let upload_id = created
        .upload_id()
        .ok_or_else(|| anyhow!("no upload id on multipart upload"))?
        .to_string();

    let result = upload_parts(s3, bucket, key, &upload_id, path).await;
    match result {
        Ok(()) => Ok(()),
        Err(err) => {
            let abort = s3
                .abort_multipart_upload()
                .bucket(bucket)
                .key(key)
                .upload_id(&upload_id)
                .send()
                .await;
            if let Err(abort_err) = abort {
                warn!("failed to abort multipart upload of {key}: {abort_err:?}");
            }
            Err(err)
        }
    }
}

async fn upload_parts(
    s3: &Client,
    bucket: &str,
    key: &str,
    upload_id: &str,
    path: &Path,
) -> anyhow::Result<()> {
    let mut file = tokio::fs::File::open(path).await?;
    let mut parts = Vec::new();
    let mut part_number = 1;

    loop {
        let mut buf = vec![0u8; 8 * 1024 * 1024];
        let mut filled = 0;
        while filled < buf.len() {
            let read = file.read(&mut buf[filled..]).await?;
            if read == 0 {
                break;
            }
            filled += read;
        }
        if filled == 0 && part_number > 1 {
            break;
        }
        buf.truncate(filled);

        let part = s3
            .upload_part()
            .bucket(bucket)
            .key(key)
            .upload_id(upload_id)
            .part_number(part_number)
            .body(ByteStream::from(buf))
            .send()
            .await
            .with_context(|| format!("failed to upload part {part_number}"))?;
        parts.push(
            CompletedPart::builder()
                .part_number(part_number)
                .set_e_tag(part.e_tag().map(str::to_string))
                .build(),
        );

        if filled < 8 * 1024 * 1024 {
            break;
        }
        part_number += 1;
    }

    s3.complete_multipart_upload()
        .bucket(bucket)
        .key(key)
        .upload_id(upload_id)
        .multipart_upload(
            CompletedMultipartUpload::builder()
                .set_parts(Some(parts))
                .build(),
        )
        .send()
        .await
        .context("failed to complete the multipart upload")?;
    Ok(())
}

pub(crate) async fn put_bytes(
    s3: &Client,
    bucket: &str,
    key: &str,
    bytes: Vec<u8>,
    content_type: &str,
) -> anyhow::Result<()> {
    s3.put_object()
        .bucket(bucket)
        .key(key)
        .content_type(content_type)
        .body(ByteStream::from(bytes))
        .send()
        .await
        .with_context(|| format!("failed to upload object {key}"))?;
    Ok(())
}

pub(crate) async fn delete_object_quiet(s3: &Client, bucket: &str, key: &str) -> bool {
    match s3.delete_object().bucket(bucket).key(key).send().await {
        Ok(_) => true,
        Err(err)
            if err
                .as_service_error()
                .is_some_and(|err| err.code() == Some("NoSuchKey")) =>
        {
            true
        }
        Err(err) => {
            warn!("failed to delete object {key}: {err:?}");
            false
        }
    }
}

pub(crate) async fn copy_object(
    s3: &Client,
    bucket: &str,
    from: &str,
    to: &str,
) -> anyhow::Result<()> {
    s3.copy_object()
        .bucket(bucket)
        .copy_source(format!("{bucket}/{from}"))
        .key(to)
        .send()
        .await
        .with_context(|| format!("failed to copy object {from} to {to}"))?;
    Ok(())
}
