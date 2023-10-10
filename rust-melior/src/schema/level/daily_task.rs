use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Interface, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::level;
use o2o::o2o;
use tasks::*;

/// State of daily tasks for a user
#[derive(SimpleObject, o2o)]
#[from_owned(level::DailyTaskInfo)]
pub(crate) struct DailyTaskInfo {
    /// Amount of task units earned by the user
    ///
    /// If a user made 5 posts, the `progress`
    /// of a [`CreatePostsTask`] would be `5`.
    ///
    /// Note that this value can be higher than
    /// `total`.
    pub progress: i64,
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
    /// This multiplier is *added* to `comboMultiplier`
    /// below, not actually multiplied.
    pub level_multiplier: f64,
    /// Bonus for completing the task daily.
    /// This one goes from `1.0` and up.
    ///
    /// `comboMultiplier` is *added* to this value to
    /// result in the final multiplier.
    pub combo_multiplier: f64,
    /// Level count the user would receive if they
    /// complete the task in full.
    pub possible_reward: u64,
    /// Daily task's fandom name
    ///
    /// If the [`DailyTask`] involves a fandom, this
    /// contains its name.
    pub fandom_name: Option<String>,
    /// The daily task for today
    #[from(~.into())]
    pub task: DailyTask,
}

#[derive(Interface)]
#[graphql(field(name = "amount", ty = "&i64"))]
pub enum DailyTask {
    CreatePosts(CreatePostsTask),
    EarnPostKarma(EarnPostKarmaTask),
    PostComments(PostCommentsTask),
    EarnAnyKarma(EarnAnyKarmaTask),
    WriteMessages(WriteMessagesTask),
    RatePublications(RatePublicationsTask),
    Login(LoginTask),
    PostInFandom(PostInFandomTask),
    CommentInFandom(CommentInFandomTask),
    AnswerNewbieComment(AnswerNewbieCommentTask),
    CommentNewbiePost(CommentNewbiePostTask),
    CreatePostWithPageType(CreatePostWithPageTypeTask),
    AnswerInChat(AnswerInChatTask),
}
#[rustfmt::skip]
impl From<level::DailyTask> for DailyTask {
    fn from(value: level::DailyTask) -> Self {
        match value {
            level::DailyTask::CreatePosts { amount } => CreatePostsTask { amount }.into(),
            level::DailyTask::EarnPostKarma { amount } => EarnPostKarmaTask { amount }.into(),
            level::DailyTask::PostComments { amount } => PostCommentsTask { amount }.into(),
            level::DailyTask::EarnAnyKarma { amount } => EarnAnyKarmaTask { amount }.into(),
            level::DailyTask::WriteMessages { amount } => WriteMessagesTask { amount }.into(),
            level::DailyTask::RatePublications { amount } => RatePublicationsTask { amount }.into(),
            level::DailyTask::Login => LoginTask { amount: 1 }.into(),
            level::DailyTask::PostInFandom { amount, fandom_id } => PostInFandomTask { amount, fandom_id }.into(),
            level::DailyTask::CommentInFandom { amount, fandom_id } => CommentInFandomTask { amount, fandom_id }.into(),
            level::DailyTask::AnswerNewbieComment { amount, max_level } => AnswerNewbieCommentTask { amount, max_level }.into(),
            level::DailyTask::CommentNewbiePost { amount, max_level } => CommentNewbiePostTask { amount, max_level }.into(),
            level::DailyTask::CreatePostWithPageType { page_type } => CreatePostWithPageTypeTask { amount: 1, page_type: page_type.into() }.into(),
            level::DailyTask::AnswerInChat { amount } => AnswerInChatTask { amount }.into(),
        }
    }
}

#[rustfmt::skip]
mod tasks {
    use async_graphql::SimpleObject;
    use crate::models::pages::PageType;

    /// Create and publish this amount of posts
    #[derive(SimpleObject)] pub struct CreatePostsTask { pub amount: i64 }
    /// Earn this amount of karma with posts
    #[derive(SimpleObject)] pub struct EarnPostKarmaTask { pub amount: i64 }
    /// Post this amount of comments
    #[derive(SimpleObject)] pub struct PostCommentsTask { pub amount: i64 }
    /// Earn this amount of karma with anything
    #[derive(SimpleObject)] pub struct EarnAnyKarmaTask { pub amount: i64 }
    /// Write this amount of messages in a public chat
    #[derive(SimpleObject)] pub struct WriteMessagesTask { pub amount: i64 }
    /// Rate this amount of different publications
    #[derive(SimpleObject)] pub struct RatePublicationsTask { pub amount: i64 }
    /// Just enter the app
    #[derive(SimpleObject)] pub struct LoginTask { pub amount: i64 }
    /// Create and publish this amount of posts in a specific fandom
    #[derive(SimpleObject)] pub struct PostInFandomTask { pub amount: i64, pub fandom_id: i64 }
    /// Post this amount of comments in a specific fandom
    #[derive(SimpleObject)] pub struct CommentInFandomTask { pub amount: i64, pub fandom_id: i64 }
    /// Post this amount of comments answering a newbie
    /// with a level smaller than `max_level`
    #[derive(SimpleObject)] pub struct AnswerNewbieCommentTask { pub amount: i64, pub max_level: i64 }
    /// Post this amount of comments under a post from
    /// a newbie with a level smaller than `max_level`
    #[derive(SimpleObject)] pub struct CommentNewbiePostTask { pub amount: i64, pub max_level: i64 }
    /// Create and publish a post with a page of this type
    #[derive(SimpleObject)] pub struct CreatePostWithPageTypeTask { pub amount: i64, pub page_type: PageType }
    /// Write this amount of messages each answering a different
    /// message in a public chat
    #[derive(SimpleObject)] pub struct AnswerInChatTask { pub amount: i64 }
}

impl User {
    pub(crate) async fn _daily_task(&self, ctx: &Context<'_>) -> Result<DailyTaskInfo, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok(req
            .level
            .get_daily_task(context::current(), self._id)
            .await??
            .into())
    }
}
