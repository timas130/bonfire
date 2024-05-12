use crate::models::color::Color;
use crate::schema::profile::badge::GBadge;
use async_graphql::{ComplexObject, SimpleObject, ID};
use c_core::services::profile::{AccountCustomization, Badge};

/// Customization parameters for an account reference (username, avatar, etc.)
#[derive(SimpleObject)]
#[graphql(complex, name = "AccountCustomization")]
pub struct GAccountCustomization {
    /// User who owns these parameters
    user_id: ID,
    /// A custom nickname color, if changed
    nickname_color: Option<Color>,
    #[graphql(skip)]
    _active_badge: Option<Badge>,
}

impl From<(i64, AccountCustomization)> for GAccountCustomization {
    fn from((user_id, core): (i64, AccountCustomization)) -> Self {
        Self {
            user_id: user_id.into(),
            nickname_color: core.nickname_color.map(From::from),
            _active_badge: core.active_badge,
        }
    }
}

#[ComplexObject]
impl GAccountCustomization {
    /// Badge to be shown next to the username
    async fn active_badge(&self) -> Option<GBadge> {
        self._active_badge.clone().map(From::from)
    }
}
