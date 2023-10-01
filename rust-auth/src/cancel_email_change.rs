use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};

impl AuthServer {
    pub(crate) async fn _cancel_email_change(&self, token: String) -> Result<(), AuthError> {
        let claims: TokenClaims = jsonwebtoken::decode(
            &token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_cancel_email_change_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)?
        .claims;

        let (user_id, old_email) = claims.sub.split_once(':').ok_or(anyhow!("invalid token"))?;
        let user_id = user_id.parse::<i64>().map_err(anyhow::Error::from)?;

        let mut tx = self.base.pool.begin().await?;

        let current_user = sqlx::query!("select email from users where id = $1", user_id)
            .fetch_optional(&mut *tx)
            .await?;
        let Some(current_user) = current_user else {
            return Err(AuthError::UserNotFound);
        };

        if current_user
            .email
            .map(|email| email == old_email)
            .unwrap_or(false)
        {
            return Err(AuthError::SameEmail);
        }

        sqlx::query!(
            "update users set email = $1, email_verification_sent = null, email_verified = now() \
             where id = $2",
            old_email,
            user_id,
        )
        .execute(&mut *tx)
        .await?;

        sqlx::query!("delete from sessions where user_id = $1", user_id)
            .execute(&mut *tx)
            .await?;

        tx.commit().await?;

        Ok(())
    }
}
