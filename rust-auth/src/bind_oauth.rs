use c_core::prelude::tracing::info;
use crate::terminate_session::AccessTokenInfo;
use crate::AuthServer;
use c_core::services::auth::{AuthError, OAuthProvider};

impl AuthServer {
    pub(crate) async fn _bind_oauth(
        &self,
        token: String,
        provider: OAuthProvider,
        nonce: String,
        code: String,
    ) -> Result<(), AuthError> {
        let AccessTokenInfo { user_id, .. } = self.get_access_token_info_secure(token).await?;

        let (token_response, id_token, claims) =
            self.get_token_response(provider, nonce, code).await?;

        let provider_id = claims.subject().as_str();

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

        self.insert_into_auth_sources(
            &mut tx,
            user_id,
            provider,
            &token_response,
            &id_token,
            &claims,
        )
        .await?;

        let email = match claims.email() {
            Some(email) if claims.email_verified().unwrap_or(false) => Some(email),
            _ => None,
        };

        if let Some(email) = email {
            let updated = sqlx::query!(
                "update users set email = $1 where id = $2 and (select id from users where email = $1) is null",
                email.as_str(),
                user_id,
            ).execute(&mut *tx).await?.rows_affected();

            if updated > 0 {
                info!("updated user_id={user_id} email={} from authenticating via {provider:?}", email.as_str());
            }
        }

        tx.commit().await?;

        Ok(())
    }
}
