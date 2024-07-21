use crate::ProfileServer;
use c_core::prelude::chrono::{NaiveDate, NaiveTime, Utc};
use c_core::prelude::tracing::warn;
use c_core::services::profile::ProfileError;

impl ProfileServer {
    pub(crate) async fn _set_birthday(
        &self,
        user_id: i64,
        birthday: NaiveDate,
    ) -> Result<(), ProfileError> {
        let birthday_dt = birthday.and_time(NaiveTime::MIN).and_utc();
        let current_age = Utc::now().years_since(birthday_dt).unwrap_or(0);

        if current_age < 13 {
            warn!(user_id, %birthday, "too young triggered");
            return Err(ProfileError::TooYoung);
        }

        let result = sqlx::query!(
            "insert into real_user_birthdays (birthday, user_id) \
             values ($1, $2)",
            birthday,
            user_id,
        )
        .execute(&self.base.pool)
        .await;

        if let Some(err) = result.err().and_then(|err| err.into_database_error()) {
            if err.is_unique_violation() {
                return Err(ProfileError::BirthdayAlreadySet);
            }
            return Err(sqlx::Error::Database(err).into());
        }

        Ok(())
    }
}
