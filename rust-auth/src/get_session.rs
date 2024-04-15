use crate::util::session::Session as RawSession;
use crate::AuthServer;
use c_core::services::auth::{AuthError, Session};

impl AuthServer {
    pub(crate) async fn _get_session(&self, session_id: i64) -> Result<Session, AuthError> {
        sqlx::query_as!(
            RawSession,
            "select * from sessions where id = $1",
            session_id
        )
        .fetch_optional(&self.base.pool)
        .await?
        .map(From::from)
        .ok_or(AuthError::NoSuchSession)
    }
}
