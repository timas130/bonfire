use crate::SecurityServer;
use c_core::services::security::{IntentionType, SecurityError};

const MAX_INTENTIONS_PER_HOUR: i64 = 3;

impl SecurityServer {
    pub(crate) async fn _create_intention(
        &self,
        user_id: i64,
        intention_type: IntentionType,
    ) -> Result<String, SecurityError> {
        let mut tx = self.base.pool.begin().await?;

        let intentions_this_hour = sqlx::query_scalar!(
            "select count(*) from security_intentions \
             where user_id = $1 and created_at > (now() - '1 hour'::interval)",
            user_id,
        )
        .fetch_one(&mut *tx)
        .await?
        .unwrap_or(0);

        if intentions_this_hour > MAX_INTENTIONS_PER_HOUR {
            return Err(SecurityError::TooManyAttempts);
        }

        let token = nanoid::nanoid!(32);
        sqlx::query!(
            "insert into security_intentions (token, intention_type, user_id) \
             values ($1, $2, $3)",
            &token,
            i32::from(intention_type),
            user_id,
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(token)
    }
}
