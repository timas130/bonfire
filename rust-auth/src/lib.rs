mod admin_get_sessions;
mod bind_oauth;
mod cancel_email_change;
mod change_email;
mod change_name;
mod change_password;
mod check_recovery_token;
mod check_tfa_status;
mod delete;
mod enable_email_tfa;
mod generate_tfa_secret;
mod get_by_id;
mod get_by_name;
mod get_by_token;
mod get_jwk_set;
mod get_meta_users;
mod get_oauth2_client;
mod get_oauth2_grants;
mod get_oauth2_token;
mod get_oauth2_userinfo;
mod get_oauth_result;
mod get_oauth_url;
mod get_openid_metadata;
mod get_security_settings;
mod get_session;
mod get_sessions;
mod get_tfa_info;
mod hard_ban;
mod login_email;
mod login_internal;
mod login_refresh;
mod mark_online;
mod oauth2_authorize_accept;
mod oauth2_authorize_info;
mod recover_password;
mod register_email;
mod resend_verification;
mod revoke_oauth2_grant;
mod send_password_recovery;
mod set_permission_level;
mod set_totp_tfa;
mod terminate_all_sessions;
mod terminate_session;
mod tfa_approve;
mod tfa_approve_totp;
mod unsafe_delete;
mod util;
mod vacuum;
mod verify_email;

use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context::Context;
use c_core::prelude::tokio::sync::Mutex;
use c_core::prelude::*;
use c_core::services::auth::tfa::{TfaInfo, TfaStatus};
use c_core::services::auth::user::{AuthUser, PermissionLevel};
use c_core::services::auth::{
    AuthError, AuthService, LoginEmailOptions, LoginEmailResponse, MetaUsers, OAuthAuthorizeInfo,
    OAuthAuthorizeResult, OAuthGrant, OAuthProvider, OAuthResult, OAuthUrl, RegisterEmailOptions,
    RegisterEmailResponse, SecuritySettings, Session, UserContext,
};
#[cfg(not(test))]
use c_core::services::email::Email;
use c_core::services::email::EmailServiceClient;
use c_core::{host_tcp, ServiceBase};
use http_cache_reqwest::{CACacheManager, Cache, CacheMode, HttpCache, HttpCacheOptions};
use jsonwebtoken::jwk::Jwk;
use openidconnect::core::CoreRsaPrivateSigningKey;
use openidconnect::JsonWebKeyId;
use reqwest::Client;
use reqwest_middleware::{ClientBuilder, ClientWithMiddleware};
use serde::Deserialize;
use std::collections::HashMap;
use std::sync::Arc;
#[cfg(test)]
use {
    c_core::prelude::tarpc::client,
    c_core::prelude::tarpc::server::{BaseChannel, Channel},
    c_core::prelude::tarpc::transport::channel,
    c_core::services::email::EmailService,
    c_email::EmailServer,
};

const GOOGLE_JWKS_URL: &str =
    "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

#[derive(Clone)]
pub struct AuthServer {
    base: ServiceBase,
    email: EmailServiceClient,

    reqwest_client: Arc<Mutex<ClientWithMiddleware>>,
    google_jwks: Arc<Vec<Jwk>>,

    rs256_signing_key: Arc<CoreRsaPrivateSigningKey>,
}
impl AuthServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    //noinspection RsUnresolvedReference
    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        #[cfg(not(test))]
        let email = Email::client_tcp(base.config.ports.email).await?;
        #[cfg(test)]
        let email = {
            let (client, server) = channel::unbounded();
            let email = EmailServer::load().await?;
            let server = BaseChannel::with_defaults(server);
            tokio::spawn(server.execute(email.serve()));
            EmailServiceClient::new(client::Config::default(), client).spawn()
        };

        #[derive(Deserialize)]
        struct GoogleJwksResponse {
            keys: Vec<Jwk>,
        }

        let google_jwks = reqwest::get(GOOGLE_JWKS_URL)
            .await?
            .json::<GoogleJwksResponse>()
            .await?
            .keys;

        let reqwest_client = ClientBuilder::new(Client::new())
            .with(Cache(HttpCache {
                mode: CacheMode::Default,
                manager: CACacheManager::default(),
                options: HttpCacheOptions::default(),
            }))
            .build();

        let rs256_signing_key = CoreRsaPrivateSigningKey::from_pem(
            &base.config.auth.rs256_key,
            Some(JsonWebKeyId::new(base.config.auth.rs256_kid.clone())),
        )
        .map_err(|s| anyhow!("{s}"))?;

        Ok(Self {
            base,
            email,
            reqwest_client: Arc::new(Mutex::new(reqwest_client)),
            google_jwks: Arc::new(google_jwks),
            rs256_signing_key: Arc::new(rs256_signing_key),
        })
    }

    host_tcp!(auth);
}

