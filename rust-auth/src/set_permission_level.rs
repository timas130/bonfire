use crate::AuthServer;
use c_core::services::auth::user::PermissionLevel;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _set_permission_level(
        &self,
        user_id: i64,
        permission_level: PermissionLevel,
    ) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let rows_affected = sqlx::query!(
            "update users set permission_level = $1 where id = $2",
            i32::from(permission_level),
            user_id,
        )
        .execute(&mut *tx)
        .await?
        .rows_affected();

        if rows_affected < 1 {
            return Err(AuthError::UserNotFound);
        }

        tx.commit().await?;

        Ok(())
    }
}
