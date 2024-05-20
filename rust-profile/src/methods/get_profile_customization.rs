use crate::ProfileServer;
use c_core::prelude::futures::TryStreamExt;
use c_core::prelude::tarpc::context;
use c_core::prelude::{anyhow, tokio};
use c_core::services::profile::{Badge, ProfileCustomization, ProfileError, ProfileService};
use std::collections::HashMap;

impl ProfileServer {
    pub(crate) async fn _get_profile_customization(
        &self,
        user_id: i64,
    ) -> Result<ProfileCustomization, ProfileError> {
        let account_customization = tokio::spawn(
            self.clone()
                .get_account_customization(context::current(), user_id),
        );

        let show_badge_shelf = sqlx::query_scalar!(
            "select show_badge_shelf from profile_customization where user_id = $1",
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?
        .unwrap_or(false);

        let badge_shelf = if show_badge_shelf {
            let mut badges = sqlx::query!(
                "select \"order\", badges.* \
                 from badge_shelf_items \
                 inner join badges on id = badge_id \
                 where badge_shelf_items.user_id = $1
                 order by \"order\"",
                user_id,
            )
            .fetch(&self.base.pool)
            .map_ok(|raw| {
                (
                    raw.order,
                    Badge {
                        id: raw.id,
                        user_id: raw.user_id,
                        name: raw.name,
                        description: raw.description,
                        mini_image_id: raw.mini_image_id,
                        image_id: raw.image_id,
                        fandom_id: raw.fandom_id,
                        link: raw.link,
                        created_at: raw.created_at,
                    },
                )
            })
            .try_collect::<HashMap<i32, Badge>>()
            .await?;

            Some([
                badges.remove(&0),
                badges.remove(&1),
                badges.remove(&2),
                badges.remove(&3),
            ])
        } else {
            None
        };

        Ok(ProfileCustomization {
            account: account_customization.await.map_err(anyhow::Error::from)??,
            badge_shelf,
        })
    }
}
