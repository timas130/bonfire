use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _unsafe_delete_user(&self, id: i64) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let rows_affected = sqlx::query!("delete from users where id = $1", id)
            .execute(&mut *tx)
            .await?
            .rows_affected();

        tx.commit().await?;

        if rows_affected >= 1 {
            Ok(())
        } else {
            Err(AuthError::UserNotFound)
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::AuthServer;
    use c_core::prelude::tarpc::context;
    use c_core::prelude::tokio;
    use c_core::services::auth::{AuthError, AuthService, RegisterEmailOptions};

    #[tokio::test]
    async fn unsafe_delete() {
        let server1 = AuthServer::load().await.unwrap();

        let server = server1.clone();
        assert_eq!(
            server.unsafe_delete_user(context::current(), 0).await,
            Err(AuthError::UserNotFound),
        );

        let server = server1.clone();
        let user_id = server
            .register_email(
                context::current(),
                RegisterEmailOptions {
                    email: "test_unsafe_delete@bonfire.moe".to_string(),
                    password: "abcABC123!@#".to_string(),
                    username: "test_unsafe_delete1".to_string(),
                    context: None,
                },
            )
            .await
            .unwrap();

        let server = server1.clone();
        assert_eq!(
            server.unsafe_delete_user(context::current(), user_id).await,
            Ok(()),
        );
        let server = server1.clone();
        assert_eq!(
            server.unsafe_delete_user(context::current(), 0).await,
            Err(AuthError::UserNotFound),
        );
    }
}
