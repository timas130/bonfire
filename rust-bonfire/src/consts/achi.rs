use std::collections::HashMap;
use konst::iter::collect_const;
use lazy_static::lazy_static;
use num_enum::{IntoPrimitive, TryFromPrimitive};

#[derive(Debug, Copy, Clone, Eq, PartialEq, IntoPrimitive, TryFromPrimitive, Hash)]
#[repr(u64)]
pub enum AchiIndex {
    AppShare = 2,
    ContentShare = 3,
    AddRecruiter = 4,
    Enters = 5,
    KarmaCount = 6,
    ReferralsCount = 7,
    RatesCount = 8,
    CommentsKarma = 12,
    PostsCount = 15,
    CommentsCount = 16,
    Login = 28,
    Chat = 29,
    Comment = 30,
    Answer = 31,
    Rate = 32,
    ChangePublication = 33,
    ChangeComment = 34,
    PostKarma = 36,
    FirstPost = 37,
    Subscribe = 38,
    TagsSearch = 39,
    Language = 40,
    TitleImage = 41,
    CreateTag = 42,
    Quests = 43,
    Fandoms = 44,
    RulesUser = 45,
    RulesModerator = 46,
    Followers = 47,
    ModerChangePostTags = 48,
    Fireworks = 50,
    MakeModer = 51,
    CreateChat = 52,
    ReviewModerAction = 53,
    AcceptFandom = 54,
    ModeratorCount = 55,
    ModeratorActionKarma = 56,
    Karma30 = 57,
    UpRates = 58,
    UpRatesOverDown = 59,
    ChatSubscribe = 60,
    StickersKarma = 61,
    RelayRaceFirstPost = 64,
    RelayRaceFirstNextMember = 65,
    RelayRaceFirstCreate = 66,
    RelayRacePostsCount = 67,
    RelayRaceMyRacePostsCount = 68,
    ViceroyAssign = 69,
    ViceroyPostsCount = 70,
    ViceroyWikiCount = 71,
    ViceroyKarmaCount = 72,
    ViceroySubscribersCount = 73,
    ViceroyLink = 74,
    ViceroyImages = 75,
    ViceroyDescription = 76,
    QuestKarma = 77,
}

pub struct AchiDef<'a> {
    pub index: AchiIndex,
    pub force: u64,
    pub targets: &'a [u64],
}
impl<'a> AchiDef<'a> {
    const fn new_static(index: AchiIndex, force: u64, targets: &'a [u64]) -> Self {
        Self {
            index,
            force,
            targets,
        }
    }
}
impl<'a> PartialEq for AchiDef<'a> {
    fn eq(&self, other: &Self) -> bool {
        self.index == other.index
    }
}

