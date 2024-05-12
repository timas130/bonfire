use crate::ProfileServer;
use c_core::services::profile::{Badge, ProfileError};

impl ProfileServer {
    pub(crate) async fn _get_badge(&self, badge_id: i64) -> Result<Option<Badge>, ProfileError> {
        sqlx::query_as!(Badge, "select * from badges where id = $1", badge_id)
            .fetch_optional(&self.base.pool)
            .await
            .map_err(From::from)
    }
}
