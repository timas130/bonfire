use crate::get_oauth2_client::RawOAuthClient;
use crate::AuthServer;
use base64::prelude::BASE64_URL_SAFE;
use base64::Engine;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono;
use c_core::prelude::chrono::Utc;
use c_core::services::auth::jwt::JWT_ISS;
use c_core::services::auth::AuthError;
use nanoid::nanoid;
use openidconnect::core::{
    CoreIdToken, CoreIdTokenClaims, CoreIdTokenFields, CoreJwsSigningAlgorithm, CoreTokenResponse,
    CoreTokenType,
};
use openidconnect::{
    AccessToken, Audience, AuthorizationCode, ClientId, EmptyAdditionalClaims,
    EmptyExtraTokenFields, EndUserEmail, EndUserName, EndUserUsername, IssuerUrl, Nonce,
    RefreshToken, Scope, StandardClaims, SubjectIdentifier,
};
use scrypt::password_hash::{PasswordHash, PasswordVerifier};
use scrypt::Scrypt;
use sha2::Digest;
use sqlx::{Postgres, Transaction};
use std::collections::HashMap;
use std::time::Duration;

const OAUTH_ACCESS_TOKEN_EXPIRATION: u64 = 3600;

impl AuthServer {
    pub(crate) async fn _get_oauth2_tokens(
        &self,
        mut params: HashMap<String, String>,
        authorization: Option<(String, String)>,
    ) -> Result<serde_json::Value, AuthError> {
        let (client_id, client_secret) = match authorization {
            Some((client_id, client_secret)) => (Some(client_id), Some(client_secret)),
            None => (None, None),
        };

        let (grant_type, client_id, client_secret) = (
            params
                .remove("grant_type")
                .ok_or(AuthError::MissingRequiredParameter(
                    "grant_type".to_string(),
                ))?,
            // the spec is a bit ambiguous on this, (I was surprised that you could even
            // authenticate like this) but imo it's fine if we prioritise client_id/secret from body
            // see https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-12#name-client-secret
            params.remove("client_id").or(client_id),
            params.remove("client_secret").or(client_secret),
        );

        let client_id =
            client_id.ok_or(AuthError::MissingRequiredParameter("client_id".to_string()))?;
        let client_secret = client_secret.ok_or(AuthError::MissingRequiredParameter(
            "client_secret".to_string(),
        ))?;

        let client = self._get_raw_oauth2_client(client_id).await?;

        // check client_secret
        let client_secret_hash = PasswordHash::new(&client.client_secret)
            .map_err(|_| anyhow!("invalid password hash"))?;

        let client_secret_valid = Scrypt
            .verify_password(client_secret.as_bytes(), &client_secret_hash)
            .is_ok();
        if !client_secret_valid {
            return Err(AuthError::InvalidClientSecret);
        }

        match grant_type.as_str() {
            "authorization_code" => self._get_oauth2_token_by_code(params, client).await,
            "refresh_token" => {
                self._get_oauth2_token_by_refresh_token(params, client)
                    .await
            }
            _ => Err(AuthError::UnsupportedResponseType),
        }
    }

