use crate::util::has_user_context::HasUserContext;
use crate::util::session::Session as RawSession;
use crate::AuthServer;
use c_core::prelude::chrono::Utc;
use c_core::services::auth::{AuthError, Session};

impl AuthServer {
    /// Get the newest user sessions for a specific user.
    pub(crate) async fn get_user_sessions(
        &self,
        id: i64,
        offset: i64,
    ) -> Result<Vec<RawSession>, AuthError> {
        Ok(sqlx::query_as!(
            RawSession,
            "select * from sessions
             where user_id = $1 and expires > now()
             order by last_refreshed desc
             limit 25 offset $2",
            id,
            offset
        )
        .fetch_all(&self.base.pool)
        .await?)
    }

    pub(crate) async fn _get_sessions(
        &self,
        access_token: String,
        offset: i64,
    ) -> Result<Vec<Session>, AuthError> {
        let access_token = self.get_access_token_info_secure(access_token).await?;

        let sessions = self
            .get_user_sessions(access_token.user_id, offset)
            .await?
            .into_iter()
            .map(|raw| Session {
                id: raw.id,
                active: raw
                    .expires
                    .map(|expires| Utc::now() < expires)
                    .unwrap_or(true),
                last_active: raw.last_refreshed,
                created_at: raw.created_at,
                context: raw.user_context(),
            })
            .collect();

        Ok(sessions)
    }
}
