use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::image::limits::UploadType;
use crate::utils::permissions::PermissionLevelGuard;
use async_graphql::{Context, Object, SimpleObject, ID};
use c_core::services::auth::user::PermissionLevel::System;
use std::str::FromStr;

#[derive(Default)]
pub struct CheckImageMutation;

/// Result of `check_image`
#[derive(SimpleObject)]
pub struct CheckImageResponse {
    /// The resource's size in bytes
    pub size: i64,
    /// Is this resource a GIF
    pub gif: bool,
}

#[Object]
impl CheckImageMutation {
    /// Internal method for checking that an uploaded image is OK to be used
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn check_image(
        &self,
        ctx: &Context<'_>,
        user_id: ID,
        key: String,
        upload_types: Vec<UploadType>,
    ) -> Result<CheckImageResponse, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id: i64 = user_id.try_into().map_err(|_| RespError::InvalidId)?;

        let mut tx = req.base.pool.begin().await?;

        let temp_image = sqlx::query!(
            "select size, gif, upload_type from temp_images \
             where user_id = $1 and key = $2",
            user_id,
            &key,
        )
        .fetch_optional(&mut *tx)
        .await?;
        let Some(temp_image) = temp_image else {
            tx.rollback().await?;
            return Err(RespError::TempImageNotFound);
        };

        let upload_type_matches = UploadType::from_str(&temp_image.upload_type)
            .map(|db_upload_type| upload_types.contains(&db_upload_type))
            .unwrap_or(false);
        if upload_type_matches {
            tx.rollback().await?;
            return Err(RespError::IncorrectUploadType);
        }

        sqlx::query!(
            "delete from temp_images where user_id = $1 and key = $2",
            user_id,
            &key
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(CheckImageResponse {
            size: temp_image.size,
            gif: temp_image.gif,
        })
    }
}
