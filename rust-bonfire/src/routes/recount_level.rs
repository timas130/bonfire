use crate::consts::achi::*;
use crate::consts::publication_type::PublicationType;
use crate::consts::status::Status;
use crate::consts::{collisions, lvl};
use crate::context::GlobalContext;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::{Extension, Json};
use futures_util::TryStreamExt;
use num_enum::TryFromPrimitive;
use serde::Serialize;
use std::collections::{HashMap, HashSet};

#[derive(Debug, Serialize)]
pub struct LevelRecountReport {
    user_id: i64,
    total_level: u64,
    achievements: HashMap<u64, AchievementRecountReport>,
}
impl LevelRecountReport {
    fn from_list(user_id: i64, list: Vec<AchievementRecountReport>) -> Self {
        Self {
            user_id,
            total_level: 100 + list.iter().map(|report| report.level).sum::<u64>(),
            achievements: list.into_iter().map(|report| (report.id, report)).collect(),
        }
    }
}

#[derive(Debug, Serialize)]
pub struct AchievementRecountReport {
    id: u64,
    count: u64,
    target: Option<usize>,
    level: u64,
}
impl AchievementRecountReport {
    fn from_value(def: &AchiDef, value: i64) -> Self {
        let value = value.max(0).try_into().unwrap_or_default();
        let current_target = def
            .targets
            .iter()
            .enumerate()
            .rfind(|(_, target)| **target <= value);
        let Some((idx, _)) = current_target else {
            return Self {
                id: def.index.into(),
                count: value,
                target: None,
                level: 0,
            };
        };
        Self {
            id: def.index.into(),
            count: value,
            target: Some(idx),
            level: (idx as u64 + 1) * def.force,
        }
    }
}

