use crate::util::session::{Session, SESSION_EXPIRE_DAYS};
use crate::AuthServer;
use c_core::prelude::chrono::Utc;
use c_core::prelude::{anyhow, chrono};
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::{AuthError, UserContext};
use nanoid::nanoid;
use sqlx::types::ipnetwork::IpNetwork;

impl AuthServer {
    pub(crate) async fn _login_refresh(
        &self,
        refresh_token: String,
        context: Option<UserContext>,
    ) -> Result<String, AuthError> {
        let session: Option<Session> = sqlx::query_as!(
            Session,
            "select * from sessions \
             where refresh_token = $1 and expires > now() \
             limit 1",
            refresh_token
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let session = match session {
            Some(session) => session,
            None => return Err(AuthError::TokenExpired),
        };

        let mut tx = self.base.pool.begin().await?;

        sqlx::query!(
            "update sessions \
             set expires = $1, ip = $2, user_agent = $3, last_refreshed = $4 \
             where id = $5",
            Utc::now() + chrono::Duration::days(SESSION_EXPIRE_DAYS),
            context.as_ref().map(|c| IpNetwork::from(c.ip)),
            context.as_ref().map(|c| &c.user_agent),
            Utc::now(),
            session.id,
        )
        .execute(&mut *tx)
        .await?;

        let access_token_id = nanoid!(32);
        let access_token = TokenClaims::new_access(access_token_id, session.id, session.user_id);
        let access_token = jsonwebtoken::encode(
            &self.base.jwt_header,
            &access_token,
            &self.base.jwt_encoding_key,
        )
        .map_err(anyhow::Error::from)?;

        tx.commit().await?;

        Ok(access_token)
    }
}
