use crate::AuthServer;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::auth::{AuthError, OAuthClientInfo};

#[allow(unused)]
pub(crate) struct RawOAuthClient {
    pub id: i64,
    pub client_id: String,
    pub client_secret: String,
    pub display_name: String,
    pub privacy_policy_url: String,
    pub tos_url: Option<String>,
    pub official: bool,
    pub allowed_scopes: Vec<String>,
    pub enforce_code_challenge: bool,
    pub created_at: DateTime<Utc>,
}

impl From<RawOAuthClient> for OAuthClientInfo {
    fn from(value: RawOAuthClient) -> Self {
        Self {
            id: value.id,
            display_name: value.display_name,
            privacy_policy_url: value.privacy_policy_url,
            tos_url: value.tos_url,
            official: value.official,
        }
    }
}

impl AuthServer {
    pub(crate) async fn _get_raw_oauth2_client(
        &self,
        client_id: String,
    ) -> Result<RawOAuthClient, AuthError> {
        let client = sqlx::query_as!(
            RawOAuthClient,
            "select * from oauth2_clients where client_id = $1",
            client_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let Some(client) = client else {
            return Err(AuthError::OAuthClientNotFound);
        };

        Ok(client)
    }

    pub(crate) async fn _get_oauth2_client(
        &self,
        client_id: String,
    ) -> Result<OAuthClientInfo, AuthError> {
        self._get_raw_oauth2_client(client_id).await.map(From::from)
    }
}
