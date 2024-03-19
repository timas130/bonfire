use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::Utc;
use c_core::prelude::tracing::warn;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthResult, UserContext};
use itertools::Itertools;
use jsonwebtoken::{decode, decode_header, Algorithm, DecodingKey, Validation};
use nanoid::nanoid;
use openidconnect::core::{CoreIdToken, CoreIdTokenClaims, CoreTokenResponse};
use openidconnect::reqwest::async_http_client;
use openidconnect::{AuthorizationCode, Nonce, NonceVerifier, OAuth2TokenResponse, TokenResponse};
use serde::Deserialize;
use sqlx::{Postgres, Transaction};
use std::str::FromStr;

struct DummyNonceVerifier;

impl NonceVerifier for DummyNonceVerifier {
    fn verify(self, _: Option<&Nonce>) -> Result<(), String> {
        Ok(())
    }
}

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

        let jwk = self
            .google_jwks
            .iter()
            .find(|jwk| {
                jwk.common
                    .key_id
                    .as_ref()
                    .map(|x| x == &kid)
                    .unwrap_or(false)
            })
            .ok_or(AuthError::InvalidToken)?;

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

    pub(crate) async fn insert_into_auth_sources(
        &self,
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        provider: OAuthProvider,
        token_response: &Option<CoreTokenResponse>,
        id_token: &CoreIdToken,
        claims: &CoreIdTokenClaims,
    ) -> Result<(), AuthError> {
        let (refresh_token, access_token, expires_in, scopes) = match token_response {
            Some(token_response) => (
                token_response.refresh_token().map(|token| token.secret()),
                Some(token_response.access_token().secret()),
                token_response.expires_in().map(|dur| Utc::now() + dur),
                token_response
                    .scopes()
                    .map(|list| list.iter().map(|scope| scope.as_str()).join(" ")),
            ),
            None => (None, None, None, None),
        };

        let id_token_ser = serde_json::to_value(id_token).map_err(anyhow::Error::from)?;
        sqlx::query!(
            "insert into auth_sources (
                 user_id, provider, provider_account_id, refresh_token,
                 access_token, expires_at, scope, id_token
             ) values ($1, $2, $3, $4, $5, $6, $7, $8)",
            user_id,
            i32::from(provider),
            claims.subject().as_str(),
            refresh_token,
            access_token,
            expires_in,
            scopes,
            id_token_ser.as_str().unwrap(),
        )
        .execute(&mut **tx)
        .await?;

        Ok(())
    }

    pub(crate) async fn register_oauth(
        &self,
        provider: OAuthProvider,
        context: Option<UserContext>,
        token_response: Option<CoreTokenResponse>,
        id_token: CoreIdToken,
        claims: CoreIdTokenClaims,
    ) -> Result<OAuthResult, AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let email = claims.email().map(|email| email.as_str());
        let email_verified = claims.email_verified().unwrap_or(true);

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

        self.insert_into_auth_sources(
            &mut tx,
            new_user_id,
            provider,
            &token_response,
            &id_token,
            &claims,
        )
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
        _nonce: String,
        code: String,
    ) -> Result<(Option<CoreTokenResponse>, CoreIdToken, CoreIdTokenClaims), AuthError> {
        let client = self.get_oauth_client(provider).await?;
        let id_token_verifier = client.id_token_verifier();

        let id_token = CoreIdToken::from_str(&code);
        let (token_response, id_token) = match id_token {
            Ok(id_token) => (None, id_token),
            Err(_) => {
                let token_response = client
                    .exchange_code(AuthorizationCode::new(code))
                    .request_async(async_http_client)
                    .await
                    .map_err(|_| AuthError::InvalidToken)?;
                let id_token = token_response
                    .id_token()
                    .ok_or(AuthError::InvalidToken)?
                    .clone();
                (Some(token_response), id_token)
            }
        };

        //let nonce = sqlx::query_scalar!(
        //    "delete from oauth_flows where nonce = $1 returning nonce",
        //    nonce,
        //)
        //.fetch_one(&self.base.pool)
        //.await?;

        let claims = id_token
            .claims(&id_token_verifier, DummyNonceVerifier)
            .map_err(|err| {
                warn!("error while verifying oauth token: {err:?}");
                AuthError::InvalidToken
            })?
            .clone();

        Ok((token_response, id_token, claims))
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

        let (token_response, id_token, claims) =
            self.get_token_response(provider, nonce, code).await?;

        let provider_id = claims.subject().as_str();

        let user_id = sqlx::query_scalar!(
            "select user_id from auth_sources
             where provider = $1 and provider_account_id = $2",
            i32::from(provider),
            provider_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let Some(user_id) = user_id else {
            let Some(email) = claims.email() else {
                return self
                    .register_oauth(provider, context, token_response, id_token, claims)
                    .await;
            };

            let same_email_account_exists = sqlx::query_scalar!(
                "select count(*) > 0 from users where lower(email) = lower($1)",
                email.as_str()
            )
            .fetch_one(&self.base.pool)
            .await?
            .unwrap_or(false);

            if same_email_account_exists {
                return Ok(OAuthResult::SameEmailDifferentAccount);
            }

            return self
                .register_oauth(provider, context, token_response, id_token, claims)
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
