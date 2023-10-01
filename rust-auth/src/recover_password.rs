use crate::register_email::PASSWORD_LENGTH;
use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _recover_password(
        &self,
        token: String,
        password: String,
    ) -> Result<(), AuthError> {
        let user_id = sqlx::query_scalar!(
            "select user_id from recovery_flows \
             where expires > now() and id = $1",
            token,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let user_id = match user_id {
            Some(user_id) => user_id,
            None => return Err(AuthError::RecoveryTokenInvalid),
        };

        if !Self::is_password_valid(&password) {
            return Err(AuthError::InvalidPassword(PASSWORD_LENGTH));
        }

        let mut tx = self.base.pool.begin().await?;

        sqlx::query!(
            "delete from recovery_flows \
             where id = $1",
            token,
        )
        .execute(&mut *tx)
        .await?;

        self.do_change_password(user_id, Self::hash_password(password)?)
            .await?;

        tx.commit().await?;

        Ok(())
    }
}
