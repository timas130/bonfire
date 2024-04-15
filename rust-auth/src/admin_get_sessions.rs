use crate::AuthServer;
use c_core::services::auth::{AuthError, Session};

impl AuthServer {
    pub(crate) async fn _admin_get_sessions(
        &self,
        user_id: i64,
        offset: i64,
    ) -> Result<Vec<Session>, AuthError> {
        Ok(self
            .get_user_sessions(user_id, offset)
            .await?
            .into_iter()
            .map(From::from)
            .collect())
    }
}
