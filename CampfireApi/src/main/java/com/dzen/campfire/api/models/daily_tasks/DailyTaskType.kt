package com.dzen.campfire.api.models.daily_tasks

enum class DailyTaskType(val karmaTask: Boolean) {
    CreatePosts(false),
    EarnPostKarma(true),
    PostComments(false),
    EarnAnyKarma(true),
    WriteMessages(false),
    RatePublications(false),
    Login(false),
    PostInFandom(false),
    CommentInFandom(false),
    AnswerNewbieComment(false),
    CommentNewbiePost(false),
    CreatePostWithPageType(false),
    AnswerInChat(false),
    Unknown(false),
}
