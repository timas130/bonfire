use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthUrl};
use openidconnect::core::{CoreClient, CoreResponseType};
use openidconnect::{AuthenticationFlow, CsrfToken, Nonce, RedirectUrl, Scope};
use std::borrow::Cow;

impl AuthServer {
    pub(crate) fn get_oauth_client(
        &self,
        provider: OAuthProvider,
    ) -> Result<&CoreClient, AuthError> {
        Ok(match provider {
            OAuthProvider::LegacyFirebase => return Err(AuthError::InvalidProvider),
            OAuthProvider::Google => &self.google_client,
        })
    }

    pub(crate) async fn _get_oauth_url(
        &self,
        provider: OAuthProvider,
    ) -> Result<OAuthUrl, AuthError> {
        let client = self.get_oauth_client(provider)?;

        let (auth_url, state, nonce) = client
            .authorize_url(
                AuthenticationFlow::<CoreResponseType>::AuthorizationCode,
                CsrfToken::new_random,
                Nonce::new_random,
            )
            .set_redirect_uri(Cow::Owned(
                RedirectUrl::new(format!(
                    "{}{}",
                    self.base.config.urls.oauth_redirect_link,
                    i32::from(provider)
                ))
                .expect("invalid oauth redirect link configured"),
            ))
            .add_scope(Scope::new("profile".to_string()))
            .add_scope(Scope::new("email".to_string()))
            .url();

        sqlx::query!(
            "insert into oauth_flows (ua_token, csrf_token, nonce, expires)
             values ($1, $2, $3, now() + '1 hour'::interval)",
            "",
            state.secret(),
            nonce.secret(),
        )
        .execute(&self.base.pool)
        .await?;

        Ok(OAuthUrl {
            scope: auth_url
                .query_pairs()
                .find(|(key, _)| key == "scope")
                .ok_or(anyhow!("couldn't find scope in auth url"))?
                .1
                .to_string(),
            url: auth_url.to_string(),
            state: state.secret().to_string(),
            nonce: nonce.secret().to_string(),
        })
    }
}
