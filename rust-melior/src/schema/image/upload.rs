use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::image::limits::{UploadLimits, UploadType};
use async_graphql::{Context, Enum, Object, SimpleObject, Upload, UploadValue};
use aws_sdk_s3::primitives::ByteStream;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tokio;
use image::{ImageDecoder, ImageFormat};
use nanoid::nanoid;
use std::io::{BufReader, Read, Seek, SeekFrom};
use strum::EnumString;
use tracing::info;

#[derive(Default)]
pub struct UploadImageMutation;

impl UploadImageMutation {
    fn do_sync_checks(file: &UploadValue, limits: UploadLimits) -> Result<ImageFormat, RespError> {
        // 2. Check the format
        let content = file
            .content
            .try_clone()
            .map_err(|_| RespError::MultipartError)?;
        let mut reader = BufReader::new(content);
        let mut magic_data = vec![0u8; 20];
        reader
            .read_exact(&mut magic_data)
            .map_err(|_| RespError::MultipartError)?;

        let Ok(detected_format) = image::guess_format(&magic_data) else {
            return Err(RespError::InvalidFormat);
        };
        if !limits.format.check(detected_format) {
            return Err(RespError::IncorrectFormat);
        }

        // Return to start of file
        reader
            .seek(SeekFrom::Start(0))
            .map_err(|_| RespError::MultipartError)?;

        // 3. Check resolution

        // We don't use image::load here, so we don't load
        // the whole image into memory.
        let mut image = image::io::Reader::new(reader);
        image.set_format(detected_format);
        let Ok(image) = image.into_decoder() else {
            return Err(RespError::ImageCorrupt);
        };

        let (w, h) = image.dimensions();
        if w > limits.max_side || h > limits.max_side {
            return Err(RespError::InvalidImageSize);
        }

        // 4. check aspect ratio
        if let Some(required_aspect) = limits.ratio {
            let aspect = w as f32 / h as f32;
            if aspect != required_aspect {
                return Err(RespError::InvalidAspectRatio);
            }
        }

        Ok(detected_format)
    }
}

#[Object]
impl UploadImageMutation {
    /// Upload an image/file and get its temporary key
    ///
    /// This method requires authentication.
    /// The calling user can only use the resulting key once.
    async fn upload_image(
        &self,
        ctx: &Context<'_>,
        upload_type: UploadType,
        file: Upload,
    ) -> Result<String, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;
        let (s3, bucket) = req.get_s3()?;

        let file = file.value(ctx).map_err(|_| RespError::MultipartError)?;
        let limits = upload_type.limits();

        // 1. Check file size
        let file_size = file.size().map_err(|_| RespError::MultipartError)?;

        if file_size > limits.size as u64 {
            return Err(RespError::InvalidByteSize);
        }
        if file_size < 20 {
            return Err(RespError::InvalidByteSize);
        }

        let image_format = Self::do_sync_checks(&file, limits)?;

        // 5. Generate temp image key
        let temp_id = nanoid!();
        let key = format!("{temp_id}-{}", user.id);

        // 6. Upload to S3
        s3.put_object()
            .bucket(bucket)
            .key(format!("temp/{key}"))
            .body(
                ByteStream::read_from()
                    .file(tokio::fs::File::from_std(file.content))
                    .build()
                    .await
                    .map_err(|_| RespError::MultipartError)?,
            )
            .send()
            .await
            .map_err(|e| anyhow!("failed to upload image: {e}"))?;

        let upload_type: &'static str = upload_type.into();
        sqlx::query!(
            "insert into temp_images (key, user_id, upload_type, size, gif) \
             values ($1, $2, $3, $4, $5)",
            key,
            user.id,
            upload_type,
            file_size as i64,
            image_format == ImageFormat::Gif,
        )
        .execute(&req.base.pool)
        .await?;

        info!(
            user_id = user.id,
            username = user.username,
            file_size,
            ?upload_type,
            "image uploaded"
        );

        Ok(key)
    }
}