    async fn _get_token_response_for_flow(
        &self,
        tx: &mut Transaction<'_, Postgres>,
        flow_id: i64,
        client: &RawOAuthClient,
        code: Option<&AuthorizationCode>,
    ) -> Result<CoreTokenResponse, AuthError> {
        let flow = sqlx::query!(
            "select fl.*, user_id, email, email_verified, username \
             from oauth2_flows_as fl \
             inner join sessions s on s.id = fl.session_id \
             inner join users u on u.id = s.user_id \
             where fl.id = $1 \
             for update",
            flow_id,
        )
        .fetch_one(&mut **tx)
        .await?;

        let access_token = format!("BF/{}/{}", flow.id, nanoid!(64));
        let refresh_token = if flow.scopes.iter().any(|s| s == "offline_access") {
            Some(format!("BF/R/{}/{}", flow.id, nanoid!(64)))
        } else {
            None
        };

        let expiration =
            Utc::now() + chrono::Duration::seconds(OAUTH_ACCESS_TOKEN_EXPIRATION as i64);

        sqlx::query!(
            "update oauth2_flows_as \
             set token_requested_at = now(), \
                 access_token = $2, \
                 refresh_token = $3, \
                 access_token_expires_at = $4 \
             where id = $1",
            flow.id,
            &access_token,
            refresh_token.as_ref(),
            expiration,
        )
        .execute(&mut **tx)
        .await?;

        // sub
        let mut standard_claims =
            StandardClaims::new(SubjectIdentifier::new(flow.user_id.to_string()))
                // name
                .set_name(Some(EndUserName::new(flow.username.clone()).into()))
                // preferred_username
                .set_preferred_username(Some(EndUserUsername::new(flow.username)));

        if flow.scopes.iter().any(|s| s == "email") {
            standard_claims = standard_claims
                // email
                .set_email(flow.email.map(EndUserEmail::new))
                // email_verified
                .set_email_verified(Some(flow.email_verified.is_some()));
        }

        let access_token = AccessToken::new(access_token);
        let id_token = CoreIdToken::new(
            CoreIdTokenClaims::new(
                // iss
                IssuerUrl::new(JWT_ISS.to_string()).expect("invalid JWT_ISS"),
                // aud
                vec![Audience::new(client.client_id.clone())],
                // exp
                expiration,
                // iat
                Utc::now(),
                standard_claims,
                EmptyAdditionalClaims {},
            )
            // azp
            .set_authorized_party(Some(ClientId::new(client.client_id.clone())))
            // nonce
            .set_nonce(flow.nonce.map(Nonce::new)),
            // signing key
            &*self.rs256_signing_key,
            // alg
            CoreJwsSigningAlgorithm::RsaSsaPkcs1V15Sha256,
            // at_hash
            Some(&access_token),
            // c_hash
            code,
        )
        .map_err(|_| anyhow!("failed to issue token"))?;

        let mut resp = CoreTokenResponse::new(
            access_token,
            CoreTokenType::Bearer,
            CoreIdTokenFields::new(Some(id_token), EmptyExtraTokenFields {}),
        );
        resp.set_refresh_token(refresh_token.map(RefreshToken::new));
        resp.set_scopes(Some(flow.scopes.into_iter().map(Scope::new).collect()));
        resp.set_expires_in(Some(&Duration::from_secs(OAUTH_ACCESS_TOKEN_EXPIRATION)));

        Ok(resp)
    }

