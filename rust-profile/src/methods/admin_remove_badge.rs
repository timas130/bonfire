use crate::ProfileServer;
use c_core::services::profile::{Badge, ProfileError};

impl ProfileServer {
    pub(crate) async fn _admin_remove_badge(&self, badge_id: i64) -> Result<Badge, ProfileError> {
        let badge = self
            ._get_badge(badge_id)
            .await?
            .ok_or(ProfileError::BadgeNotFound)?;

        sqlx::query!("delete from badges where id = $1", badge_id)
            .execute(&self.base.pool)
            .await?;

        Ok(badge)
    }
}
