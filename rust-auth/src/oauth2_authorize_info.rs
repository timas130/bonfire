use crate::terminate_session::AccessTokenInfo;
use crate::AuthServer;
use c_core::prelude::tracing::{error, info};
use c_core::services::auth::jwt::JWT_ISS;
use c_core::services::auth::{AuthError, OAuthAuthorizeInfo};
use nanoid::nanoid;
use reqwest::Url;
use std::collections::{HashMap, HashSet};
use std::str::FromStr;

// also applies to nonce and scope
const MAX_STATE_LEN: usize = 256;

impl AuthServer {
    pub(crate) async fn _oauth2_authorize_info(
        &self,
        mut query: HashMap<String, String>,
        access_token: Option<String>,
    ) -> Result<OAuthAuthorizeInfo, AuthError> {
        let (
            response_type,
            client_id,
            code_challenge,
            code_challenge_method,
            raw_redirect_uri,
            state,
            nonce,
            scope,
        ) = (
            query
                .remove("response_type")
                .ok_or(AuthError::MissingRequiredParameter(
                    "response_type".to_string(),
                ))?,
            query
                .remove("client_id")
                .ok_or(AuthError::MissingRequiredParameter("client_id".to_string()))?,
            query.remove("code_challenge").unwrap_or_default(),
            query.remove("code_challenge_method").unwrap_or_default(),
            query.remove("redirect_uri"),
            query.remove("state"),
            query.remove("nonce"),
            query.remove("scope").unwrap_or_default(),
        );

        // basic checks

        if response_type != "code" {
            return Err(AuthError::UnsupportedResponseType);
        }

        if state.as_ref().map(|s| s.len()).unwrap_or(0) > MAX_STATE_LEN {
            return Err(AuthError::ParameterTooLong("state".to_string()));
        }
        if nonce.as_ref().map(|s| s.len()).unwrap_or(0) > MAX_STATE_LEN {
            return Err(AuthError::ParameterTooLong("nonce".to_string()));
        }
        if scope.len() > MAX_STATE_LEN {
            return Err(AuthError::ParameterTooLong("scope".to_string()));
        }

        // check that the client exists and get it
        let client = self._get_raw_oauth2_client(client_id).await?;

        // check code_challenge

        if client.enforce_code_challenge && code_challenge.is_empty() {
            return Err(AuthError::MissingRequiredParameter(
                "code_challenge".to_string(),
            ));
        }

        if !code_challenge.is_empty() {
            // See https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-12#section-4.1.1-17
            // S256 is mandatory to implement on the server, and mandatory to use on the client
            // if it supports it.
            if code_challenge_method != "S256" {
                return Err(AuthError::UnsupportedCodeChallengeMethod);
            }

            // Check that code_challenge is the correct length.
            // > code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
            // If some genius uses regular base64, that's not my problem.
            // 32 bytes is always 43 bytes in base64url.
            if code_challenge.len() != 43 {
                return Err(AuthError::InvalidCodeChallenge);
            }
        }

        // check redirect_uri

        let redirect_uri_count = sqlx::query_scalar!(
            "select count(*) as count from oauth2_redirect_uris \
             where client_id = $1",
            client.id,
        )
        .fetch_one(&self.base.pool)
        .await?
        .unwrap_or(0);

        if redirect_uri_count <= 0 {
            return Err(AuthError::NoRedirectUrisDefined);
        }

        let redirect_uri = if redirect_uri_count == 1 {
            // https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-12#section-4.1.1-8.8.1
            // redirect_uri is *optional* if only one redirect URI is registered for this client.
            let only_redirect_uri = sqlx::query_scalar!(
                "select exact_uri from oauth2_redirect_uris \
                 where client_id = $1 \
                 limit 1",
                client.id,
            )
            .fetch_one(&self.base.pool)
            .await?;

            if raw_redirect_uri.is_some() && Some(&only_redirect_uri) != raw_redirect_uri.as_ref() {
                return Err(AuthError::InvalidRedirectUri);
            }

            only_redirect_uri
        } else {
            if raw_redirect_uri.is_none() {
                return Err(AuthError::MissingRequiredParameter(
                    "redirect_uri".to_string(),
                ));
            }

            let redirect_uri = sqlx::query_scalar!(
                "select exact_uri from oauth2_redirect_uris \
                 where client_id = $1 and exact_uri = $2",
                client.id,
                raw_redirect_uri,
            )
            .fetch_optional(&self.base.pool)
            .await?;

            redirect_uri.ok_or(AuthError::InvalidRedirectUri)?
        };

        let scopes = scope.split(' ').map(String::from).collect::<Vec<String>>();
        let scopes_set = scopes.iter().collect::<HashSet<_>>();

        if !scopes_set.is_subset(&client.allowed_scopes.iter().collect()) {
            return Err(AuthError::UnauthorizedScope);
        }

        // all looks good, good to try to authorise

        let Some(access_token) = access_token else {
            // if no account was provided, we simply return the authorisation info
            return Ok(OAuthAuthorizeInfo::Prompt {
                flow_id: None,
                client: client.into(),
                scopes,
            });
        };

        let AccessTokenInfo {
            user_id,
            session_id,
            ..
        } = self.get_access_token_info_secure(access_token).await?;

        // if a grant (at most 1 month old) exists, allow authorising without a prompt
        let existing_grant = sqlx::query!(
            "select id, scope from oauth2_grants \
             where user_id = $1 and client_id = $2 and last_used_at > now() - '1 month'::interval",
            user_id,
            client.id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        match existing_grant {
            Some(grant) if scopes_set.is_subset(&grant.scope.iter().collect()) => {
                info!(
                    user_id,
                    client.id, grant.id, "authorised client with existing grant"
                );

                let code = nanoid!(32);

                sqlx::query_scalar!(
                    "insert into oauth2_flows_as \
                     (session_id, client_id, grant_id, redirect_uri, raw_redirect_uri, scopes,\
                      state, nonce, code_challenge, code_challenge_method, code, authorized_at) \
                     values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, now())",
                    session_id,
                    client.id,
                    grant.id,
                    redirect_uri,
                    raw_redirect_uri,
                    scopes.as_slice(),
                    state,
                    nonce,
                    code_challenge,
                    code_challenge_method,
                    code,
                )
                .execute(&self.base.pool)
                .await?;

                Ok(OAuthAuthorizeInfo::AlreadyAuthorized {
                    redirect_uri: self.build_redirect_uri(redirect_uri, code, state)?,
                })
            }
            _ => {
                let flow_id = sqlx::query_scalar!(
                    "insert into oauth2_flows_as \
                     (session_id, client_id, redirect_uri, raw_redirect_uri, scopes, state,\
                      nonce, code_challenge, code_challenge_method) \
                     values ($1, $2, $3, $4, $5, $6, $7, $8, $9) \
                     returning id",
                    session_id,
                    client.id,
                    redirect_uri,
                    raw_redirect_uri,
                    scopes.as_slice(),
                    state,
                    nonce,
                    code_challenge,
                    code_challenge_method,
                )
                .fetch_one(&self.base.pool)
                .await?;

                Ok(OAuthAuthorizeInfo::Prompt {
                    flow_id: Some(flow_id),
                    scopes,
                    client: client.into(),
                })
            }
        }
    }

    pub(crate) fn build_redirect_uri(
        &self,
        base: String,
        code: String,
        state: Option<String>,
    ) -> Result<String, AuthError> {
        let mut uri = Url::from_str(&base).map_err(|err| {
            error!(?err, base, "invalid redirect_uri stored in db");
            AuthError::InvalidRedirectUri
        })?;

        uri.query_pairs_mut()
            .append_pair("code", &code)
            .append_pair("iss", JWT_ISS);

        if let Some(state) = state {
            uri.query_pairs_mut().append_pair("state", state.as_str());
        }

        Ok(uri.to_string())
    }
}
