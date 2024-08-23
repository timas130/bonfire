use crate::GifServer;
use c_core::services::gif::{GifContext, GifError};

impl GifServer {
    pub(crate) async fn _add_to_favourites(
        &self,
        id: String,
        gif_context: GifContext,
    ) -> Result<(), GifError> {
        sqlx::query!(
            "insert into gif_favourites (user_id, gif_id) \
             values ($1, $2) \
             on conflict (user_id, gif_id) do update \
             set last_used_at = now()",
            gif_context.user_id,
            &id,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
