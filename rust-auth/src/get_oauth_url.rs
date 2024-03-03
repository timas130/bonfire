use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthUrl};
use openidconnect::core::{CoreClient, CoreProviderMetadata, CoreResponseType};
use openidconnect::{AuthenticationFlow, ClientId, ClientSecret, CsrfToken, HttpRequest, HttpResponse, IssuerUrl, Nonce, RedirectUrl, Scope};
use std::borrow::Cow;
use openidconnect::reqwest::Error;
use c_core::prelude::tracing::warn;

impl AuthServer {
    async fn cached_http_client(&self, request: HttpRequest) -> Result<HttpResponse, Error<reqwest_middleware::Error>> {
        let client = self.reqwest_client.lock().await;
        
        let mut req = client
            .request(request.method, request.url.as_str())
            .body(request.body);
        for (name, value) in &request.headers {
            req = req.header(name.as_str(), value.as_bytes());
        }
        let req = req.build().map_err(From::from).map_err(Error::Reqwest)?;
        
        let response = client.execute(req).await.map_err(Error::Reqwest)?;
        
        Ok(HttpResponse {
            status_code: response.status(),
            headers: response.headers().to_owned(),
            body: response.bytes().await.map_err(From::from).map_err(Error::Reqwest)?.to_vec(),
        })
    }
    
    async fn make_oauth_client(&self, provider: OAuthProvider) -> Result<CoreClient, AuthError> {
        if provider == OAuthProvider::LegacyFirebase {
            return Err(AuthError::InvalidProvider);
        }

        let issuer_url = IssuerUrl::new("https://accounts.google.com".to_string())
            .expect("invalid constant issuer url");
        let google_metadata =
            CoreProviderMetadata::discover_async(issuer_url, |req| self.cached_http_client(req)).await
                .map_err(|err| {
                    warn!("failed to discover oauth client: {err}");
                    AuthError::InvalidProvider
                })?;
        let google_client = CoreClient::from_provider_metadata(
            google_metadata,
            ClientId::new(self.base.config.google.client_id.clone()),
            Some(ClientSecret::new(self.base.config.google.client_secret.clone())),
        );

        Ok(google_client)
    }

    pub(crate) async fn get_oauth_client(
        &self,
        provider: OAuthProvider,
    ) -> Result<CoreClient, AuthError> {
        Ok(match provider {
            OAuthProvider::LegacyFirebase => return Err(AuthError::InvalidProvider),
            provider => self.make_oauth_client(provider).await?,
        })
    }

    pub(crate) async fn _get_oauth_url(
        &self,
        provider: OAuthProvider,
    ) -> Result<OAuthUrl, AuthError> {
        let client = self.get_oauth_client(provider).await?;

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
