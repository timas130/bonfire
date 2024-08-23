use crate::GifServer;
use c_core::services::gif::{GifContext, GifError};

impl GifServer {
    pub(crate) async fn _remove_from_favourites(
        &self,
        id: String,
        gif_context: GifContext,
    ) -> Result<(), GifError> {
        sqlx::query!(
            "delete from gif_favourites \
             where user_id = $1 and gif_id = $2",
            gif_context.user_id,
            &id,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
