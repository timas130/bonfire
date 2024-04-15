use crate::NotificationServer;
use c_core::services::notification::{NotificationError, NotificationTokenType};

impl NotificationServer {
    pub(crate) async fn _set_token(
        &self,
        session_id: i64,
        token_type: NotificationTokenType,
        token: String,
    ) -> Result<(), NotificationError> {
        sqlx::query!(
            "insert into notification_tokens (session_id, service, token) \
             values ($1, $2, $3) \
             on conflict (session_id) do update set \
             service = excluded.service, token = excluded.token",
            session_id,
            i32::from(token_type),
            token,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
