use crate::ProfileServer;
use c_core::prelude::chrono::{NaiveTime, Utc};
use c_core::services::profile::ProfileError;

impl ProfileServer {
    pub(crate) async fn _is_age_at_least(
        &self,
        user_id: i64,
        age: u32,
    ) -> Result<Option<bool>, ProfileError> {
        let birthday = self._get_birthday(user_id).await?;
        let Some(birthday) = birthday else {
            return Ok(None);
        };

        let birthday = birthday.and_time(NaiveTime::MIN).and_utc();
        let current_age = Utc::now().years_since(birthday).unwrap_or(0);

        Ok(Some(current_age >= age))
    }
}