//noinspection RsTraitImplementation
#[tarpc::server]
impl AuthService for AuthServer {
    async fn get_oauth_url(
        self,
        _: Context,
        provider: OAuthProvider,
    ) -> Result<OAuthUrl, AuthError> {
        self._get_oauth_url(provider).await
    }

    async fn get_oauth_result(
        self,
        _: Context,
        provider: OAuthProvider,
        nonce: String,
        code: String,
        user_context: Option<UserContext>,
    ) -> Result<OAuthResult, AuthError> {
        self._get_oauth_result(provider, nonce, code, user_context)
            .await
    }

    async fn bind_oauth(
        self,
        _: Context,
        token: String,
        provider: OAuthProvider,
        nonce: String,
        code: String,
    ) -> Result<(), AuthError> {
        self._bind_oauth(token, provider, nonce, code).await
    }

    async fn register_email(
        self,
        _: Context,
        opts: RegisterEmailOptions,
    ) -> Result<RegisterEmailResponse, AuthError> {
        self._register_email(opts).await
    }

    async fn resend_verification(self, _: Context, address: String) -> Result<(), AuthError> {
        self._resend_verification(address).await
    }

    async fn verify_email(
        self,
        _: Context,
        token: String,
        user_context: Option<UserContext>,
    ) -> Result<i64, AuthError> {
        self._verify_email(token, user_context).await
    }

    async fn login_email(
        self,
        _: Context,
        opts: LoginEmailOptions,
    ) -> Result<LoginEmailResponse, AuthError> {
        self._login_email(opts).await
    }

    async fn login_internal(self, _: Context, key: String) -> Result<(String, String), AuthError> {
        self._login_internal(key).await
    }

    async fn check_tfa_status(self, _: Context, token: String) -> Result<TfaStatus, AuthError> {
        self._check_tfa_status(token).await
    }

    async fn get_tfa_info(self, _: Context, token: String) -> Result<TfaInfo, AuthError> {
        self._get_tfa_info(token).await
    }

    async fn tfa_approve(self, _: Context, token: String) -> Result<(), AuthError> {
        self._tfa_approve(token).await
    }

    async fn tfa_approve_totp(
        self,
        _: Context,
        wait_token: String,
        code: String,
    ) -> Result<(), AuthError> {
        self._tfa_approve_totp(wait_token, code).await
    }

    async fn login_refresh(
        self,
        _: Context,
        refresh_token: String,
        user_context: Option<UserContext>,
    ) -> Result<String, AuthError> {
        self._login_refresh(refresh_token, user_context).await
    }

    async fn mark_online(self, _: Context, access_token: String) -> Result<(), AuthError> {
        self._mark_online(access_token).await
    }

    async fn send_password_recovery(
        self,
        _: Context,
        email: String,
        user_context: Option<UserContext>,
    ) -> Result<(), AuthError> {
        self._send_password_recovery(email, user_context).await
    }

    async fn check_recovery_token(self, _: Context, token: String) -> Result<i64, AuthError> {
        self._check_recovery_token(token).await
    }

    async fn recover_password(
        self,
        _: Context,
        token: String,
        password: String,
    ) -> Result<(), AuthError> {
        self._recover_password(token, password).await
    }

    async fn get_sessions(
        self,
        _: Context,
        access_token: String,
        offset: i64,
    ) -> Result<Vec<Session>, AuthError> {
        self._get_sessions(access_token, offset).await
    }

    async fn terminate_session(
        self,
        _: Context,
        access_token: String,
        id: i64,
    ) -> Result<(), AuthError> {
        self._terminate_session(access_token, id).await
    }

    async fn terminate_all_sessions(
        self,
        _: Context,
        access_token: String,
    ) -> Result<(), AuthError> {
        self._terminate_all_sessions(access_token).await
    }

    async fn change_password(
        self,
        _: Context,
        access_token: String,
        old_password: String,
        new_password: String,
        user_context: Option<UserContext>,
    ) -> Result<Option<String>, AuthError> {
        self._change_password(access_token, old_password, new_password, user_context)
            .await
    }

    async fn change_email(
        self,
        _: Context,
        access_token: String,
        new_email: String,
    ) -> Result<(), AuthError> {
        self._change_email(access_token, new_email).await
    }

    async fn cancel_email_change(self, _: Context, token: String) -> Result<(), AuthError> {
        self._cancel_email_change(token).await
    }

    async fn generate_tfa_secret(self, _: Context) -> Result<String, AuthError> {
        self._generate_tfa_secret().await
    }