pub const ACHI_APP_SHARE: AchiDef = AchiDef::new_static(AchiIndex::AppShare, 5, &[1]);
pub const ACHI_CONTENT_SHARE: AchiDef = AchiDef::new_static(AchiIndex::ContentShare, 5, &[1, 10, 30]);
pub const ACHI_ADD_RECRUITER: AchiDef = AchiDef::new_static(AchiIndex::AddRecruiter, 2 * 10, &[1]);
pub const ACHI_ENTERS: AchiDef = AchiDef::new_static(AchiIndex::Enters, 3 * 5, &collect_const!(u64 => (1..=500), map(|idx| idx * 5)));
pub const ACHI_KARMA_COUNT: AchiDef = AchiDef::new_static(AchiIndex::KarmaCount, 4 * 5, &collect_const!(u64 => (1..=20), map(|idx| idx * 20000)));
pub const ACHI_REFERRALS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ReferralsCount, 2 * 10, &[3, 12, 36]);
pub const ACHI_RATES_COUNT: AchiDef = AchiDef::new_static(AchiIndex::RatesCount, 3 * 5, &[100]);
pub const ACHI_COMMENTS_KARMA: AchiDef = AchiDef::new_static(AchiIndex::CommentsKarma, 35, &[5000, 15000, 20000, 30000]);
pub const ACHI_POSTS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::PostsCount, 4 * 5, &[10, 20, 50, 100, 150]);
pub const ACHI_COMMENTS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::CommentsCount, 4 * 5, &[100]);
pub const ACHI_LOGIN: AchiDef = AchiDef::new_static(AchiIndex::Login, 2 * 5, &[1]);
pub const ACHI_CHAT: AchiDef = AchiDef::new_static(AchiIndex::Chat, 3 * 5, &[1]);
pub const ACHI_COMMENT: AchiDef = AchiDef::new_static(AchiIndex::Comment, 3 * 5, &[1]);
pub const ACHI_ANSWER: AchiDef = AchiDef::new_static(AchiIndex::Answer, 3 * 5, &[1]);
pub const ACHI_RATE: AchiDef = AchiDef::new_static(AchiIndex::Rate, 3 * 5, &[1]);
pub const ACHI_CHANGE_PUBLICATION: AchiDef = AchiDef::new_static(AchiIndex::ChangePublication, 3 * 5, &[1]);
pub const ACHI_CHANGE_COMMENT: AchiDef = AchiDef::new_static(AchiIndex::ChangeComment, 3 * 5, &[1]);
pub const ACHI_POST_KARMA: AchiDef = AchiDef::new_static(AchiIndex::PostKarma, 4 * 10, &[10000, 40000, 70000]);
pub const ACHI_FIRST_POST: AchiDef = AchiDef::new_static(AchiIndex::FirstPost, 4 * 5, &[1]);
pub const ACHI_SUBSCRIBE: AchiDef = AchiDef::new_static(AchiIndex::Subscribe, 3 * 5, &[1]);
pub const ACHI_TAGS_SEARCH: AchiDef = AchiDef::new_static(AchiIndex::TagsSearch, 3 * 5, &[1]);
pub const ACHI_LANGUAGE: AchiDef = AchiDef::new_static(AchiIndex::Language, 3 * 5, &[1]);
pub const ACHI_TITLE_IMAGE: AchiDef = AchiDef::new_static(AchiIndex::TitleImage, 3 * 5, &[1]);
pub const ACHI_CREATE_TAG: AchiDef = AchiDef::new_static(AchiIndex::CreateTag, 3 * 5, &[1]);
pub const ACHI_QUESTS: AchiDef = AchiDef::new_static(AchiIndex::Quests, 3 * 2, &collect_const!(u64 => (1..=15)));
pub const ACHI_FANDOMS: AchiDef = AchiDef::new_static(AchiIndex::Fandoms, 5, &[1, 5, 10, 20]);
pub const ACHI_RULES_USER: AchiDef = AchiDef::new_static(AchiIndex::RulesUser, 4 * 5, &[1]);
pub const ACHI_RULES_MODERATOR: AchiDef = AchiDef::new_static(AchiIndex::RulesModerator, 2 * 5, &[1]);
pub const ACHI_FOLLOWERS: AchiDef = AchiDef::new_static(AchiIndex::Followers, 5, &[10, 100, 200, 400, 600, 800, 1000, 3000]);
pub const ACHI_MODER_CHANGE_POST_TAGS: AchiDef = AchiDef::new_static(AchiIndex::ModerChangePostTags, 3 * 5, &[1]);
pub const ACHI_FIREWORKS: AchiDef = AchiDef::new_static(AchiIndex::Fireworks, 3, &[1]);
pub const ACHI_MAKE_MODER: AchiDef = AchiDef::new_static(AchiIndex::MakeModer, 5, &[1]);
pub const ACHI_CREATE_CHAT: AchiDef = AchiDef::new_static(AchiIndex::CreateChat, 2 * 5, &[1]);
pub const ACHI_REVIEW_MODER_ACTION: AchiDef = AchiDef::new_static(AchiIndex::ReviewModerAction, 2 * 5, &[1]);
pub const ACHI_ACCEPT_FANDOM: AchiDef = AchiDef::new_static(AchiIndex::AcceptFandom, 2 * 5, &[1]);
pub const ACHI_MODERATOR_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ModeratorCount, 2 * 5, &[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
pub const ACHI_MODERATOR_ACTION_KARMA: AchiDef = AchiDef::new_static(AchiIndex::ModeratorActionKarma, 2 * 5, &[5000, 8000, 10000, 25000, 40000]);
pub const ACHI_KARMA_30: AchiDef = AchiDef::new_static(AchiIndex::Karma30, 3 * 5, &[50000, 75000, 100000, 130000, 160000, 200000, 250000]);
pub const ACHI_UP_RATES: AchiDef = AchiDef::new_static(AchiIndex::UpRates, 4 * 2, &[10, 50, 150, 300, 500, 750, 1000]);
pub const ACHI_UP_RATES_OVER_DOWN: AchiDef = AchiDef::new_static(AchiIndex::UpRatesOverDown, 3 * 2, &[5, 20, 50, 150, 300, 500, 750]);
pub const ACHI_CHAT_SUBSCRIBE: AchiDef = AchiDef::new_static(AchiIndex::ChatSubscribe, 3 * 2, &[1]);
pub const ACHI_STICKERS_KARMA: AchiDef = AchiDef::new_static(AchiIndex::StickersKarma, 3 * 10, &[5000, 25000, 50000]);
pub const ACHI_RELAY_RACE_FIRST_POST: AchiDef = AchiDef::new_static(AchiIndex::RelayRaceFirstPost, 3, &[1]);
pub const ACHI_RELAY_RACE_FIRST_NEXT_MEMBER: AchiDef = AchiDef::new_static(AchiIndex::RelayRaceFirstNextMember, 3, &[1]);
pub const ACHI_RELAY_RACE_FIRST_CREATE: AchiDef = AchiDef::new_static(AchiIndex::RelayRaceFirstCreate, 3, &[1]);
pub const ACHI_RELAY_RACE_POSTS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::RelayRacePostsCount, 3 * 5, &[5, 10, 20, 50]);
pub const ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::RelayRaceMyRacePostsCount, 3 * 5, &[5, 10, 20, 50, 100]);
pub const ACHI_VICEROY_ASSIGN: AchiDef = AchiDef::new_static(AchiIndex::ViceroyAssign, 3, &[1]);
pub const ACHI_VICEROY_POSTS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ViceroyPostsCount, 3 * 2, &[5, 10, 20, 50]);
pub const ACHI_VICEROY_WIKI_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ViceroyWikiCount, 3 * 2, &[1, 5, 10, 50, 100]);
pub const ACHI_VICEROY_KARMA_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ViceroyKarmaCount, 3 * 2, &[50000, 200000, 500000, 1000000]);
pub const ACHI_VICEROY_SUBSCRIBERS_COUNT: AchiDef = AchiDef::new_static(AchiIndex::ViceroySubscribersCount, 3 * 2, &[10, 20, 50, 100, 500]);
pub const ACHI_VICEROY_LINK: AchiDef = AchiDef::new_static(AchiIndex::ViceroyLink, 3, &[1]);
pub const ACHI_VICEROY_IMAGES: AchiDef = AchiDef::new_static(AchiIndex::ViceroyImages, 3, &[1]);
pub const ACHI_VICEROY_DESCRIPTION: AchiDef = AchiDef::new_static(AchiIndex::ViceroyDescription, 3, &[1]);
pub const ACHI_QUEST_KARMA: AchiDef = AchiDef::new_static(AchiIndex::QuestKarma, 3 * 15, &[7000, 14000, 25000, 37000]);

