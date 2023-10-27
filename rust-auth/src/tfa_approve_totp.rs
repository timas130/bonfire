use crate::login_email::TFA_MODE_TOTP;
use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};
use totp_rs::{Secret, TOTP};

impl AuthServer {
    pub(crate) async fn _tfa_approve_totp(
        &self,
        wait_token: String,
        code: String,
    ) -> Result<(), AuthError> {
        let wait_token = jsonwebtoken::decode::<TokenClaims>(
            &wait_token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_tfa_wait_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)?;

        let token_id = wait_token.claims.jti;
        let user_id = wait_token
            .claims
            .sub
            .parse::<i64>()
            .map_err(anyhow::Error::from)?;

        let tfa_flow = self.get_tfa_flow(&token_id, user_id).await?;

        if tfa_flow.completed {
            return Err(AuthError::TfaConfirmed);
        }

        let mut tx = self.base.pool.begin().await?;

        let user = sqlx::query!(
            "select tfa_mode, tfa_data from users where id = $1",
            user_id
        )
        .fetch_one(&mut *tx)
        .await?;

        if user.tfa_mode != Some(TFA_MODE_TOTP) {
            tx.rollback().await?;
            return Err(AuthError::InvalidTfa);
        }

        let Some(tfa_data) = user.tfa_data else {
            tx.rollback().await?;
            return Err(anyhow!("mode set but not tfa data").into());
        };

        let attempts = sqlx::query_scalar!(
            "select count(*) as count
             from totp_attempts
             where user_id = $1 and created_at > now() - '1 minute'::interval",
            user_id,
        )
        .fetch_one(&mut *tx)
        .await?
        .unwrap_or(0);

        if attempts >= 3 {
            tx.rollback().await?;
            return Err(AuthError::TooManyAttempts);
        }

        // outside the transaction!
        sqlx::query!("insert into totp_attempts (user_id) values ($1)", user_id)
            .execute(&self.base.pool)
            .await?;

        let totp_secret = Secret::Encoded(tfa_data);
        let totp = TOTP::new(
            totp_rs::Algorithm::SHA1,
            6,
            5,
            30,
            totp_secret
                .to_bytes()
                .map_err(|_| anyhow!("mode set but invalid tfa data"))?,
        )
        .map_err(|e| anyhow!("mode set but couldn't create totp: {:?}", e))?;

        if !totp.check_current(&code).map_err(anyhow::Error::from)? {
            return Err(AuthError::InvalidTfaCode);
        }

        // clear attempt counter
        sqlx::query!("delete from totp_attempts where user_id = $1", user_id)
            .execute(&mut *tx)
            .await?;

        sqlx::query!(
            "update tfa_flows set completed = true where id = $1",
            tfa_flow.id
        )
        .execute(&mut *tx)
        .await?;

        tfa_flow.do_exactly_that(self).await?;

        tx.commit().await?;

        Ok(())
    }
}
