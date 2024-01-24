use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::chrono::{DateTime, Duration, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;
use c_core::services::email::types::EmailTemplate;

impl AuthServer {
    pub(crate) async fn send_verification_email(
        &self,
        email: String,
        username: String,
    ) -> Result<(), AuthError> {
        let verify_link = format!(
            "{prefix}{token}",
            prefix = self.base.config.urls.verify_link,
            token = self.create_verify_token(email.clone())?,
        );
        self.email
            .send(
                context::current(),
                email,
                EmailTemplate::VerifyEmail {
                    username,
                    verify_link,
                },
            )
            .await
            .map_err(|_| AuthError::VerificationEmailFail)?
            .map_err(|_| AuthError::VerificationEmailFail)?;
        Ok(())
    }

    pub(crate) async fn _resend_verification(&self, email: String) -> Result<(), AuthError> {
        let user = sqlx::query!(
            "select username, email_verification_sent, email_verified from users \
             where lower(email) = lower($1)",
            email
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let user = match user {
            Some(user) => user,
            None => {
                return Err(AuthError::AlreadyVerified);
            }
        };

        let email_verification_sent: Option<DateTime<Utc>> = user.email_verification_sent;
        let email_verified: Option<DateTime<Utc>> = user.email_verified;

        if email_verified.is_some() {
            return Err(AuthError::AlreadyVerified);
        }
        if let Some(datetime) = email_verification_sent {
            let time_passed = Utc::now() - datetime;
            if time_passed < Duration::seconds(60) {
                return Err(AuthError::TryAgainLater(
                    (Duration::seconds(60) - time_passed)
                        .num_seconds()
                        .try_into()
                        .map_err(anyhow::Error::from)?,
                ));
            }
        }

        self.send_verification_email(email.clone(), user.username)
            .await?;

        let mut tx = self.base.pool.begin().await?;

        sqlx::query!(
            "update users set email_verification_sent = $1 where email = $2",
            Utc::now(),
            email,
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(())
    }
}
