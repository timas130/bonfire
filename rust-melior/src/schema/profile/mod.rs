pub(crate) mod badge;
pub(crate) mod customization;
mod internal_customization;
mod set_badge_shelf;
mod set_nickname_color;
mod user_badges;

use crate::schema::profile::badge::BadgeQuery;
use crate::schema::profile::internal_customization::InternalAccountCustomizationQuery;
use crate::schema::profile::set_badge_shelf::SetBadgeShelfMutation;
use crate::schema::profile::set_nickname_color::SetNicknameColorMutation;
use async_graphql::MergedObject;

#[derive(Default, MergedObject)]
pub struct ProfileQuery(InternalAccountCustomizationQuery, BadgeQuery);

#[derive(Default, MergedObject)]
pub struct ProfileMutation(SetBadgeShelfMutation, SetNicknameColorMutation);
