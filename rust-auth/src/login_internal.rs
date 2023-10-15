use c_core::services::auth::AuthError;
use crate::AuthServer;

impl AuthServer {
    pub(crate) async fn _login_internal(&self, key: String) -> Result<(String, String), AuthError> {
        if key != self.base.config.internal_key {
            return Err(AuthError::AccessDenied);
        }

        let system_id = self._get_meta_users().await?.system_id;

        let (access_token, refresh_token) = self.create_session(
            system_id,
            None,
            None,
            false,
        );

        Ok((access_token, refresh_token))
    }
}