    async fn set_totp_tfa(
        self,
        _: Context,
        user_id: i64,
        totp_token: String,
        code: String,
    ) -> Result<(), AuthError> {
        self._set_totp_tfa(user_id, totp_token, code).await
    }

    async fn get_security_settings(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<SecuritySettings, AuthError> {
        self._get_security_settings(user_id).await
    }

    async fn enable_email_tfa(self, _: Context, user_id: i64) -> Result<(), AuthError> {
        self._enable_email_tfa(user_id).await
    }

    async fn get_by_id(self, _: Context, id: i64) -> Result<Option<AuthUser>, AuthError> {
        self._get_by_id(id).await
    }

    async fn get_by_ids(
        self,
        _: Context,
        ids: Vec<i64>,
    ) -> Result<HashMap<i64, AuthUser>, AuthError> {
        self._get_by_ids(&ids).await
    }

    async fn get_by_name(self, _: Context, name: String) -> Result<Option<AuthUser>, AuthError> {
        self._get_by_name(name).await
    }

    async fn get_by_names(
        self,
        _: Context,
        names: Vec<String>,
    ) -> Result<HashMap<String, AuthUser>, AuthError> {
        self._get_by_names(&names).await
    }

    async fn get_by_token(self, _: Context, token: String) -> Result<(i64, AuthUser), AuthError> {
        self._get_by_token(token).await
    }

    async fn get_openid_metadata(self, _: Context) -> Result<serde_json::Value, AuthError> {
        self._get_openid_metadata().await
    }

    async fn get_jwk_set(self, _: Context) -> Result<serde_json::Value, AuthError> {
        self._get_jwk_set().await
    }

    async fn oauth2_authorize_info(
        self,
        _: Context,
        query: HashMap<String, String>,
        access_token: Option<String>,
    ) -> Result<OAuthAuthorizeInfo, AuthError> {
        self._oauth2_authorize_info(query, access_token).await
    }

    async fn oauth2_authorize_accept(
        self,
        _: Context,
        flow_id: i64,
        access_token: String,
        user_context: Option<UserContext>,
    ) -> Result<OAuthAuthorizeResult, AuthError> {
        self._oauth2_authorize_accept(flow_id, access_token, user_context)
            .await
    }

    async fn get_oauth2_token(
        self,
        _: Context,
        params: HashMap<String, String>,
        authorization: Option<(String, String)>,
    ) -> Result<serde_json::Value, AuthError> {
        self._get_oauth2_tokens(params, authorization).await
    }

    async fn get_oauth2_userinfo(
        self,
        _: Context,
        access_token: String,
    ) -> Result<serde_json::Value, AuthError> {
        self._get_oauth2_userinfo(access_token).await
    }

    async fn get_oauth2_grants(
        self,
        _: Context,
        user_id: i64,
        offset: i64,
        limit: i64,
    ) -> Result<Vec<OAuthGrant>, AuthError> {
        self._get_oauth2_grants(user_id, offset, limit).await
    }

    async fn revoke_oauth2_grant(
        self,
        _: Context,
        user_id: i64,
        grant_id: i64,
    ) -> Result<(), AuthError> {
        self._revoke_oauth2_grant(user_id, grant_id).await
    }

    async fn admin_get_sessions(
        self,
        _: Context,
        user_id: i64,
        offset: i64,
    ) -> Result<Vec<Session>, AuthError> {
        self._admin_get_sessions(user_id, offset).await
    }

    async fn get_session(self, _: Context, session_id: i64) -> Result<Session, AuthError> {
        self._get_session(session_id).await
    }

    async fn unsafe_delete_user(self, _: Context, id: i64) -> Result<(), AuthError> {
        self._unsafe_delete_user(id).await
    }

    async fn hard_ban(self, _: Context, user_id: i64, banned: bool) -> Result<(), AuthError> {
        self._hard_ban(user_id, banned).await
    }

    async fn set_permission_level(
        self,
        _: Context,
        user_id: i64,
        permission_level: PermissionLevel,
    ) -> Result<(), AuthError> {
        self._set_permission_level(user_id, permission_level).await
    }

    async fn get_meta_users(self, _: Context) -> Result<MetaUsers, AuthError> {
        self._get_meta_users().await
    }

    async fn change_name(
        self,
        _: Context,
        user_id: i64,
        new_name: String,
        loose: bool,
    ) -> Result<(), AuthError> {
        self._change_name(user_id, new_name, loose).await
    }

    async fn delete_user(self, _: Context, user_id: i64) -> Result<(), AuthError> {
        self._delete_user(user_id).await
    }

    async fn vacuum(self, _: Context) -> Result<(), AuthError> {
        self._vacuum().await
    }
}
