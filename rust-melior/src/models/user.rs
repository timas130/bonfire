use crate::data_loaders::AuthUserLoader;
use crate::error::RespError;
use crate::schema::level::daily_task::DailyTaskInfo;
use crate::schema::level::daily_task_fandoms::DailyTaskFandom;
use crate::utils::permissions::PermissionLevelGuard;
use async_graphql::dataloader::DataLoader;
use async_graphql::{ComplexObject, Context, Enum, SimpleObject, ID};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::auth::user::{AuthUser, PermissionLevel};

/// User's base permission level
#[derive(Enum, Copy, Clone, Eq, PartialEq)]
#[graphql(name = "PermissionLevel")]
pub enum GPermissionLevel {
    /// Regular user
    User,
    /// A member of highly trusted meta-staff
    Supermod,
    /// Highest ranking user
    Admin,
    /// Internal system account
    System,
}
impl From<PermissionLevel> for GPermissionLevel {
    fn from(value: PermissionLevel) -> Self {
        match value {
            PermissionLevel::User => Self::User,
            PermissionLevel::Supermod => Self::Supermod,
            PermissionLevel::Admin => Self::Admin,
            PermissionLevel::System => Self::System,
        }
    }
}

/// A user
#[derive(SimpleObject)]
#[graphql(complex)]
pub struct User {
    /// Internal ID that doesn't have the GQL [`ID`] type
    #[graphql(skip)]
    pub _id: i64,
    /// Unique nickname for the user
    pub username: String,
    /// The user's email
    #[graphql(guard = "PermissionLevelGuard::new(PermissionLevel::Supermod)")]
    pub email: Option<String>,
    /// Base permission level for the user
    #[graphql(guard = "PermissionLevelGuard::new(PermissionLevel::Supermod)")]
    pub permission_level: GPermissionLevel,
    /// Account creation date
    pub created_at: DateTime<Utc>,
}

impl User {
    fn from_parts((auth,): (AuthUser,)) -> Self {
        Self {
            _id: auth.id,
            username: auth.username,
            email: auth.email,
            permission_level: auth.permission_level.into(),
            created_at: auth.created_at,
        }
    }

    pub async fn by_id(ctx: &Context<'_>, user_id: i64) -> Result<Option<Self>, RespError> {
        let auth_data_loader = ctx.data_unchecked::<DataLoader<AuthUserLoader>>();

        let auth = auth_data_loader.load_one(user_id).await?;

        Ok(auth.map(|auth| Self::from_parts((auth,))))
    }

    pub async fn by_name(ctx: &Context<'_>, name: String) -> Result<Option<Self>, RespError> {
        let auth_data_loader = ctx.data_unchecked::<DataLoader<AuthUserLoader>>();

        let auth = auth_data_loader.load_one(name).await?;

        Ok(auth.map(|auth| Self::from_parts((auth,))))
    }

    pub async fn by_auth(_ctx: &Context<'_>, auth: AuthUser) -> Result<Self, RespError> {
        Ok(Self::from_parts((auth,)))
    }
}

#[ComplexObject]
impl User {
    /// User's ID
    ///
    /// Note that it is not guaranteed to be an
    /// integer.
    /// It should be treated as one only
    /// when another API requires it.
    async fn id(&self) -> ID {
        self._id.into()
    }

    /// Get daily task information for this User
    async fn daily_task(&self, ctx: &Context<'_>) -> Result<DailyTaskInfo, RespError> {
        self._daily_task(ctx).await
    }

    /// Get fandoms this user is active in
    ///
    /// These fandoms have a chance of appearing in
    /// a daily task, their chance proportional to
    /// the `multiplier` field in [`DailyTaskFandom`].
    async fn daily_task_fandoms(
        &self,
        ctx: &Context<'_>,
    ) -> Result<Vec<DailyTaskFandom>, RespError> {
        self._daily_task_fandoms(ctx).await
    }
}
