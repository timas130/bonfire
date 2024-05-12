use crate::schema::profile::badge::GBadge;
use crate::schema::profile::customization::account::GAccountCustomization;
use async_graphql::{SimpleObject, ID};
use c_core::services::profile::ProfileCustomization;

/// Full page profile customizations
#[derive(SimpleObject)]
#[graphql(name = "ProfileCustomization")]
pub struct GProfileCustomization {
    /// User who owns these parameters
    user_id: ID,
    /// Account part of customization parameters
    account: GAccountCustomization,
    /// Badges to be shown on the badge shelf
    ///
    /// If this is `null`, the shelf must be hidden.
    /// If a badge is `null`, that spot must be empty.
    badge_shelf: Option<[Option<GBadge>; 4]>,
}

impl From<(i64, ProfileCustomization)> for GProfileCustomization {
    fn from((user_id, core): (i64, ProfileCustomization)) -> Self {
        Self {
            user_id: user_id.into(),
            account: (user_id, core.account).into(),
            badge_shelf: core
                .badge_shelf
                .map(|shelf| shelf.map(|badge| badge.map(From::from))),
        }
    }
}
