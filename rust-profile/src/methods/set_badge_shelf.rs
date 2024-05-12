use crate::ProfileServer;
use c_core::services::profile::{ProfileCustomization, ProfileError};

impl ProfileServer {
    pub(crate) async fn _set_badge_shelf(
        &self,
        user_id: i64,
        badges: [Option<i64>; 4],
    ) -> Result<ProfileCustomization, ProfileError> {
        let mut tx = self.base.pool.begin().await?;

        for (idx, badge_id) in badges.into_iter().enumerate() {
            // if badge_id is None, remove any badges at this position from the shelf
            let Some(badge_id) = badge_id else {
                sqlx::query!(
                    "delete from badge_shelf_items where user_id = $1 and \"order\" = $2",
                    user_id,
                    idx as i32,
                )
                .execute(&mut *tx)
                .await?;
                continue;
            };

            // check badge ownership
            let badge_owner =
                sqlx::query_scalar!("select user_id from badges where id = $1", badge_id,)
                    .fetch_optional(&mut *tx)
                    .await?;
            let Some(badge_owner) = badge_owner else {
                return Err(ProfileError::BadgeNotFound);
            };

            if badge_owner != user_id {
                return Err(ProfileError::NotYourBadge);
            }

            sqlx::query!(
                "insert into badge_shelf_items (user_id, badge_id, \"order\") \
                 values ($1, $2, $3) \
                 on conflict (user_id, \"order\") do \
                 update set badge_id = excluded.badge_id",
                user_id,
                badge_id,
                idx as i32,
            )
            .execute(&mut *tx)
            .await?;
        }

        tx.commit().await?;

        self._get_profile_customization(user_id).await
    }
}
