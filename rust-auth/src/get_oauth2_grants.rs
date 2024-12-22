use crate::AuthServer;
use c_core::prelude::futures::TryStreamExt;
use c_core::services::auth::{AuthError, OAuthClientInfo, OAuthGrant};

impl AuthServer {
    pub(crate) async fn _get_oauth2_grants(
        &self,
        user_id: i64,
        offset: i64,
        limit: i64,
    ) -> Result<Vec<OAuthGrant>, AuthError> {
        let grants = sqlx::query!(
            "select g.*, \
                    cl.display_name, cl.privacy_policy_url, \
                    cl.tos_url, cl.official \
             from oauth2_grants g \
             inner join oauth2_clients cl on g.client_id = cl.id \
             where g.user_id = $1 \
             order by cl.created_at \
             limit $2 offset $3",
            user_id,
            limit,
            offset,
        )
        .fetch(&self.base.pool)
        .map_ok(|record| OAuthGrant {
            id: record.id,
            user_id: record.user_id,
            client: OAuthClientInfo {
                id: record.client_id,
                display_name: record.display_name,
                privacy_policy_url: record.privacy_policy_url,
                tos_url: record.tos_url,
                official: record.official,
            },
            scope: record.scope,
            created_at: record.created_at,
            last_used_at: record.last_used_at,
        })
        .try_collect::<Vec<_>>()
        .await?;

        Ok(grants)
    }
}
