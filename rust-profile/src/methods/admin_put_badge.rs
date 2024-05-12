use crate::ProfileServer;
use c_core::services::profile::{Badge, ProfileError};

impl ProfileServer {
    pub(crate) async fn _admin_put_badge(&self, badge: Badge) -> Result<Badge, ProfileError> {
        let id = sqlx::query_scalar!(
            "insert into badges (user_id, name, description, mini_image_id, image_id, fandom_id, link, created_at) \
             values ($1, $2, $3, $4, $5, $6, $7, $8) \
             returning id",
            badge.user_id,
            badge.name,
            badge.description,
            badge.mini_image_id,
            badge.image_id,
            badge.fandom_id,
            badge.link,
            badge.created_at,
        ).fetch_one(&self.base.pool).await?;

        Ok(Badge { id, ..badge })
    }
}