    async fn _get_oauth2_token_by_code(
        &self,
        mut params: HashMap<String, String>,
        client: RawOAuthClient,
    ) -> Result<serde_json::Value, AuthError> {
        let (code, code_verifier, redirect_uri) = (
            params.remove("code"),
            params.remove("code_verifier"),
            params.remove("redirect_uri"),
        );

        let code = code.ok_or(AuthError::MissingRequiredParameter("code".to_string()))?;

        let mut tx = self.base.pool.begin().await?;

        // get the oauth2 flow
        let flow = sqlx::query!(
            "select id, raw_redirect_uri, code_challenge, code_challenge_method \
             from oauth2_flows_as fl \
             where code = $1 and client_id = $2 and authorized_at is not null \
                   and token_requested_at is null \
             for update",
            code,
            client.id,
        )
        .fetch_optional(&mut *tx)
        .await?;

        let Some(flow) = flow else {
            tx.rollback().await?;
            return Err(AuthError::FlowNotFound);
        };

        // check that code_verifier and redirect_uri are specified if necessary
        if client.enforce_code_challenge && code_verifier.is_none() {
            tx.rollback().await?;
            return Err(AuthError::MissingRequiredParameter(
                "code_verifier".to_string(),
            ));
        }

        if !client.enforce_code_challenge
            && redirect_uri.is_none()
            && flow.raw_redirect_uri.is_some()
        {
            tx.rollback().await?;
            return Err(AuthError::MissingRequiredParameter(
                "redirect_uri".to_string(),
            ));
        }

        // if redirect_uri is specified, check it
        if redirect_uri.is_some() && redirect_uri != flow.raw_redirect_uri {
            tx.rollback().await?;
            return Err(AuthError::InvalidRedirectUri);
        }

        // if code_verifier is specified, check it
        if let Some(code_verifier) = code_verifier {
            if code_verifier.len() > 128 {
                tx.rollback().await?;
                return Err(AuthError::ParameterTooLong("code_verifier".to_string()));
            }

            // just in case, check code_challenge_method.
            // see oauth2_authorize_info() for details on this
            if flow
                .code_challenge_method
                .map(|code| &code == "S256")
                .unwrap_or(false)
            {
                tx.rollback().await?;
                return Err(AuthError::UnsupportedCodeChallengeMethod);
            }

            // code_challenge = base64url(sha256(initial_code_verifier))
            let code_challenge = flow.code_challenge.ok_or(anyhow!("out of sync"))?;
            // code_challenge = sha256(initial_code_verifier)
            let code_challenge = BASE64_URL_SAFE
                .decode(code_challenge.as_bytes())
                .map_err(|_| anyhow!("invalid base64"))?;

            // expected_hash = sha256(code_verifier)
            let expected_hash = sha2::Sha256::digest(code_verifier.as_bytes());

            if code_challenge != expected_hash.as_slice() {
                tx.rollback().await?;
                return Err(AuthError::InvalidRedirectUri);
            }
        }

        let resp = self
            ._get_token_response_for_flow(
                &mut tx,
                flow.id,
                &client,
                Some(&AuthorizationCode::new(code)),
            )
            .await?;

        tx.commit().await?;

        Ok(serde_json::to_value(&resp).map_err(|_| anyhow!("serialization error"))?)
    }

    async fn _get_oauth2_token_by_refresh_token(
        &self,
        mut params: HashMap<String, String>,
        client: RawOAuthClient,
    ) -> Result<serde_json::Value, AuthError> {
        let (refresh_token, scope) = (
            params
                .remove("refresh_token")
                .ok_or(AuthError::MissingRequiredParameter(
                    "refresh_token".to_string(),
                ))?,
            params.remove("scope"),
        );

        if scope.is_some() {
            // yeah, I'm done
            return Err(AuthError::ParameterTooLong("scope".to_string()));
        }

        let mut decoded_token = refresh_token.splitn(4, '/');
        if decoded_token.next() != Some("BF") {
            return Err(AuthError::InvalidToken);
        }
        if decoded_token.next() != Some("R") {
            return Err(AuthError::InvalidToken);
        }

        let flow_id = decoded_token.next().ok_or(AuthError::InvalidToken)?;
        let flow_id = flow_id
            .parse::<i64>()
            .map_err(|_| AuthError::InvalidToken)?;

        let mut tx = self.base.pool.begin().await?;

        let flow = sqlx::query!(
            "select id, code, refresh_token from oauth2_flows_as \
             where id = $1 and client_id = $2 and refresh_token = $3 \
                   and authorized_at is not null \
             for update",
            flow_id,
            client.id,
            &refresh_token,
        )
        .fetch_optional(&mut *tx)
        .await?
        .ok_or(AuthError::InvalidToken)?;

        // the refresh_token changes with this.
        // too bad!
        // (this is a problem because our response failing to reach the client can break offline
        //  access for the service entirely.)

        let resp = self
            ._get_token_response_for_flow(
                &mut tx,
                flow.id,
                &client,
                flow.code.map(AuthorizationCode::new).as_ref(),
            )
            .await?;

        tx.commit().await?;

        Ok(serde_json::to_value(&resp).map_err(|_| anyhow!("serialization error"))?)
    }
}