lazy_static! {
    pub static ref ACHIEVEMENTS: HashMap<AchiIndex, AchiDef<'static>> = {
        let mut hm = HashMap::new();
        hm.insert(AchiIndex::AppShare, ACHI_APP_SHARE);
        hm.insert(AchiIndex::ContentShare, ACHI_CONTENT_SHARE);
        hm.insert(AchiIndex::AddRecruiter, ACHI_ADD_RECRUITER);
        hm.insert(AchiIndex::Enters, ACHI_ENTERS);
        hm.insert(AchiIndex::KarmaCount, ACHI_KARMA_COUNT);
        hm.insert(AchiIndex::ReferralsCount, ACHI_REFERRALS_COUNT);
        hm.insert(AchiIndex::RatesCount, ACHI_RATES_COUNT);
        hm.insert(AchiIndex::CommentsKarma, ACHI_COMMENTS_KARMA);
        hm.insert(AchiIndex::PostsCount, ACHI_POSTS_COUNT);
        hm.insert(AchiIndex::CommentsCount, ACHI_COMMENTS_COUNT);
        hm.insert(AchiIndex::Login, ACHI_LOGIN);
        hm.insert(AchiIndex::Chat, ACHI_CHAT);
        hm.insert(AchiIndex::Comment, ACHI_COMMENT);
        hm.insert(AchiIndex::Answer, ACHI_ANSWER);
        hm.insert(AchiIndex::Rate, ACHI_RATE);
        hm.insert(AchiIndex::ChangePublication, ACHI_CHANGE_PUBLICATION);
        hm.insert(AchiIndex::ChangeComment, ACHI_CHANGE_COMMENT);
        hm.insert(AchiIndex::PostKarma, ACHI_POST_KARMA);
        hm.insert(AchiIndex::FirstPost, ACHI_FIRST_POST);
        hm.insert(AchiIndex::Subscribe, ACHI_SUBSCRIBE);
        hm.insert(AchiIndex::TagsSearch, ACHI_TAGS_SEARCH);
        hm.insert(AchiIndex::Language, ACHI_LANGUAGE);
        hm.insert(AchiIndex::TitleImage, ACHI_TITLE_IMAGE);
        hm.insert(AchiIndex::CreateTag, ACHI_CREATE_TAG);
        hm.insert(AchiIndex::Quests, ACHI_QUESTS);
        hm.insert(AchiIndex::Fandoms, ACHI_FANDOMS);
        hm.insert(AchiIndex::RulesUser, ACHI_RULES_USER);
        hm.insert(AchiIndex::RulesModerator, ACHI_RULES_MODERATOR);
        hm.insert(AchiIndex::Followers, ACHI_FOLLOWERS);
        hm.insert(AchiIndex::ModerChangePostTags, ACHI_MODER_CHANGE_POST_TAGS);
        hm.insert(AchiIndex::Fireworks, ACHI_FIREWORKS);
        hm.insert(AchiIndex::MakeModer, ACHI_MAKE_MODER);
        hm.insert(AchiIndex::CreateChat, ACHI_CREATE_CHAT);
        hm.insert(AchiIndex::ReviewModerAction, ACHI_REVIEW_MODER_ACTION);
        hm.insert(AchiIndex::AcceptFandom, ACHI_ACCEPT_FANDOM);
        hm.insert(AchiIndex::ModeratorCount, ACHI_MODERATOR_COUNT);
        hm.insert(AchiIndex::ModeratorActionKarma, ACHI_MODERATOR_ACTION_KARMA);
        hm.insert(AchiIndex::Karma30, ACHI_KARMA_30);
        hm.insert(AchiIndex::UpRates, ACHI_UP_RATES);
        hm.insert(AchiIndex::UpRatesOverDown, ACHI_UP_RATES_OVER_DOWN);
        hm.insert(AchiIndex::ChatSubscribe, ACHI_CHAT_SUBSCRIBE);
        hm.insert(AchiIndex::StickersKarma, ACHI_STICKERS_KARMA);
        hm.insert(AchiIndex::RelayRaceFirstPost, ACHI_RELAY_RACE_FIRST_POST);
        hm.insert(AchiIndex::RelayRaceFirstNextMember, ACHI_RELAY_RACE_FIRST_NEXT_MEMBER);
        hm.insert(AchiIndex::RelayRaceFirstCreate, ACHI_RELAY_RACE_FIRST_CREATE);
        hm.insert(AchiIndex::RelayRacePostsCount, ACHI_RELAY_RACE_POSTS_COUNT);
        hm.insert(AchiIndex::RelayRaceMyRacePostsCount, ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT);
        hm.insert(AchiIndex::ViceroyAssign, ACHI_VICEROY_ASSIGN);
        hm.insert(AchiIndex::ViceroyPostsCount, ACHI_VICEROY_POSTS_COUNT);
        hm.insert(AchiIndex::ViceroyWikiCount, ACHI_VICEROY_WIKI_COUNT);
        hm.insert(AchiIndex::ViceroyKarmaCount, ACHI_VICEROY_KARMA_COUNT);
        hm.insert(AchiIndex::ViceroySubscribersCount, ACHI_VICEROY_SUBSCRIBERS_COUNT);
        hm.insert(AchiIndex::ViceroyLink, ACHI_VICEROY_LINK);
        hm.insert(AchiIndex::ViceroyImages, ACHI_VICEROY_IMAGES);
        hm.insert(AchiIndex::ViceroyDescription, ACHI_VICEROY_DESCRIPTION);
        hm.insert(AchiIndex::QuestKarma, ACHI_QUEST_KARMA);
        hm
    };
}
