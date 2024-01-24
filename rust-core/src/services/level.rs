//! # The leveling service
//!
//! This service handles counting achievements and daily tasks.
//! While other services are not ready yet, it also handles
//! some stuff that depends on karma, posts, etc.

use crate::client_tcp;
use crate::models::PageType;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use educe::Educe;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use thiserror::Error;

/// Error from the leveling service
#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum LevelError {
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

/// Result of recounting a user's level
///
/// This is returned by [`LevelService::recount_level`],
/// which counts all the achievements and returns details
/// about each of them and the total level of a specific
/// user.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LevelRecountResult {
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
    pub achievements: HashMap<i64, AchievementRecountResult>,
}

/// Result of recounting a specific achievement of a user
///
/// This result can't be obtained by itself, it
/// is only found in [`LevelRecountReport`] alongside
/// all the other achievements.
#[derive(Debug, Clone, Serialize, Deserialize)]
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
    /// this would be `None`.
    /// Then it goes to `0` and up.
    ///
    /// For example, if an achievement has targets
    /// 4, 7, and 10, and the user is at `count`
    /// 8, the `target` would be `Some(1)`.
    pub target: Option<usize>,
    /// The amount of levels this achievements gives
    pub level: u64,
}

/// A daily task
#[derive(Debug, Copy, Clone, Serialize, Deserialize)]
#[serde(tag = "type")]
pub enum DailyTask {
    /// Create and publish this amount of posts
    CreatePosts { amount: i64 },
    /// Earn this amount of karma with posts
    EarnPostKarma { amount: i64 },
    /// Post this amount of comments
    PostComments { amount: i64 },
    /// Earn this amount of karma with anything
    EarnAnyKarma { amount: i64 },
    /// Write this amount of messages in a public chat
    WriteMessages { amount: i64 },
    /// Rate this amount of different publications
    RatePublications { amount: i64 },
    /// Just enter the app
    Login,
    /// Create and publish this amount of posts in a specific fandom
    PostInFandom { amount: i64, fandom_id: i64 },
    /// Post this amount of comments in a specific fandom
    CommentInFandom { amount: i64, fandom_id: i64 },
    /// Post this amount of comments answering a newbie
    /// with a level smaller than `max_level`
    AnswerNewbieComment { amount: i64, max_level: i64 },
    /// Post this amount of comments under a post from
    /// a newbie with a level smaller than `max_level`
    CommentNewbiePost { amount: i64, max_level: i64 },
    /// Create and publish a post with a page of this type
    CreatePostWithPageType { page_type: PageType },
    /// Write this amount of messages each answering a different
    /// message in a public chat
    AnswerInChat { amount: i64 },
}
impl DailyTask {
    /// Get the total task units needed to
    /// complete the daily task.
    ///
    /// This is equivalent to the `amount` field
    /// in the enum variants.
    pub fn get_amount(self) -> i64 {
        match self {
            DailyTask::CreatePosts { amount } => amount,
            DailyTask::EarnPostKarma { amount } => amount,
            DailyTask::PostComments { amount } => amount,
            DailyTask::EarnAnyKarma { amount } => amount,
            DailyTask::WriteMessages { amount } => amount,
            DailyTask::RatePublications { amount } => amount,
            DailyTask::Login => 1,
            DailyTask::PostInFandom { amount, .. } => amount,
            DailyTask::CommentInFandom { amount, .. } => amount,
            DailyTask::AnswerNewbieComment { amount, .. } => amount,
            DailyTask::CommentNewbiePost { amount, .. } => amount,
            DailyTask::CreatePostWithPageType { .. } => 1,
            DailyTask::AnswerInChat { amount } => amount,
        }
    }

    /// Returns the fandom ID involved with this task,
    /// if it has one
    ///
    /// This method only returns `Some(_)` if the task
    /// is either [`PostInFandom`] or [`CommentInFandom`].
    ///
    /// [`PostInFandom`]: DailyTask::PostInFandom
    /// [`CommentInFandom`]: DailyTask::CommentInFandom
    pub fn get_fandom_id(self) -> Option<i64> {
        Some(match self {
            DailyTask::PostInFandom { fandom_id, .. } => fandom_id,
            DailyTask::CommentInFandom { fandom_id, .. } => fandom_id,
            _ => return None,
        })
    }
}

