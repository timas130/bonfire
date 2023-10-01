mod bind_oauth;
mod cancel_email_change;
mod change_email;
mod change_password;
mod check_recovery_token;
mod check_tfa_status;
mod enable_email_tfa;
mod generate_tfa_secret;
mod get_by_id;
mod get_by_name;
mod get_by_token;
mod get_meta_users;
mod get_oauth_result;
mod get_oauth_url;
mod get_security_settings;
mod get_sessions;
mod get_tfa_info;
mod hard_ban;
mod login_email;
mod login_refresh;
mod recover_password;
mod register_email;
mod resend_verification;
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
use c_core::prelude::*;
use c_core::services::auth::tfa::{TfaInfo, TfaStatus};
use c_core::services::auth::user::{AuthUser, PermissionLevel};
use c_core::services::auth::{
    AuthError, AuthService, LoginEmailOptions, LoginEmailResponse, MetaUsers, OAuthProvider,
    OAuthResult, OAuthUrl, RegisterEmailOptions, SecuritySettings, Session, UserContext,
};
#[cfg(not(test))]
use c_core::services::email::Email;
use c_core::services::email::EmailServiceClient;
use c_core::{host_tcp, ServiceBase};
use jsonwebtoken::jwk::Jwk;
use openidconnect::core::{CoreClient, CoreProviderMetadata};
use openidconnect::reqwest::async_http_client;
use openidconnect::{ClientId, ClientSecret, IssuerUrl};
use serde::Deserialize;
use std::collections::HashMap;
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

    google_client: CoreClient,
    google_jwks: HashMap<String, Jwk>,
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

        let issuer_url = IssuerUrl::new("https://accounts.google.com".to_string())?;
        let google_metadata =
            CoreProviderMetadata::discover_async(issuer_url, async_http_client).await?;
        let google_client = CoreClient::from_provider_metadata(
            google_metadata,
            ClientId::new(base.config.google.client_id.clone()),
            Some(ClientSecret::new(base.config.google.client_secret.clone())),
        );

        #[derive(Deserialize)]
        struct GoogleJwksResponse {
            keys: HashMap<String, Jwk>,
        }

        let google_jwks = reqwest::get(GOOGLE_JWKS_URL)
            .await?
            .json::<GoogleJwksResponse>()
            .await?
            .keys;

        Ok(Self {
            base,
            email,
            google_client,
            google_jwks,
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
        context: Option<UserContext>,
    ) -> Result<OAuthResult, AuthError> {
        self._get_oauth_result(provider, nonce, code, context).await
    }

    async fn bind_oauth(self, _: Context, token: String, provider: OAuthProvider, nonce: String, code: String) -> Result<(), AuthError> {
        self._bind_oauth(token, provider, nonce, code).await
    }

    async fn register_email(
        self,
        _: Context,
        opts: RegisterEmailOptions,
    ) -> Result<i64, AuthError> {
        self._register_email(opts).await
    }

    async fn resend_verification(self, _: Context, address: String) -> Result<(), AuthError> {
        self._resend_verification(address).await
    }

    async fn verify_email(
        self,
        _: Context,
        token: String,
        context: Option<UserContext>,
    ) -> Result<LoginEmailResponse, AuthError> {
        self._verify_email(token, context).await
    }

    async fn login_email(
        self,
        _: Context,
        opts: LoginEmailOptions,
    ) -> Result<LoginEmailResponse, AuthError> {
        self._login_email(opts).await
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
        context: Option<UserContext>,
    ) -> Result<String, AuthError> {
        self._login_refresh(refresh_token, context).await
    }

    async fn send_password_recovery(
        self,
        _: Context,
        email: String,
        context: Option<UserContext>,
    ) -> Result<(), AuthError> {
        self._send_password_recovery(email, context).await
    }

    async fn check_recovery_token(self, _: Context, token: String) -> Result<String, AuthError> {
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
        context: Option<UserContext>,
    ) -> Result<Option<String>, AuthError> {
        self._change_password(access_token, old_password, new_password, context)
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
        Ok(self._get_by_ids(&[id]).await?.remove(&id))
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

    async fn get_by_token(self, _: Context, token: String) -> Result<(i64, AuthUser), AuthError> {
        self._get_by_token(token).await
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

    async fn vacuum(self, _: Context) -> Result<(), AuthError> {
        self._vacuum().await
    }
}
