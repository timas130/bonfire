use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tracing::warn;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthUrl};
use openidconnect::core::{CoreClient, CoreProviderMetadata, CoreResponseType};
use openidconnect::{
    AsyncHttpClient, AuthenticationFlow, ClientId, ClientSecret, CsrfToken, EndpointMaybeSet,
    EndpointNotSet, EndpointSet, HttpRequest, HttpResponse, IssuerUrl, Nonce, RedirectUrl, Scope,
};
use std::borrow::Cow;
use std::future::Future;
use std::pin::Pin;

type Client = CoreClient<
    EndpointSet,
    EndpointNotSet,
    EndpointNotSet,
    EndpointNotSet,
    EndpointMaybeSet,
    EndpointMaybeSet,
>;

impl AsyncHttpClient<'_> for &AuthServer {
    type Error = reqwest_middleware::Error;
    type Future = Pin<Box<dyn Future<Output = Result<HttpResponse, Self::Error>> + Send + 'static>>;

    fn call(&self, request: HttpRequest) -> Self::Future {
        let reqwest_client = self.reqwest_client.clone();
        Box::pin(async move {
            let client = reqwest_client.lock().await;

            let (parts, body) = request.into_parts();

            let mut req = client
                .request(parts.method, parts.uri.to_string())
                .body(body);
            for (name, value) in &parts.headers {
                req = req.header(name.as_str(), value.as_bytes());
            }
            let req = req.build().map_err(reqwest_middleware::Error::Reqwest)?;

            let response = client.execute(req).await?;
            let status = response.status();
            let headers = response.headers().to_owned();
            let mut ret = HttpResponse::new(response.bytes().await?.to_vec());
            *ret.status_mut() = status;
            *ret.headers_mut() = headers;

            Ok(ret)
        })
    }
}

impl AuthServer {
    async fn make_oauth_client(&self, provider: OAuthProvider) -> Result<Client, AuthError> {
        if provider == OAuthProvider::LegacyFirebase {
            return Err(AuthError::InvalidProvider);
        }

        let issuer_url = IssuerUrl::new("https://accounts.google.com".to_string())
            .expect("invalid constant issuer url");
        let google_metadata = CoreProviderMetadata::discover_async(issuer_url, &self)
            .await
            .map_err(|err| {
                warn!("failed to discover oauth client: {err}");
                AuthError::InvalidProvider
            })?;
        let google_client = CoreClient::from_provider_metadata(
            google_metadata,
            ClientId::new(self.base.config.google.client_id.clone()),
            Some(ClientSecret::new(
                self.base.config.google.client_secret.clone(),
            )),
        );

        Ok(google_client)
    }

    pub(crate) async fn get_oauth_client(
        &self,
        provider: OAuthProvider,
    ) -> Result<Client, AuthError> {
        Ok(match provider {
            OAuthProvider::LegacyFirebase => return Err(AuthError::InvalidProvider),
            provider => self.make_oauth_client(provider).await?,
        })
    }

    pub(crate) fn get_oauth_redirect_url(&self, provider: OAuthProvider) -> RedirectUrl {
        RedirectUrl::new(format!(
            "{}{}",
            self.base.config.urls.oauth_redirect_link, provider
        ))
        .expect("invalid oauth redirect link configured")
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
            .set_redirect_uri(Cow::Owned(self.get_oauth_redirect_url(provider)))
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
