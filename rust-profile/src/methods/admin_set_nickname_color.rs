use crate::ProfileServer;
use c_core::services::profile::{AccountCustomization, ProfileError};

impl ProfileServer {
    pub(crate) async fn _admin_set_nickname_color(
        &self,
        user_id: i64,
        color: Option<u32>,
    ) -> Result<AccountCustomization, ProfileError> {
        sqlx::query!(
            "insert into account_customization (user_id, nickname_color) \
             values ($1, $2) \
             on conflict (user_id) do \
             update set nickname_color = excluded.nickname_color",
            user_id,
            color.map(|color| color as i32),
        )
        .execute(&self.base.pool)
        .await?;

        self._get_account_customization(user_id).await
    }
}
