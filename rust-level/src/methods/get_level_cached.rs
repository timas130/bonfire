use crate::consts::lvl::LevelCategoryExt;
use crate::LevelServer;
use c_core::services::level::{LevelCategory, LevelError};
use tracing::{span, Instrument, Level};

impl LevelServer {
    pub(crate) async fn _get_level_cached(
        &self,
        user_id: i64,
    ) -> Result<(u64, LevelCategory), LevelError> {
        let cached =
            sqlx::query_scalar!("select level from level_cache where user_id = $1", user_id,)
                .fetch_optional(&self.base.pool)
                .await?;

        let level = match cached {
            Some(level) => level as u64,
            None => {
                self._recount_level(user_id)
                    .instrument(span!(Level::INFO, "recounting level for cache", user_id))
                    .await?
                    .total_level
            }
        };

        Ok((level, LevelCategory::from_level(level)))
    }
}
