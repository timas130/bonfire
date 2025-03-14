use crate::AuthServer;
use c_core::prelude::chrono;
use c_core::prelude::chrono::Utc;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _terminate_all_sessions(
        &self,
        access_token: String,
    ) -> Result<(), AuthError> {
        let access_token = self.get_access_token_info_secure(access_token).await?;

        if access_token.created_at + chrono::Duration::days(7) > Utc::now() {
            return Err(AuthError::SessionTooNew(7));
        }

        let mut tx = self.base.pool.begin().await?;

        sqlx::query_scalar!(
            "delete from sessions where user_id = $1 and id != $2",
            access_token.user_id,
            access_token.session_id,
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(())
    }

    pub(crate) async fn _terminate_all_sessions_internal(
        &self,
        user_id: i64,
    ) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        sqlx::query_scalar!("delete from sessions where user_id = $1", user_id)
            .execute(&mut *tx)
            .await?;

        tx.commit().await?;

        Ok(())
    }
}
