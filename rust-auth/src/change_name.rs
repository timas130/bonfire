use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _change_name(
        &self,
        user_id: i64,
        new_name: String,
        loose: bool,
    ) -> Result<(), AuthError> {
        let username_valid = if loose {
            Self::is_username_valid_loose(&new_name)
        } else {
            Self::is_username_valid(&new_name)
        };
        if !username_valid {
            return Err(AuthError::InvalidUsername);
        }

        let affected_rows = sqlx::query!(
            "update users set username = $1 \
             where id = $2",
            new_name,
            user_id,
        )
        .execute(&self.base.pool)
        .await?
        .rows_affected();

        if affected_rows < 1 {
            return Err(AuthError::UserNotFound);
        }

        Ok(())
    }
}
