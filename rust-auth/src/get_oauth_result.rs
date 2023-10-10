use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::Utc;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthResult, UserContext};
use itertools::Itertools;
use jsonwebtoken::{decode, decode_header, Algorithm, DecodingKey, Validation};
use nanoid::nanoid;
use openidconnect::core::{CoreIdTokenClaims, CoreTokenResponse};
use openidconnect::reqwest::async_http_client;
use openidconnect::{AuthorizationCode, Nonce, OAuth2TokenResponse, TokenResponse};
use serde::Deserialize;

#[derive(Deserialize)]
struct FirebaseToken {
    sub: String,
}

impl AuthServer {
    pub(crate) async fn get_firebase_result(
        &self,
        id_token: String,
        context: Option<&UserContext>,
    ) -> Result<OAuthResult, AuthError> {
        let mut validation = Validation::new(Algorithm::RS256);
        validation.set_issuer(&[format!(
            "https://securetoken.google.com/{}",
            self.base.config.firebase.project_id
        )]);
        validation.set_audience(&[&self.base.config.firebase.project_id]);

        let kid = decode_header(&id_token)
            .map_err(|_| AuthError::InvalidToken)
            .and_then(|hdr| hdr.kid.ok_or(AuthError::InvalidToken))?;

        let jwk = self.google_jwks.get(&kid).ok_or(AuthError::InvalidToken)?;

        let key = DecodingKey::from_jwk(jwk)
            .and_then(|key| decode::<FirebaseToken>(&id_token, &key, &validation))
            .map(|data| data.claims)
            .map_err(|_| AuthError::InvalidToken)?;

        let user_id = sqlx::query_scalar!(
            "select account_id from campfire_db.accounts_firebase where firebase_uid = $1",
            key.sub,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let Some(user_id) = user_id else {
            return Err(AuthError::UserNotFound);
        };

        let (access_token, refresh_token) = self
            .create_session(user_id, context, Some(OAuthProvider::LegacyFirebase), true)
            .await?;

        Ok(OAuthResult::Success {
            access_token,
            refresh_token,
        })
    }

    pub(crate) async fn register_oauth(
        &self,
        provider: OAuthProvider,
        context: Option<UserContext>,
        token_response: CoreTokenResponse,
        id_token: CoreIdTokenClaims,
    ) -> Result<OAuthResult, AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let email = id_token.email().map(|email| email.as_str());
        let email_verified = id_token.email_verified().unwrap_or(true);

        let temp_name = nanoid!(32);
        let new_user_id = sqlx::query_scalar!(
            "insert into users (username, email, email_verification_sent, email_verified)
             values ($1, $2, now(), $3)
             returning id",
            temp_name,
            email,
            if email_verified {
                Some(Utc::now())
            } else {
                None
            },
        )
        .fetch_one(&mut *tx)
        .await?;

        let new_name = format!("User#{new_user_id}");

        sqlx::query!(
            "update users set username = $1 where id = $2",
            &new_name,
            new_user_id
        )
        .execute(&mut *tx)
        .await?;

        if !email_verified {
            let Some(email) = email else {
                return Err(anyhow!("no email and email not verified").into());
            };
            self.send_verification_email(email.to_string(), new_name)
                .await?;
        }

        sqlx::query!(
            "insert into auth_sources (
                 user_id, provider, provider_account_id, refresh_token,
                 access_token, expires_at, scope, id_token
             ) values ($1, $2, $3, $4, $5, $6, $7, $8)",
            new_user_id,
            i32::from(provider),
            id_token.subject().as_str(),
            token_response.refresh_token().map(|token| token.secret()),
            token_response.access_token().secret(),
            token_response.expires_in().map(|dur| Utc::now() + dur),
            token_response
                .scopes()
                .map(|list| list.iter().map(|scope| scope.as_str()).join(" ")),
            token_response.id_token().map(|token| token.to_string())
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        let (access_token, refresh_token) = self
            .create_session(new_user_id, context.as_ref(), Some(provider), true)
            .await?;

        Ok(OAuthResult::Success {
            access_token,
            refresh_token,
        })
    }

    pub(crate) async fn get_token_response(
        &self,
        provider: OAuthProvider,
        nonce: String,
        code: String,
    ) -> Result<(CoreTokenResponse, CoreIdTokenClaims), AuthError> {
        let client = self.get_oauth_client(provider)?;

        let token_response = client
            .exchange_code(AuthorizationCode::new(code))
            .request_async(async_http_client)
            .await
            .map_err(|_| AuthError::InvalidToken)?;

        let nonce = sqlx::query_scalar!(
            "delete from oauth_flows where nonce = $1 returning nonce",
            nonce
        )
        .fetch_one(&self.base.pool)
        .await?;

        let id_token_verifier = client.id_token_verifier();
        let id_token = token_response
            .id_token()
            .ok_or(AuthError::InvalidToken)?
            .claims(&id_token_verifier, &Nonce::new(nonce))
            .map_err(|_| AuthError::InvalidToken)?
            .clone();

        Ok((token_response, id_token))
    }

    pub(crate) async fn _get_oauth_result(
        &self,
        provider: OAuthProvider,
        nonce: String,
        code: String,
        context: Option<UserContext>,
    ) -> Result<OAuthResult, AuthError> {
        if provider == OAuthProvider::LegacyFirebase {
            return self.get_firebase_result(code, context.as_ref()).await;
        }

        let (token_response, id_token) = self.get_token_response(provider, nonce, code).await?;

        let provider_id = id_token.subject().as_str();

        let user_id = sqlx::query_scalar!(
            "select user_id from auth_sources
             where provider = $1 and provider_account_id = $2",
            i32::from(provider),
            provider_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let Some(user_id) = user_id else {
            let Some(email) = id_token.email() else {
                return self
                    .register_oauth(provider, context, token_response, id_token)
                    .await;
            };

            let same_email_account_exists = sqlx::query_scalar!(
                "select count(*) > 0 from users where email = $1",
                email.as_str()
            )
            .fetch_one(&self.base.pool)
            .await?
            .unwrap_or(false);

            if same_email_account_exists {
                return Ok(OAuthResult::SameEmailDifferentAccount);
            }

            return self
                .register_oauth(provider, context, token_response, id_token)
                .await;
        };

        let (access_token, refresh_token) = self
            .create_session(user_id, context.as_ref(), Some(provider), true)
            .await?;

        Ok(OAuthResult::Success {
            access_token,
            refresh_token,
        })
    }
}
