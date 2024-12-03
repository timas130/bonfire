use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::services::auth::AuthError;
use openidconnect::core::CoreJsonWebKeySet;
use openidconnect::PrivateSigningKey;

impl AuthServer {
    pub(crate) async fn _get_jwk_set(&self) -> Result<serde_json::Value, AuthError> {
        let jwks = CoreJsonWebKeySet::new(vec![self.rs256_signing_key.as_verification_key()]);

        Ok(serde_json::to_value(jwks).map_err(anyhow::Error::from)?)
    }
}
