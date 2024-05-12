use crate::methods::get_account_customization::NicknameColorPresetExt;
use crate::ProfileServer;
use c_core::prelude::anyhow;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::profile::{AccountCustomization, NicknameColorPreset, ProfileError};

impl ProfileServer {
    pub(crate) async fn _set_nickname_color(
        &self,
        user_id: i64,
        preset: Option<NicknameColorPreset>,
    ) -> Result<AccountCustomization, ProfileError> {
        let Some(preset) = preset else {
            sqlx::query!(
                "update account_customization set nickname_color = null \
                 where user_id = $1",
                user_id,
            )
            .execute(&self.base.pool)
            .await?;
            return self._get_account_customization(user_id).await;
        };

        let category_req = preset.get_level_req();
        if let Some(category_req) = category_req {
            let (_, category) = self
                .level
                .get_level_cached(context::current(), user_id)
                .await
                .map_err(anyhow::Error::from)??;

            if category < category_req {
                return Err(ProfileError::NotEnoughLevel(category_req));
            }
        }

        if preset == NicknameColorPreset::Pink {
            let user = self
                .auth
                .get_by_id(context::current(), user_id)
                .await
                .map_err(anyhow::Error::from)??
                .ok_or(ProfileError::NotEarlyBird)?;
            let early_bird_mark = "2023-09-01T00:00:00+03:00"
                .parse::<DateTime<Utc>>()
                .unwrap();

            // if later than Sep 1, no access to pink username
            if user.created_at >= early_bird_mark {
                return Err(ProfileError::NotEarlyBird);
            }
        }

        sqlx::query!(
            "insert into account_customization (user_id, nickname_color) \
             values ($1, $2) \
             on conflict (user_id) do \
             update set nickname_color = excluded.nickname_color",
            user_id,
            preset.get_color() as i32,
        )
        .execute(&self.base.pool)
        .await?;

        self._get_account_customization(user_id).await
    }
}
