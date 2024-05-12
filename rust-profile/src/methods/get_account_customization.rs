use crate::ProfileServer;
use c_core::prelude::anyhow;
use c_core::prelude::strum::IntoEnumIterator;
use c_core::prelude::tarpc::context;
use c_core::services::level::LevelCategory;
use c_core::services::profile::{AccountCustomization, NicknameColorPreset, ProfileError};
use cached::proc_macro::cached;

pub trait NicknameColorPresetExt {
    fn get_level_req(&self) -> Option<LevelCategory>;
}
impl NicknameColorPresetExt for NicknameColorPreset {
    fn get_level_req(&self) -> Option<LevelCategory> {
        Some(match self {
            NicknameColorPreset::Green => LevelCategory::Trusted,
            NicknameColorPreset::Purple => LevelCategory::Experienced,
            NicknameColorPreset::Aqua => LevelCategory::Curator,
            NicknameColorPreset::Blue => LevelCategory::Moderator,
            NicknameColorPreset::Orange => LevelCategory::Admin,
            NicknameColorPreset::Red => LevelCategory::Superadmin,
            NicknameColorPreset::Bonfire => LevelCategory::Expert,
            NicknameColorPreset::Pink => return None,
        })
    }
}

#[cached(result = true, time = 3600, key = "i64", convert = "{ user_id }")]
pub(crate) async fn get_level(
    this: &ProfileServer,
    user_id: i64,
) -> Result<(u64, LevelCategory), ProfileError> {
    this.level
        .get_level_cached(context::current(), user_id)
        .await
        .map_err(anyhow::Error::from)?
        .map_err(From::from)
}

impl ProfileServer {
    pub(crate) async fn _get_account_customization(
        &self,
        user_id: i64,
    ) -> Result<AccountCustomization, ProfileError> {
        let raw = sqlx::query!(
            "select nickname_color, active_badge \
             from account_customization \
             where user_id = $1",
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let Some(raw) = raw else {
            return Ok(AccountCustomization::default());
        };

        // ensure the user has the required level to have this color
        let nickname_color = if let Some(nickname_color) = raw.nickname_color {
            let nickname_color = nickname_color as u32;
            let preset =
                NicknameColorPreset::iter().find(|category| category.get_color() == nickname_color);

            if let Some(preset) = preset {
                let category_req = preset.get_level_req();
                if let Some(category_req) = category_req {
                    // if the level service is not available, assume permissions are met
                    let (_, category) = get_level(self, user_id)
                        .await
                        .unwrap_or((0, LevelCategory::Protoadmin));

                    if category >= category_req {
                        Some(nickname_color)
                    } else {
                        None
                    }
                } else {
                    Some(nickname_color)
                }
            } else {
                Some(nickname_color)
            }
        } else {
            None
        };

        let active_badge = match raw.active_badge {
            Some(id) => self._get_badge(id).await?,
            None => None,
        };

        Ok(AccountCustomization {
            nickname_color,
            active_badge,
        })
    }
}
