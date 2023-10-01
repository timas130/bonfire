use crate::login_email::TFA_MODE_EMAIL;
use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _enable_email_tfa(&self, user_id: i64) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let current_mode = sqlx::query_scalar!("select tfa_mode from users where id = $1", user_id)
            .fetch_one(&mut *tx)
            .await?;
        if current_mode.is_some() {
            return Err(AuthError::AlreadyTfa);
        }

        sqlx::query!(
            "update users set tfa_mode = $1 where id = $2",
            TFA_MODE_EMAIL,
            user_id
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(())
    }
}
