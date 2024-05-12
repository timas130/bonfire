use crate::ProfileServer;
use c_core::services::profile::{ProfileCustomization, ProfileError};

impl ProfileServer {
    pub(crate) async fn _set_show_badge_shelf(
        &self,
        user_id: i64,
        show: bool,
    ) -> Result<ProfileCustomization, ProfileError> {
        sqlx::query!(
            "insert into profile_customization (user_id, show_badge_shelf) \
             values ($1, $2) \
             on conflict (user_id) do \
             update set show_badge_shelf = excluded.show_badge_shelf",
            user_id,
            show,
        )
        .execute(&self.base.pool)
        .await?;

        self._get_profile_customization(user_id).await
    }
}
