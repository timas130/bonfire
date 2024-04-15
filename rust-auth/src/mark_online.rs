use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _mark_online(&self, access_token: String) -> Result<(), AuthError> {
        let token = self.get_access_token_info_secure(access_token).await?;

        sqlx::query!(
            "update sessions set last_online = now() where id = $1",
            token.session_id,
        )
        .execute(&self.base.pool)
        .await?;
        sqlx::query!(
            "update accounts \
             set last_online_time = extract(epoch from now()) * 1000 \
             where id = $1",
            token.user_id,
        )
        .execute(&self.base.pool)
        .await?;

        Ok(())
    }
}
