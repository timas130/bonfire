use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _revoke_oauth2_grant(
        &self,
        user_id: i64,
        grant_id: i64,
    ) -> Result<(), AuthError> {
        let rows_affected = sqlx::query!(
            "delete from oauth2_grants where user_id = $1 and id = $2",
            user_id,
            grant_id,
        )
        .execute(&self.base.pool)
        .await?
        .rows_affected();

        if rows_affected == 0 {
            return Err(AuthError::GrantNotFound);
        }

        Ok(())
    }
}
