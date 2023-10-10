use crate::AuthServer;
use c_core::services::auth::{AuthError, UserContext};

impl AuthServer {
    pub(crate) async fn _verify_email(
        &self,
        token: String,
        user_context: Option<UserContext>,
    ) -> Result<i64, AuthError> {
        let email = self.get_verify_token_email(token)?;

        let user = sqlx::query!(
            "select id, email_verification_sent, email_verified from users \
             where email = $1",
            email
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let user = match user {
            Some(user) => user,
            None => {
                return Err(AuthError::UserNotFound);
            }
        };

        if user.email_verified.is_some() {
            return Err(AuthError::AlreadyVerified);
        }

        let mut tx = self.base.pool.begin().await?;

        sqlx::query!(
            "update users set email_verified = now(), email_verification_sent = null, \
                              anon_id = null \
             where id = $1",
            user.id
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(user.id)
    }
}
