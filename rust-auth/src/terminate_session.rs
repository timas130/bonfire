use crate::AuthServer;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::{anyhow, chrono};
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};

pub(crate) struct AccessTokenInfo {
    pub user_id: i64,
    pub session_id: i64,
    pub created_at: DateTime<Utc>,
}

impl AuthServer {
    /// Decode an `access_token` if the session for that access token exists
    pub(crate) async fn get_access_token_info_secure(
        &self,
        access_token: String,
    ) -> Result<AccessTokenInfo, AuthError> {
        let access_token = jsonwebtoken::decode::<TokenClaims>(
            &access_token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_access_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(|_| AuthError::InvalidToken)?;

        let user_id = access_token
            .claims
            .sub
            .parse::<i64>()
            .map_err(anyhow::Error::from)?;
        let session_id = access_token
            .claims
            .jti
            .split(':')
            .next()
            .ok_or(anyhow::Error::msg("Invalid token"))?
            .parse::<i64>()
            .map_err(anyhow::Error::from)?;

        let session = sqlx::query!(
            "select s.created_at, u.hard_banned, u.email_verified, u.anon_id from sessions s \
             inner join users u on s.user_id = u.id \
             where s.id = $1 and s.user_id = $2",
            session_id,
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let session = match session {
            Some(session) => session,
            None => return Err(anyhow::Error::msg("no such token").into()),
        };

        if session.email_verified.is_none() && session.anon_id.is_none() {
            return Err(AuthError::NotVerified);
        }
        if session.hard_banned {
            return Err(AuthError::HardBanned);
        }

        Ok(AccessTokenInfo {
            user_id,
            session_id,
            created_at: session.created_at,
        })
    }

    pub(crate) async fn _terminate_session(
        &self,
        access_token: String,
        delete_session_id: i64,
    ) -> Result<(), AuthError> {
        let access_token = self.get_access_token_info_secure(access_token).await?;

        // The session must be at least 7 days old
        // to terminate other sessions.
        if access_token.session_id != delete_session_id
            && access_token.created_at + chrono::Duration::days(7) > Utc::now()
        {
            return Err(AuthError::SessionTooNew(7));
        }

        let mut tx = self.base.pool.begin().await?;

        let rows_affected = sqlx::query_scalar!(
            "delete from sessions where id = $1 and user_id = $2",
            delete_session_id,
            access_token.user_id,
        )
        .execute(&mut *tx)
        .await?
        .rows_affected();

        tx.commit().await?;

        if rows_affected > 0 {
            Ok(())
        } else {
            Err(AuthError::NoSuchSession)
        }
    }
}
