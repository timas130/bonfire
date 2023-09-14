use crate::consts::collisions::{COLLISION_FANDOM_SUBSCRIBE, COLLISION_FANDOM_VICEROY};
use crate::consts::fandoms::{FANDOM_ANYTHING_ID, FANDOM_HELLO_ID};
use crate::consts::publication::{PublicationImportance, PublicationType};
use crate::consts::status::Status;
use crate::context::GlobalContext;
use chrono::{Days, NaiveDate, NaiveTime};
use rand::distributions::WeightedIndex;
use rand::Rng;
use rand_xoshiro::Xoshiro256PlusPlus;
use serde::Serialize;

#[derive(Serialize)]
pub struct DTFandom {
    pub id: i64,
    pub multiplier: f64,
}

pub async fn get_possible_dt_fandoms(
    context: &GlobalContext,
    user_id: i64,
    date: &NaiveDate,
) -> Result<Vec<DTFandom>, sqlx::Error> {
    let start_posts = (*date - Days::new(7))
        .and_time(NaiveTime::MIN)
        .timestamp_millis();
    let start_comments = (*date - Days::new(3))
        .and_time(NaiveTime::MIN)
        .timestamp_millis();
    let end = date
        .and_time(NaiveTime::from_hms_opt(23, 59, 59).unwrap())
        .timestamp_millis();

    // craziest query in bonfire yet
    let fandoms = sqlx::query_as!(
        DTFandom,
        "select
             f.id,
             coalesce(
                 ((vc.value_1 is not null and vc.value_1 = $3)::int * 2 + 1) *
                 ((f.karma_cof::float / 100) * (f.karma_cof::float / 100)) *
                 (
                     (select count(*) from units u
                      where u.unit_type = $5 and u.status = $7 and u.creator_id = $3 and
                            u.date_create >= $9 and u.date_create <= $11 and u.fandom_id = f.id) +
                     (select count(*) from units u
                      where u.unit_type = $6 and u.status = $7 and u.creator_id = $3 and
                            u.date_create >= $10 and u.date_create <= $11 and u.fandom_id = f.id) / 2
                     + 1
                 ),
                 0
             ) as \"multiplier!\"
         from fandoms f
         inner join collisions sc on f.id = sc.collision_id
         left join collisions vc on f.id = vc.owner_id
         where
             f.id != any($1) and
             f.status = $7 and
             sc.collision_type = $2 and
             sc.owner_id = $3 and
             sc.value_1 != $4 and
             (vc.collision_type is null or vc.collision_type = $8)
         order by \"multiplier!\" desc
         limit 5",
        &[FANDOM_ANYTHING_ID, FANDOM_HELLO_ID],
        COLLISION_FANDOM_SUBSCRIBE,
        user_id,
        i64::from(PublicationImportance::None),
        i64::from(PublicationType::Post),
        i64::from(PublicationType::Comment),
        i64::from(Status::Public),
        COLLISION_FANDOM_VICEROY,
        start_posts,
        start_comments,
        end,
    ).fetch_all(&context.pool).await?;

    Ok(fandoms.into_iter().filter(|f| f.multiplier > 0.).collect())
}

pub async fn determine_dt_fandom(
    rng: &mut Xoshiro256PlusPlus,
    context: &GlobalContext,
    user_id: i64,
    date: &NaiveDate,
) -> Result<Option<i64>, sqlx::Error> {
    let fandoms = get_possible_dt_fandoms(context, user_id, date).await?;
    if fandoms.is_empty() {
        return Ok(None);
    }

    let dist =
        WeightedIndex::new(fandoms.iter().map(|f| f.multiplier)).expect("invalid multipliers");

    Ok(Some(fandoms[rng.sample(dist)].id))
}
