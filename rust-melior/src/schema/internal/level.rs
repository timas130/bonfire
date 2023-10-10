use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::utils::permissions::{is_system_caller, PermissionLevelGuard};
use async_graphql::{ComplexObject, Context, Object, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::prelude::tarpc::serde::Serialize;
use c_core::services::auth::user::PermissionLevel::System;
use c_core::services::level;
use o2o::o2o;
use std::collections::HashMap;

#[derive(Default)]
pub struct LevelQuery;

#[Object]
impl LevelQuery {
    /// Force a recount of a user's level
    ///
    /// This method is for internal services only
    /// and you morons can't use it.
    #[graphql(
        visible = "is_system_caller",
        guard = "PermissionLevelGuard::new(System)"
    )]
    async fn internal_recount_level(
        &self,
        ctx: &Context<'_>,
        user_id: i64,
    ) -> Result<LevelRecountResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok(req
            .level
            .recount_level(context::current(), user_id)
            .await??
            .into())
    }
}

/// The internal result of recount a user's level
///
/// It contains all the achievements and returns details about
/// each of them and the total level of a person.
#[derive(SimpleObject, o2o)]
#[from_owned(level::LevelRecountResult)]
#[graphql(complex)]
pub struct LevelRecountResult {
    #[graphql(skip)]
    /// The ID of the user whose level has been counted
    pub user_id: i64,
    /// The user's total level
    ///
    /// The minimum is 100. In UI, the level is shown
    /// as a 2-decimal number, so level `540` is `5.4`
    /// for the user.
    pub total_level: u64,
    /// Details about each achievement's progress
    ///
    /// The key of this map is the numeric ID of the
    /// achievement, and the value is details about
    /// that achievement.
    /// It is guaranteed that every achievement has
    /// a value in here.
    /// (In case it doesn't, just assume it's at zero.)
    #[from(~.into_iter().map(|(k, v)| (k, v.into())).collect())]
    pub achievements: HashMap<i64, AchievementRecountResult>,
}
#[ComplexObject]
impl LevelRecountResult {
    /// The user whose level was recounted
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self.user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }
}

/// Result of recounting a specific achievement of a user
///
/// This result can't be obtained by itself, it
/// is only found in [`LevelRecountReport`] alongside
/// all the other achievements.
#[derive(SimpleObject, Serialize, o2o)]
#[from(level::AchievementRecountResult)]
pub struct AchievementRecountResult {
    /// Numeric ID of the achievement
    pub id: i64,
    /// Actual amount of task units completed
    ///
    /// For example, if a user has 5 posts, the achievement
    /// `ACHI_POSTS_COUNT` would have the count `5`.
    pub count: u64,
    /// Current level index for this achievement
    ///
    /// If the user hasn't surpassed the first level,
    /// this would be `null`.
    /// Then it goes to `0` and up.
    ///
    /// For example, if an achievement has targets
    /// 4, 7, and 10, and the user is at `count`
    /// 8, the `target` would be `1`.
    pub target: Option<usize>,
    /// The amount of levels this achievements gives
    pub level: u64,
}
