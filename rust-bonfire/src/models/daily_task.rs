use crate::consts::page::PageType;
use serde::{Deserialize, Serialize};

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

    pub fn get_fandom_id(self) -> Option<i64> {
        Some(match self {
            DailyTask::PostInFandom { fandom_id, .. } => fandom_id,
            DailyTask::CommentInFandom { fandom_id, .. } => fandom_id,
            _ => return None,
        })
    }
}

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
