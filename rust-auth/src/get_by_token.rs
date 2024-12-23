use crate::terminate_session::AccessTokenInfo;
use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::user::AuthUser;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _get_by_token(&self, token: String) -> Result<(i64, AuthUser), AuthError> {
        let AccessTokenInfo {
            user_id,
            session_id,
            ..
        } = self.get_access_token_info_secure(token).await?;

        Ok((
            session_id,
            self._get_by_id(user_id)
                .await?
                .ok_or(anyhow!("out of sync"))?,
        ))
    }
}
