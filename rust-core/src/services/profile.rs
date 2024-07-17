//! Profile and profile customization service

use crate::client_tcp;
use crate::page_info::Paginated;
use crate::services::auth::AuthError;
use crate::services::level::{LevelCategory, LevelError};
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use chrono::{DateTime, NaiveDate, Utc};
use educe::Educe;
use serde::{Deserialize, Serialize};
use strum_macros::EnumIter;
use thiserror::Error;

#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum ProfileError {
    #[error("NotEnoughLevel: You must at least be a {0:?} to do this")]
    NotEnoughLevel(LevelCategory),
    #[error("NotEarlyBird: This nickname color is only available to early supporters")]
    NotEarlyBird,
    #[error("NotYourBadge: This badge is not yours, don't touch it")]
    NotYourBadge,
    #[error("BadgeNotFound: This badge does not exist")]
    BadgeNotFound,
    #[error("BirthdayAlreadySet: You have already set your birthday")]
    BirthdayAlreadySet,
    #[error("TooYoung: According to the Bonfire privacy policy, you must be at least 13 years old to use the service")]
    TooYoung,

    #[error("{0}")]
    Auth(#[from] AuthError),
    #[error("{0}")]
    Level(#[from] LevelError),
    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        // rust macros moment
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// A badge shown in a profile or beside a username
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Badge {
    /// Unique ID for this badge (unique for every occurrence)
    pub id: i64,
    /// Who owns this badge
    pub user_id: i64,
    /// Badge name
    pub name: String,
    /// Badge description (inline-only Markdown supported)
    pub description: String,
    /// Image to be shown beside a username and in other small spaces
    pub mini_image_id: i64,
    /// The badge's image in its full glory and size
    pub image_id: i64,
    /// Fandom related to this badge, if any
    pub fandom_id: Option<i64>,
    /// Arbitrary link with more information about the badge or something related to it
    pub link: Option<String>,
    /// When the badge was given to the user
    pub created_at: DateTime<Utc>,
}

/// Customizations for nicknames, avatars, etc.
#[derive(Clone, Default, Debug, Serialize, Deserialize)]
pub struct AccountCustomization {
    /// Nickname color in the format `0xAARRGGBB`
    pub nickname_color: Option<u32>,
    /// Badge shown next to the username, if any
    pub active_badge: Option<Badge>,
}

/// Customizations for the full profile page
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ProfileCustomization {
    /// Account part of the customization parameters
    pub account: AccountCustomization,
    /// The list of badges to be shown on a profile shelf.
    ///
    /// If this is `None`, the shelf must be hidden.
    /// If an element is `None`, the spot must be blank.
    pub badge_shelf: Option<[Option<Badge>; 4]>,
}

/// Presets for nickname colors, unlockable after
/// reaching certain levels.
#[derive(Copy, Clone, Debug, Serialize, Deserialize, EnumIter, Eq, PartialEq)]
pub enum NicknameColorPreset {
    /// `#388E3C` (Trusted)
    Green,
    /// `#7B1FA2` (Experienced)
    Purple,
    /// `#03A9F4` (Curator)
    Aqua,
    /// `#2979FF` (Moderator)
    Blue,
    /// `#F57C00` (Admin)
    Orange,
    /// `#D32F2F` (Superadmin)
    Red,
    /// `#F97316` (Expert)
    Bonfire,
    /// #DB2777 (Early Supporter)
    Pink,
}
impl NicknameColorPreset {
    /// Get the nickname color in the format `0xAARRGGBB`
    pub fn get_color(&self) -> u32 {
        match self {
            NicknameColorPreset::Green => 0xFF388E3C,
            NicknameColorPreset::Purple => 0xFF7B1FA2,
            NicknameColorPreset::Aqua => 0xFF03A9F4,
            NicknameColorPreset::Blue => 0xFF2979FF,
            NicknameColorPreset::Orange => 0xFFF57C00,
            NicknameColorPreset::Red => 0xFFD32F2F,
            NicknameColorPreset::Bonfire => 0xFFF97316,
            NicknameColorPreset::Pink => 0xFFDB2777,
        }
    }
}

/// Profile and profile customization service
#[tarpc::service]
pub trait ProfileService {
    /// Get a user's [`AccountCustomization`] (nickname, avatar, etc.)
    async fn get_account_customization(user_id: i64) -> Result<AccountCustomization, ProfileError>;

    /// Get full profile customization info for a user
    async fn get_profile_customization(user_id: i64) -> Result<ProfileCustomization, ProfileError>;

    /// Set the preset to be used for the nickname color
    async fn set_nickname_color(
        user_id: i64,
        preset: Option<NicknameColorPreset>,
    ) -> Result<AccountCustomization, ProfileError>;

    /// Get all badges received by a user, newer first
    async fn get_user_badges(
        user_id: i64,
        before: Option<DateTime<Utc>>,
    ) -> Result<Paginated<Badge, DateTime<Utc>>, ProfileError>;

    /// Get a badge by its ID
    async fn get_badge(badge_id: i64) -> Result<Option<Badge>, ProfileError>;

    /// Add a badge to a user
    ///
    /// The input [`Badge`]'s id can be any number.
    /// The correct ID is returned in the response.
    async fn admin_put_badge(badge: Badge) -> Result<Badge, ProfileError>;

    /// Remove a badge by its ID
    async fn admin_remove_badge(badge_id: i64) -> Result<Badge, ProfileError>;

    /// Set the badge to be displayed next to a user's username
    async fn set_active_badge(
        user_id: i64,
        badge_id: Option<i64>,
    ) -> Result<AccountCustomization, ProfileError>;

    /// Set the items of [`ProfileCustomization::badge_shelf`]
    async fn set_badge_shelf(
        user_id: i64,
        badges: [Option<i64>; 4],
    ) -> Result<ProfileCustomization, ProfileError>;

    /// Show or hide the [badge shelf]
    ///
    /// [badge shelf]: ProfileCustomization::badge_shelf
    async fn set_show_badge_shelf(
        user_id: i64,
        show: bool,
    ) -> Result<ProfileCustomization, ProfileError>;

    /// Forcibly set the raw nickname color for a user
    async fn admin_set_nickname_color(
        user_id: i64,
        color: Option<u32>,
    ) -> Result<AccountCustomization, ProfileError>;

    // == birthday ==

    /// Set the real birthday of a user (for age restricting purposes)
    async fn set_birthday(user_id: i64, birthday: NaiveDate) -> Result<(), ProfileError>;

    /// Get the real birthday of a user
    ///
    /// Returns `None` if a birthday hasn't been set.
    async fn get_birthday(user_id: i64) -> Result<Option<NaiveDate>, ProfileError>;

    /// Get whether the user is at least `age` years old
    ///
    /// Returns `None` if a birthday hasn't been set.
    async fn is_age_at_least(user_id: i64, age: u32) -> Result<Option<bool>, ProfileError>;
}

pub struct Profiles;
impl Profiles {
    client_tcp!(ProfileServiceClient);
}
