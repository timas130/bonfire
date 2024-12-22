use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::services::auth::jwt::JWT_ISS;
use c_core::services::auth::AuthError;
use openidconnect::core::{
    CoreClaimName, CoreJwsSigningAlgorithm, CoreProviderMetadata, CoreResponseType,
    CoreSubjectIdentifierType,
};
use openidconnect::{
    AuthUrl, EmptyAdditionalProviderMetadata, IssuerUrl, JsonWebKeySetUrl, ResponseTypes, Scope,
    TokenUrl, UserInfoUrl,
};

impl AuthServer {
    pub(crate) async fn _get_openid_metadata(&self) -> Result<serde_json::Value, AuthError> {
        let root = &self.base.config.auth.openid_api_root;
        let provider_metadata = CoreProviderMetadata::new(
            IssuerUrl::new(JWT_ISS.to_string()).map_err(anyhow::Error::from)?,
            AuthUrl::new(format!("{root}/authorize")).map_err(anyhow::Error::from)?,
            JsonWebKeySetUrl::new(format!("{root}/jwks")).map_err(anyhow::Error::from)?,
            vec![ResponseTypes::new(vec![CoreResponseType::Code])],
            vec![CoreSubjectIdentifierType::Public],
            vec![CoreJwsSigningAlgorithm::RsaSsaPkcs1V15Sha256],
            EmptyAdditionalProviderMetadata {},
        )
        .set_token_endpoint(Some(
            TokenUrl::new(format!("{root}/token")).map_err(anyhow::Error::from)?,
        ))
        .set_userinfo_endpoint(Some(
            UserInfoUrl::new(format!("{root}/userinfo")).map_err(anyhow::Error::from)?,
        ))
        .set_scopes_supported(Some(vec![
            Scope::new("openid".to_string()),
            Scope::new("email".to_string()),
            Scope::new("profile".to_string()),
            Scope::new("offline_access".to_string()),
        ]))
        .set_claims_supported(Some(vec![
            CoreClaimName::new("sub".to_string()),
            CoreClaimName::new("aud".to_string()),
            CoreClaimName::new("email".to_string()),
            CoreClaimName::new("email_verified".to_string()),
            CoreClaimName::new("exp".to_string()),
            CoreClaimName::new("iat".to_string()),
            CoreClaimName::new("iss".to_string()),
            CoreClaimName::new("name".to_string()),
            CoreClaimName::new("preferred_username".to_string()),
        ]));

        Ok(serde_json::to_value(provider_metadata).map_err(anyhow::Error::from)?)
    }
}
