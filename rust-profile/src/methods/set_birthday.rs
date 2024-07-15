use crate::ProfileServer;
use c_core::prelude::chrono::NaiveDate;
use c_core::services::profile::ProfileError;

impl ProfileServer {
    pub(crate) async fn _set_birthday(
        &self,
        user_id: i64,
        birthday: NaiveDate,
    ) -> Result<(), ProfileError> {
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
