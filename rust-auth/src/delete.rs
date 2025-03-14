use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _delete_user(&self, user_id: i64) -> Result<(), AuthError> {
        sqlx::query!("delete from oauth2_grants where user_id = $1", user_id)
            .execute(&self.base.pool)
            .await?;

        self._terminate_all_sessions_internal(user_id).await?;
        self._unsafe_delete_user(user_id).await?;

        Ok(())
    }
}
