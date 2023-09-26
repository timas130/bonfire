use crate::consts::chat::ChatType;
use crate::consts::publication::PublicationType;
use crate::consts::status::Status;
use crate::context::GlobalContext;
use crate::models::daily_task::DailyTask;
use chrono::{NaiveDate, NaiveTime};
use crate::consts::fandoms::FANDOM_HELLO_ID;

pub async fn get_task_progress(
    context: &GlobalContext,
    task: &DailyTask,
    user_id: i64,
    date: &NaiveDate,
) -> Result<i64, sqlx::Error> {
    let date_start = date.and_time(NaiveTime::MIN).timestamp_millis();
    let date_end = date
        .and_time(NaiveTime::from_hms_opt(23, 59, 59).unwrap())
        .timestamp_millis();

    let progress = match task {
        DailyTask::CreatePosts { .. } => sqlx::query_scalar!(
            "select count(*) from campfire_db.units
             where creator_id = $1 and status = $2 and unit_type = $3 and
                   date_create >= $4 and date_create <= $5",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Post),
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::EarnPostKarma { .. } => sqlx::query_scalar!(
            "select sum(ukt.karma_count)::bigint from campfire_db.units_karma_transactions ukt
             inner join campfire_db.units u on u.id = ukt.unit_id
             where target_account_id = $1 and u.status = $2 and u.unit_type = $3 and
                   ukt.date_create >= $4 and ukt.date_create <= $5 and
                   u.date_create >= $4 and u.date_create <= $5",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Post),
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::PostComments { .. } => sqlx::query_scalar!(
            "select count(*) from campfire_db.units
             where creator_id = $1 and status = $2 and unit_type = $3 and
                   date_create >= $4 and date_create <= $5 and fandom_id != $6",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Comment),
            date_start,
            date_end,
            FANDOM_HELLO_ID,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::EarnAnyKarma { .. } => sqlx::query_scalar!(
            "select sum(ukt.karma_count)::bigint from campfire_db.units_karma_transactions ukt
             inner join campfire_db.units u on u.id = ukt.unit_id
             where target_account_id = $1 and u.status = $2 and
                   ukt.date_create >= $3 and ukt.date_create <= $4 and
                   u.date_create >= $3 and u.date_create <= $4",
            user_id,
            i64::from(Status::Public),
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::WriteMessages { .. } => sqlx::query_scalar!(
            "select count(*) from campfire_db.units
             where creator_id = $1 and status = $2 and unit_type = $3 and
                   date_create >= $4 and date_create <= $5 and tag_1 = $6 and
                   fandom_id != $7",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::ChatMessage),
            date_start,
            date_end,
            i64::from(ChatType::FandomRoot),
            FANDOM_HELLO_ID,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::RatePublications { .. } => sqlx::query_scalar!(
            "select count(*)
             from campfire_db.units_karma_transactions
             where from_account_id = $1 and
                   date_create >= $2 and date_create <= $3",
            user_id,
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::Login => i64::from(
            sqlx::query_scalar!(
                "select checked_in from daily_tasks
                 where account_id = $1 and date = $2",
                user_id,
                date,
            )
            .fetch_optional(&context.pool)
            .await?
            .unwrap_or(false),
        ),

        DailyTask::PostInFandom { fandom_id, .. } => sqlx::query_scalar!(
            "select count(*) from units
             where creator_id = $1 and fandom_id = $2 and status = $3 and
                   unit_type = $4 and
                   date_create >= $5 and date_create <= $6",
            user_id,
            fandom_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Post),
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::CommentInFandom { fandom_id, .. } => sqlx::query_scalar!(
            "select count(*) from units
             where creator_id = $1 and fandom_id = $2 and status = $3 and
                   unit_type = $4 and
                   date_create >= $5 and date_create <= $6",
            user_id,
            fandom_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Comment),
            date_start,
            date_end,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::AnswerNewbieComment { max_level, .. } => sqlx::query_scalar!(
            "select count(*) from units u
             /* yep. postgresql json, here i come */
             inner join units pu on (u.unit_json::json->>'J_PARENT_COMMENT_ID')::bigint = pu.id
             inner join accounts pua on pu.creator_id = pua.id
             where u.creator_id = $1 and u.status = $2 and u.unit_type = $3 and
                   u.date_create >= $4 and u.date_create <= $5 and pua.lvl < $6 and
                   pu.creator_id != u.creator_id",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Comment),
            date_start,
            date_end,
            max_level,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::CommentNewbiePost { max_level, .. } => sqlx::query_scalar!(
            "select count(*) from units u
             /* yep. postgresql json, here i come */
             inner join units pu on u.parent_unit_id = pu.id
             inner join accounts pua on pu.creator_id = pua.id
             where u.creator_id = $1 and u.status = $2 and u.unit_type = $3 and
                   u.date_create >= $4 and u.date_create <= $5 and pua.lvl < $6 and
                   pu.creator_id != u.creator_id",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Comment),
            date_start,
            date_end,
            max_level,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::CreatePostWithPageType { page_type } => sqlx::query_scalar!(
            "select count(*)
             from units u
             /* what the fuck */
             cross join lateral json_array_elements(u.unit_json::json->'J_PAGES') as pages
             where u.creator_id = $1 and u.status = $2 and u.unit_type = $3 and
                   u.date_create >= $4 and u.date_create <= $5 and
                   (pages->>'J_PAGE_TYPE')::bigint = $6",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::Post),
            date_start,
            date_end,
            i64::from(*page_type),
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),

        DailyTask::AnswerInChat { .. } => sqlx::query_scalar!(
            "select count(distinct qu.id) from units u
             inner join units qu on (u.unit_json::json->>'quoteId')::bigint = qu.id
             where u.creator_id = $1 and u.status = $2 and u.unit_type = $3 and
                   u.date_create >= $4 and u.date_create <= $5 and qu.status = $2 and
                   qu.creator_id != $1 and u.tag_1 = $6",
            user_id,
            i64::from(Status::Public),
            i64::from(PublicationType::ChatMessage),
            date_start,
            date_end,
            i64::from(ChatType::FandomRoot),
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    };

    Ok(progress)
}
