use crate::ProfileServer;
use c_core::prelude::chrono::NaiveDate;
use c_core::services::profile::ProfileError;

impl ProfileServer {
    pub(crate) async fn _get_birthday(
        &self,
        user_id: i64,
    ) -> Result<Option<NaiveDate>, ProfileError> {
        let birthday = sqlx::query_scalar!(
            "select birthday from real_user_birthdays where user_id = $1",
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        Ok(birthday)
    }
}
