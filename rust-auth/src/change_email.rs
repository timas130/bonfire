use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use c_core::services::email::types::EmailTemplate;

impl AuthServer {
    pub(crate) fn gen_cancel_email_change_token(
        &self,
        user_id: i64,
        old_email: &str,
    ) -> Result<String, AuthError> {
        let claims = TokenClaims::new_cancel_email_change(user_id, old_email);
        jsonwebtoken::encode(&self.base.jwt_header, &claims, &self.base.jwt_encoding_key)
            .map_err(anyhow::Error::from)
            .map_err(AuthError::from)
    }

    pub(crate) async fn _change_email(
        &self,
        access_token: String,
        new_email: String,
    ) -> Result<(), AuthError> {
        let access_token = self.get_access_token_info_secure(access_token).await?;

        if !Self::is_email_valid(&new_email) {
            return Err(AuthError::InvalidEmail);
        }

        let mut tx = self.base.pool.begin().await?;

        let email_taken =
            sqlx::query_scalar!("select count(*) from users where email = $1", &new_email)
                .fetch_one(&mut *tx)
                .await?
                .unwrap_or(0);

        if email_taken > 0 {
            tx.rollback().await?;
            return Err(AuthError::EmailTaken);
        }

        let current_user = sqlx::query!(
            "select username, email, email_verification_sent, email_verified, anon_id from users \
             where id = $1",
            access_token.user_id,
        )
        .fetch_one(&mut *tx)
        .await?;

        if current_user.email.is_some()
            && current_user.email_verified.is_none()
            && current_user.anon_id.is_none()
        {
            return Err(AuthError::NotVerified);
        }

        self.send_verification_email(new_email.clone(), current_user.username.clone())
            .await?;

        if let Some(email) = current_user.email {
            let cancel_email_change_token =
                self.gen_cancel_email_change_token(access_token.user_id, &email)?;
            let prefix = &self.base.config.urls.cancel_email_change_link;
            let link = format!("{prefix}{cancel_email_change_token}");

            self.email
                .send(
                    context::current(),
                    email,
                    EmailTemplate::CancelEmailChange {
                        username: current_user.username,
                        link,
                        email: new_email.clone(),
                    },
                )
                .await
                .map_err(|_| AuthError::VerificationEmailFail)?
                .map_err(|_| AuthError::VerificationEmailFail)?;
        }

        sqlx::query!(
            "update users set email = $1, email_verification_sent = now(), 
                              email_verified = null \
             where id = $2",
            new_email,
            access_token.user_id,
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(())
    }
}
