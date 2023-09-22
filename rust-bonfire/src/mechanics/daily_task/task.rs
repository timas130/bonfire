use crate::consts::page::PageType;
use crate::context::GlobalContext;
use crate::mechanics::daily_task::{fandom, seed};
use crate::models::daily_task::{DailyTask, DailyTaskType};
use chrono::NaiveDate;
use rand::distributions::{Uniform, WeightedIndex};
use rand::{Rng, SeedableRng};
use rand_xoshiro::Xoshiro256PlusPlus;

const NEWBIE_MAX_LEVEL: i64 = 400;
const TASK_CHANCES: &[(DailyTaskType, i32)] = &[
    (DailyTaskType::CreatePosts, 13),
    (DailyTaskType::EarnPostKarma, 18),
    (DailyTaskType::PostComments, 7),
    (DailyTaskType::EarnAnyKarma, 19),
    (DailyTaskType::WriteMessages, 6),
    (DailyTaskType::RatePublications, 3),
    (DailyTaskType::Login, 1),
    (DailyTaskType::PostInFandom, 15),
    (DailyTaskType::CommentInFandom, 20),
    (DailyTaskType::AnswerNewbieComment, 10),
    (DailyTaskType::CommentNewbiePost, 10),
    (DailyTaskType::CreatePostWithPageType, 10),
    (DailyTaskType::AnswerInChat, 12),
];

pub async fn determine_task(
    context: &GlobalContext,
    user_id: i64,
    level: i64,
    date: &NaiveDate,
) -> error_stack::Result<DailyTask, sqlx::Error> {
    let seed = seed::get_seed_for_day(context, user_id, date).await?;
    let mut rng = Xoshiro256PlusPlus::from_seed(seed);

    let task_dist =
        WeightedIndex::new(TASK_CHANCES.iter().map(|it| it.1)).expect("invalid daily task chances");

    loop {
        let task_type = TASK_CHANCES[rng.sample(&task_dist)].0;

        let task = match task_type {
            DailyTaskType::CreatePosts => {
                let max_posts = if level > 1000 {
                    4
                } else if level > 600 {
                    3
                } else if level > 400 {
                    2
                } else {
                    1
                };
                let amount = rng.sample(Uniform::new_inclusive(1, max_posts));
                DailyTask::CreatePosts { amount }
            }
            DailyTaskType::EarnPostKarma => {
                let max_karma = if level > 1000 {
                    130
                } else if level > 600 {
                    100
                } else if level > 400 {
                    50
                } else {
                    30
                };
                let amount = rng.sample(Uniform::new_inclusive(10, max_karma));
                DailyTask::EarnPostKarma {
                    amount: amount * 100,
                }
            }
            DailyTaskType::PostComments => {
                let max_comments = if level > 1000 {
                    25
                } else if level > 600 {
                    15
                } else if level > 400 {
                    8
                } else {
                    3
                };
                let amount = rng.sample(Uniform::new_inclusive(2, max_comments));
                DailyTask::PostComments { amount }
            }
            DailyTaskType::EarnAnyKarma => {
                let max_karma = if level > 1000 {
                    180
                } else if level > 600 {
                    120
                } else if level > 400 {
                    80
                } else {
                    40
                };
                let amount = rng.sample(Uniform::new_inclusive(10, max_karma));
                DailyTask::EarnAnyKarma {
                    amount: amount * 100,
                }
            }
            DailyTaskType::WriteMessages => {
                let max_messages = if level > 1000 {
                    30
                } else if level > 600 {
                    20
                } else {
                    10
                };
                let amount = rng.sample(Uniform::new_inclusive(3, max_messages));
                DailyTask::WriteMessages { amount }
            }
            DailyTaskType::RatePublications => {
                let max_rates = if level > 1000 {
                    30
                } else if level > 600 {
                    20
                } else if level > 400 {
                    10
                } else {
                    5
                };
                let amount = rng.sample(Uniform::new_inclusive(3, max_rates));
                DailyTask::RatePublications { amount }
            }
            DailyTaskType::Login => DailyTask::Login,
            DailyTaskType::PostInFandom => {
                let amount = rng.sample(Uniform::new_inclusive(1, 2));
                let fandom_id =
                    fandom::determine_dt_fandom(&mut rng, context, user_id, date).await?;
                let Some(fandom_id) = fandom_id else {
                    continue;
                };
                DailyTask::PostInFandom { amount, fandom_id }
            }
            DailyTaskType::CommentInFandom => {
                let amount = rng.sample(Uniform::new_inclusive(2, 6));
                let fandom_id =
                    fandom::determine_dt_fandom(&mut rng, context, user_id, date).await?;
                let Some(fandom_id) = fandom_id else {
                    continue;
                };
                DailyTask::CommentInFandom { amount, fandom_id }
            }
            DailyTaskType::AnswerNewbieComment => {
                let amount = rng.sample(Uniform::new_inclusive(2, 6));
                DailyTask::AnswerNewbieComment {
                    amount,
                    max_level: NEWBIE_MAX_LEVEL,
                }
            }
            DailyTaskType::CommentNewbiePost => {
                let amount = rng.sample(Uniform::new_inclusive(1, 5));
                DailyTask::CommentNewbiePost {
                    amount,
                    max_level: NEWBIE_MAX_LEVEL,
                }
            }
            DailyTaskType::CreatePostWithPageType => {
                let types = [
                    (PageType::Polling, 5),
                    (PageType::Video, 2),
                    (PageType::CampfireObject, 2),
                    (PageType::Quote, 1),
                ];
                let dist = WeightedIndex::new(types.map(|(_, chance)| chance)).unwrap();
                DailyTask::CreatePostWithPageType {
                    page_type: types[rng.sample(dist)].0,
                }
            }
            DailyTaskType::AnswerInChat => {
                let amount = rng.sample(Uniform::new_inclusive(3, 10));
                DailyTask::AnswerInChat { amount }
            }
        };

        return Ok(task);
    }
}