/// Types of daily tasks without details about the task
///
/// See [`DailyTask`] for task descriptions
#[derive(Debug, Copy, Clone)]
pub enum DailyTaskType {
    CreatePosts,
    EarnPostKarma,
    PostComments,
    EarnAnyKarma,
    WriteMessages,
    RatePublications,
    Login,
    PostInFandom,
    CommentInFandom,
    AnswerNewbieComment,
    CommentNewbiePost,
    CreatePostWithPageType,
    AnswerInChat,
}

/// State of daily tasks for a user
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DailyTaskInfo {
    /// Amount of task units earned by the user
    ///
    /// If a user made 5 posts, the `progress`
    /// of a [`DailyTask::CreatePosts`] task would
    /// be `5`.
    ///
    /// Note that this value can be higher than
    /// `total`.
    pub progress: i64,
    /// The total amount of task units needed to
    /// complete the task
    ///
    /// This is equivalent to [`DailyTask::get_amount`],
    /// but easier for the client.
    pub total: i64,
    /// Total level count earned from doing daily
    /// tasks
    ///
    /// The last 3 daily tasks are recounted to
    /// account for cheating, the older tasks are
    /// just accepted as-is.
    pub total_levels: i64,
    /// Bonus for having a lower level.
    /// This goes from `0.0` and up.
    ///
    /// This multiplier is *added* to `combo_multiplier`
    /// below, not actually multiplied.
    pub level_multiplier: f64,
    /// Bonus for completing the task daily.
    /// This one goes from `1.0` and up.
    ///
    /// `combo_multiplier` is *added* to this value to
    /// result in the final multiplier.
    pub combo_multiplier: f64,
    /// Level count the user would receive if they
    /// complete the task in full.
    pub possible_reward: u64,
    /// Daily task's fandom name
    ///
    /// If the [`DailyTask`] involves a fandom, this
    /// contains its name.
    /// The fandom ID can be obtained with
    /// [`DailyTask::get_fandom_id`].
    pub fandom_name: Option<String>,
    /// The daily task for today
    pub task: DailyTask,
}

/// Details about a fandom's chance to appear
/// in a [`DailyTask`]
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DailyTaskFandom {
    /// Numeric ID of the fandom
    pub id: i64,
    /// Relative chance that the fandom may appear
    ///
    /// This goes from `0.0` (exclusive) and up.
    pub multiplier: f64,
}

/// The leveling service
///
/// See [module-level documentation]
///
/// [module-level documentation]: super::level
#[tarpc::service]
pub trait LevelService {
    /// Completely recount the level of a user
    ///
    /// This goes through all the achievements and
    /// determines their level, then adds everything
    /// together and spits out a [`LevelRecountResult`].
    ///
    /// This also may call [`get_daily_task`] under the hood.
    ///
    /// > This is an expensive operation and its result
    /// should be cached.
    async fn recount_level(user_id: i64) -> Result<LevelRecountResult, LevelError>;

    /// Get the daily task information for today
    ///
    /// The resulting struct contains information about
    /// today's daily task for the specified user and
    /// the total amount of levels earned from DTs.
    async fn get_daily_task(user_id: i64) -> Result<DailyTaskInfo, LevelError>;

    /// Get the probabilities of fandoms appearing in a daily task
    ///
    /// Only the top 5 fandoms are displayed
    /// with multipliers above zero.
    /// That is how they're used in [`get_daily_task`].
    ///
    /// [`get_daily_task`]: LevelService::get_daily_task
    async fn get_daily_task_fandoms(user_id: i64) -> Result<Vec<DailyTaskFandom>, LevelError>;

    /// Notify that the user has logged in
    ///
    /// This is used for [`DailyTask::Login`].
    async fn check_in(user_id: i64) -> Result<(), LevelError>;
}

pub struct Level;
impl Level {
    client_tcp!(LevelServiceClient);
}
