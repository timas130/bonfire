use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _check_recovery_token(&self, token: String) -> Result<i64, AuthError> {
        let user_id = sqlx::query_scalar!(
            "select user_id from recovery_flows \
             where expires > now() and recovery_flows.id = $1",
            token,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let user_id = match user_id {
            Some(flow) => flow,
            None => return Err(AuthError::RecoveryTokenInvalid),
        };

        Ok(user_id)
    }
}
