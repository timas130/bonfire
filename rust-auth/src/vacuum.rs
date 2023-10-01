use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _vacuum(&self) -> Result<(), AuthError> {
        sqlx::query!("delete from sessions where expires < now()",)
            .execute(&self.base.pool)
            .await?;
        sqlx::query!("delete from tfa_flows where expires < now()",)
            .execute(&self.base.pool)
            .await?;
        sqlx::query!("delete from recovery_flows where expires < now()",)
            .execute(&self.base.pool)
            .await?;
        sqlx::query!("delete from oauth_flows where expires < now()",)
            .execute(&self.base.pool)
            .await?;
        sqlx::query!("delete from totp_attempts where created_at < now() - '1 hour'::interval")
            .execute(&self.base.pool)
            .await?;

        Ok(())
    }
}
