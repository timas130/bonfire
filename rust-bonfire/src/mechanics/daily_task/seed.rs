use crate::context::GlobalContext;
use chrono::{NaiveDate, NaiveTime, Utc};
use rand::distributions::Standard;
use rand::{Rng, SeedableRng};
use rand_xoshiro::SplitMix64;

pub async fn get_seed_for_day(
    context: &GlobalContext,
    user_id: i64,
    date: &NaiveDate,
) -> Result<[u8; 32], sqlx::Error> {
    // i'm not a fucking cryptography expert
    let mut rng = SplitMix64::seed_from_u64(user_id as u64);
    let user_bytes: [u8; 8] = rng.sample(Standard);
    let mut rng = SplitMix64::seed_from_u64(date.and_time(NaiveTime::MIN).timestamp() as u64);
    let date_bytes: [u8; 8] = rng.sample(Standard);

    let mut tx = context.pool.begin().await?;

    let random_seed: [u8; 16] = rand::random();
    let date = Utc::now().date_naive();
    let today_seed = sqlx::query_scalar!(
        "insert into random_seeds (date, seed)
         values ($1, $2)
         on conflict (date) do update set seed = excluded.seed
         returning seed",
        date,
        &random_seed,
    )
    .fetch_one(&mut *tx)
    .await?;

    tx.commit().await?;

    Ok(user_bytes
        .into_iter()
        .chain(date_bytes)
        .chain(today_seed)
        .collect::<Vec<u8>>()
        .try_into()
        .expect("wrong seed size"))
}
