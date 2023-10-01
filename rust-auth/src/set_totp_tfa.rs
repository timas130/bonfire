use crate::login_email::TFA_MODE_TOTP;
use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};
use totp_rs::{Secret, TOTP};

impl AuthServer {
    pub(crate) async fn _set_totp_tfa(
        &self,
        user_id: i64,
        totp_token: String,
        code: String,
    ) -> Result<(), AuthError> {
        let totp_token = jsonwebtoken::decode::<TokenClaims>(
            &totp_token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_tfa_totp_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)?;
        let totp_secret = totp_token.claims.jti;

        let mut tx = self.base.pool.begin().await?;

        let user = sqlx::query!(
            "select tfa_mode, tfa_data from users where id = $1",
            user_id
        )
        .fetch_one(&mut *tx)
        .await?;

        if user.tfa_mode == Some(TFA_MODE_TOTP) {
            tx.rollback().await?;
            return Err(AuthError::AlreadyTfa);
        }

        sqlx::query!(
            "update users set tfa_mode = $1, tfa_data = $2 where id = $3",
            TFA_MODE_TOTP,
            &totp_secret,
            user_id,
        )
        .execute(&mut *tx)
        .await?;

        let totp = TOTP::new(
            totp_rs::Algorithm::SHA1,
            6,
            5,
            30,
            Secret::Encoded(totp_secret)
                .to_bytes()
                .map_err(|_| anyhow!("invalid totp secret in token"))?,
        )
        .map_err(|_| anyhow!("invalid totp secret in token 2"))?;

        if !totp.check_current(&code).map_err(anyhow::Error::from)? {
            tx.rollback().await?;
            return Err(AuthError::InvalidTfaCode);
        }

        tx.commit().await?;

        Ok(())
    }
}