async fn get_counts(
    context: GlobalContext,
    user_id: i64,
) -> anyhow::Result<HashMap<AchiIndex, i64>> {
    let mut hm = HashMap::new();

    #[rustfmt::skip]
    let achievements_collisions = [
        (AchiIndex::AppShare, collisions::COLLISION_ACHIEVEMENT_SHARE_APP),
        (AchiIndex::Chat, collisions::COLLISION_ACHIEVEMENT_CHAT),
        (AchiIndex::Answer, collisions::COLLISION_ACHIEVEMENT_ANSWER),
        (AchiIndex::Rate, collisions::COLLISION_ACHIEVEMENT_RATE),
        (AchiIndex::ChangePublication, collisions::COLLISION_ACHIEVEMENT_CHANGE_PUBLICATION),
        (AchiIndex::ChangeComment, collisions::COLLISION_ACHIEVEMENT_CHANGE_COMMENT),
        (AchiIndex::Subscribe, collisions::COLLISION_ACCOUNT_FOLLOW),
        (AchiIndex::TagsSearch, collisions::COLLISION_ACHIEVEMENT_TAG_SEARCH),
        (AchiIndex::Language, collisions::COLLISION_ACHIEVEMENT_FANDOM_LANGUAGE),
        (AchiIndex::RulesUser, collisions::COLLISION_ACHIEVEMENT_RULES_USER),
        (AchiIndex::RulesModerator, collisions::COLLISION_ACHIEVEMENT_RULES_MODER),
        (AchiIndex::CreateTag, collisions::COLLISION_ACHIEVEMENT_TAG_CREATE),
        (AchiIndex::ModerChangePostTags, collisions::COLLISION_ACHIEVEMENT_MODERATIONS_POST_TAGS),
        (AchiIndex::Fireworks, collisions::COLLISION_ACHIEVEMENT_FIREWORKS),
        (AchiIndex::MakeModer, collisions::COLLISION_ACHIEVEMENT_MAKE_MODER),
        (AchiIndex::CreateChat, collisions::COLLISION_ACHIEVEMENT_CREATE_FANDOM_CHAT),
        (AchiIndex::ReviewModerAction, collisions::COLLISION_ACHIEVEMENT_REVIEW_MODER_ACTION),
        (AchiIndex::AcceptFandom, collisions::COLLISION_ACHIEVEMENT_ACCEPT_FANDOM),
        (AchiIndex::RelayRaceFirstPost, collisions::COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_POST),
        (AchiIndex::RelayRaceFirstNextMember, collisions::COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_NEXT_MEMBER),
        (AchiIndex::RelayRaceFirstCreate, collisions::COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_CREATE),
        (AchiIndex::ViceroyLink, collisions::COLLISION_ACHIEVEMENT_VICEROY_LINK),
        (AchiIndex::ViceroyImages, collisions::COLLISION_ACHIEVEMENT_VICEROY_IMAGES),
        (AchiIndex::ViceroyDescription, collisions::COLLISION_ACHIEVEMENT_VICEROY_DESCRIPTIONS),
        (AchiIndex::ChatSubscribe, collisions::COLLISION_ACHIEVEMENT_CHAT_SUBSCRIBE),
    ];

    let collisions = sqlx::query_scalar!(
        "select collision_type from collisions
         where owner_id = $1 and collision_type = any($2)
         group by collision_type",
        user_id,
        &achievements_collisions
            .iter()
            .map(|(_, collision)| *collision)
            .collect::<Vec<i64>>(),
    )
    .fetch(&context.pool)
    .try_collect::<HashSet<i64>>()
    .await?;

    achievements_collisions.map(|(index, collision)| {
        hm.insert(index, i64::from(collisions.contains(&collision)));
    });

    hm.insert(
        AchiIndex::ContentShare,
        sqlx::query_scalar!(
            "select count(*) from collisions where collision_type = $1 and collision_id = $2",
            collisions::COLLISION_SHARE,
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::Enters,
        sqlx::query_scalar!(
            "select count(distinct to_timestamp(date_create / 1000)::date)
             from accounts_enters
             where account_id = $1",
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    // todo: this query is too slow
    // hm.insert(
    //     AchiIndex::KarmaCount,
    //     sqlx::query_scalar!(
    //         "select sum(ukt.karma_count)::bigint
    //          from units u
    //          inner join (
    //              select unit_id, sum(karma_count) as karma_count
    //              from units_karma_transactions
    //              where change_account_karma
    //              group by unit_id
    //          ) ukt on ukt.unit_id = u.id
    //          where u.creator_id = $1 and u.status = $2",
    //         user_id,
    //         i64::from(Status::Public),
    //     ).fetch_one(&context.pool).await?.unwrap_or(0),
    // );
    hm.insert(
        AchiIndex::KarmaCount,
        sqlx::query_scalar!(
            "select collision_id from collisions where owner_id = $1 and collision_type = $2",
            user_id,
            collisions::COLLISION_ACCOUNT_KARMA_COUNT
        )
        .fetch_optional(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ReferralsCount,
        sqlx::query_scalar!(
            "select count(*) from accounts
             where recruiter_id = $1 and to_timestamp(ban_date / 1000) < now()",
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::RatesCount,
        sqlx::query_scalar!(
            "select count(*) from units_karma_transactions where from_account_id = $1",
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );

    #[derive(Clone, Default)]
    struct UnitCountKarma {
        count: i64,
        max_karma: i64,
    }
    let units_agg = sqlx::query!(
        "select
             unit_type,
             count(*),
             max(karma_count) as max_karma
         from units
         where creator_id = $1 and status = $2 and unit_type = any($3)
         group by unit_type",
        user_id,
        i64::from(Status::Public),
        &[
            i64::from(PublicationType::Post),
            i64::from(PublicationType::Comment),
            i64::from(PublicationType::Quest),
            i64::from(PublicationType::Moderation),
            i64::from(PublicationType::StickersPack),
        ],
    )
    .fetch(&context.pool)
    .map_ok(|row| {
        (
            PublicationType::try_from_primitive(row.unit_type)
                .expect("db returned unexpected pub type"),
            UnitCountKarma {
                count: row.count.unwrap_or(0),
                max_karma: row.max_karma.unwrap_or(0),
            },
        )
    })
    .try_collect::<HashMap<PublicationType, UnitCountKarma>>()
    .await?;

    let [comments, posts, quests, moderation, sticker_packs] = [
        PublicationType::Comment,
        PublicationType::Post,
        PublicationType::Quest,
        PublicationType::Moderation,
        PublicationType::StickersPack,
    ]
    .map(|pub_type| units_agg.get(&pub_type).cloned().unwrap_or_default());

    // actually "comment karma", because it's "single best comment"
    hm.insert(AchiIndex::CommentsCount, comments.count);
    hm.insert(AchiIndex::CommentsKarma, comments.max_karma);
    hm.insert(AchiIndex::Comment, i64::from(comments.count > 0));
    hm.insert(AchiIndex::PostsCount, posts.count);
    hm.insert(AchiIndex::PostKarma, posts.max_karma);
    hm.insert(AchiIndex::FirstPost, i64::from(posts.count > 0));
    hm.insert(AchiIndex::Quests, quests.count);
    hm.insert(AchiIndex::QuestKarma, quests.max_karma);
    hm.insert(AchiIndex::ModeratorActionKarma, moderation.count);
    hm.insert(AchiIndex::StickersKarma, sticker_packs.max_karma);

    let account = sqlx::query!(
        "select name,
                img_title_id,
                coalesce(recruiter_id, 0) as \"recruiter_id!\",
                karma_count,
                date_create
         from accounts
         where id = $1",
        user_id
    )
    .fetch_one(&context.pool)
    .await?;

    hm.insert(AchiIndex::Login, i64::from(!account.name.contains('#')));
    hm.insert(AchiIndex::TitleImage, i64::from(account.img_title_id > 0));
    hm.insert(AchiIndex::AddRecruiter, i64::from(account.recruiter_id > 0));
    hm.insert(AchiIndex::Karma30, account.karma_count);

    // earlier than Fri Sep 01 2023 00:00:00 GMT+0300
    if account.date_create < 1693515600000 {
        hm.insert(AchiIndex::Bonus, 10);
    }

    hm.insert(
        AchiIndex::Fandoms,
        sqlx::query_scalar!(
            "select count(*) from fandoms where creator_id = $1 and status = $2",
            user_id,
            i64::from(Status::Public),
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::Followers,
        sqlx::query_scalar!(
            "select count(*)
             from collisions c
             inner join accounts a on a.id = c.owner_id
             where collision_type = $1 and collision_id = $2 and to_timestamp(a.ban_date / 1000) < now()",
            collisions::COLLISION_ACCOUNT_FOLLOW,
            user_id,
        ).fetch_one(&context.pool).await?.unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ModeratorCount,
        sqlx::query_scalar!(
            "select count(*) from collisions
             where owner_id = $1 and collision_type = $2 and value_1 >= $3",
            user_id,
            collisions::COLLISION_KARMA_30,
            lvl::MODERATOR_BLOCK_KARMA,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );

    let up_rates = sqlx::query_scalar!(
        "select count(*) from units_karma_transactions
         where target_account_id = $1 and change_account_karma and karma_count > 0",
        user_id,
    )
    .fetch_one(&context.pool)
    .await?
    .unwrap_or(0);
    let down_rates = sqlx::query_scalar!(
        "select count(*) from units_karma_transactions
         where target_account_id = $1 and change_account_karma and karma_count < 0",
        user_id,
    )
    .fetch_one(&context.pool)
    .await?
    .unwrap_or(0);
    hm.insert(AchiIndex::UpRates, up_rates);
    hm.insert(AchiIndex::UpRatesOverDown, up_rates - down_rates);

    hm.insert(
        AchiIndex::RelayRacePostsCount,
        sqlx::query_scalar!(
            "select count(*)
             from activities_collisions ac
             inner join campfire_db.units u on ac.tag_1 = u.id
             where u.status = $1 and ac.account_id = $2",
            i64::from(Status::Public),
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::RelayRaceMyRacePostsCount,
        sqlx::query_scalar!(
            "select count(*)
             from units u
             inner join activities_collisions ac on ac.tag_1 = u.id
             inner join activities a on a.id = ac.activity_id
             where u.unit_type = $1 and u.status = $2 and a.creator_id = $3 and ac.type = $4",
            i64::from(PublicationType::Post),
            i64::from(Status::Public),
            user_id,
            collisions::ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ViceroyAssign,
        i64::from(
            sqlx::query_scalar!(
                "select collision_id from collisions
                 where value_1 = $1 and collision_type = $2
                 limit 1",
                user_id,
                collisions::COLLISION_FANDOM_VICEROY,
            )
            .fetch_optional(&context.pool)
            .await?
            .is_some(),
        ),
    );
    hm.insert(
        AchiIndex::ViceroyPostsCount,
        sqlx::query_scalar!(
            "select count(*)
             from units u
             inner join collisions c on c.owner_id = u.fandom_id and u.language_id = c.collision_id
             where c.collision_type = $1 and u.status = $2 and
                   u.unit_type = $3 and c.value_1 = $4 and
                   u.date_create >= c.collision_date_create",
            collisions::COLLISION_FANDOM_VICEROY,
            i64::from(Status::Public),
            i64::from(PublicationType::Post),
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ViceroyWikiCount,
        sqlx::query_scalar!(
            "select count(*)
             from wiki_titles wt
             inner join collisions c on c.owner_id = wt.fandom_id
             where c.collision_type = $1 and wt.wiki_status = $2 and
                   c.value_1 = $3 and wt.date_create >= c.collision_date_create",
            collisions::COLLISION_FANDOM_VICEROY,
            i64::from(Status::Public),
            user_id,
        )
        .fetch_one(&context.pool)
        .await?
        .unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ViceroyKarmaCount,
        sqlx::query_scalar!(
            "select sum(kc.value_1)::bigint
             from collisions kc
             inner join collisions vc on kc.collision_id = vc.owner_id and kc.collision_sub_id = vc.collision_id
             where vc.collision_type = $1 and kc.collision_type = $2 and
                   vc.value_1 = $3",
            collisions::COLLISION_FANDOM_VICEROY,
            collisions::COLLISION_KARMA_30,
            user_id,
        ).fetch_one(&context.pool).await?.unwrap_or(0),
    );
    hm.insert(
        AchiIndex::ViceroySubscribersCount,
        sqlx::query_scalar!(
            "select count(*)
             from collisions fc
             inner join collisions vc on fc.collision_id = vc.owner_id and fc.collision_sub_id = vc.collision_id
             where vc.collision_type = $1 and fc.collision_type = $2 and
                   vc.value_1 = $3",
            collisions::COLLISION_FANDOM_VICEROY,
            collisions::COLLISION_FANDOM_SUBSCRIBE,
            user_id,
        ).fetch_one(&context.pool).await?.unwrap_or(0),
    );

    Ok(hm)
}

pub async fn recount_level_route(
    Path(id): Path<i64>,
    Extension(context): Extension<GlobalContext>,
) -> Result<Json<LevelRecountReport>, StatusCode> {
    let counts = get_counts(context, id)
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    let counts = counts
        .into_iter()
        .map(|(index, count)| (ACHIEVEMENTS.get(&index), count))
        .filter_map(|(def, count)| def.map(|def| (def, count)))
        .map(|(def, count)| AchievementRecountReport::from_value(def, count))
        .collect::<Vec<AchievementRecountReport>>();
    Ok(Json(LevelRecountReport::from_list(id, counts)))
}
