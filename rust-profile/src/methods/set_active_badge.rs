use crate::ProfileServer;
use c_core::services::profile::{AccountCustomization, ProfileError};

impl ProfileServer {
    pub(crate) async fn _set_active_badge(
        &self,
        user_id: i64,
        badge_id: Option<i64>,
    ) -> Result<AccountCustomization, ProfileError> {
        if badge_id.is_none() {
            sqlx::query!(
                "update account_customization set active_badge = null \
                 where user_id = $1",
                user_id,
            )
            .execute(&self.base.pool)
            .await?;
            return self._get_account_customization(user_id).await;
        }

        let badge_owner = sqlx::query_scalar!("select user_id from badges where id = $1", badge_id)
            .fetch_optional(&self.base.pool)
            .await?;
        let Some(badge_owner) = badge_owner else {
            return Err(ProfileError::BadgeNotFound);
        };

        if badge_owner != user_id {
            return Err(ProfileError::NotYourBadge);
        }

        sqlx::query!(
            "insert into account_customization (user_id, active_badge) \
             values ($1, $2) \
             on conflict (user_id) do \
             update set active_badge = excluded.active_badge",
            user_id,
            badge_id,
        )
        .execute(&self.base.pool)
        .await?;

        self._get_account_customization(user_id).await
    }
}
