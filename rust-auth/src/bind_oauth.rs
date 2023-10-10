use crate::terminate_session::AccessTokenInfo;
use crate::AuthServer;
use c_core::services::auth::{AuthError, OAuthProvider};
use itertools::Itertools;
use openidconnect::{OAuth2TokenResponse, TokenResponse};
use sqlx::types::chrono::Utc;

impl AuthServer {
    pub(crate) async fn _bind_oauth(
        &self,
        token: String,
        provider: OAuthProvider,
        nonce: String,
        code: String,
    ) -> Result<(), AuthError> {
        let AccessTokenInfo { user_id, .. } = self.get_access_token_info_secure(token).await?;

        let (token_response, id_token) = self.get_token_response(provider, nonce, code).await?;

        let provider_id = id_token.subject().as_str();

        let mut tx = self.base.pool.begin().await?;

        let another_account_exists = sqlx::query_scalar!(
            "select count(*) > 0 from auth_sources
             where provider = $1 and provider_account_id = $2",
            i32::from(provider),
            provider_id,
        )
        .fetch_one(&mut *tx)
        .await?
        .unwrap_or(false);

        if another_account_exists {
            return Err(AuthError::AnotherAccountExists);
        }

        sqlx::query!(
            "insert into auth_sources (
                 user_id, provider, provider_account_id, refresh_token,
                 access_token, expires_at, scope, id_token
             ) values ($1, $2, $3, $4, $5, $6, $7, $8)",
            user_id,
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

        Ok(())
    }
}
