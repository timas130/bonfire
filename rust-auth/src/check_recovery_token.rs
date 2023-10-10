use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _check_recovery_token(&self, token: String) -> Result<i64, AuthError> {
        let recovery_flow = sqlx::query!(
            "select username from recovery_flows \
             inner join users on recovery_flows.user_id = users.id \
             where expires > now() and recovery_flows.id = $1",
            token,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let recovery_flow = match recovery_flow {
            Some(flow) => flow,
            None => return Err(AuthError::RecoveryTokenInvalid),
        };

        Ok(recovery_flow.username)
    }
}
