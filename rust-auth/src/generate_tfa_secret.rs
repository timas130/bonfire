use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::AuthError;
use totp_rs::Secret;

impl AuthServer {
    pub(crate) async fn _generate_tfa_secret(&self) -> Result<String, AuthError> {
        let secret = Secret::generate_secret().to_encoded().to_string();
        Ok(jsonwebtoken::encode(
            &self.base.jwt_header,
            &TokenClaims::new_tfa_totp(secret),
            &self.base.jwt_encoding_key,
        )
        .map_err(anyhow::Error::from)?)
    }
}
