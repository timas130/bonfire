use crate::AuthServer;
use c_core::prelude::futures::TryStreamExt;
use c_core::services::auth::user::{AuthUser, PermissionLevel, TfaMode};
use c_core::services::auth::AuthError;
use sqlx::query;
use std::collections::HashMap;

impl AuthServer {
    pub(crate) async fn _get_by_id(&self, user_id: i64) -> Result<Option<AuthUser>, AuthError> {
        Ok(self._get_by_ids(&[user_id]).await?.remove(&user_id))
    }

    pub(crate) async fn _get_by_ids(
        &self,
        user_ids: &[i64],
    ) -> Result<HashMap<i64, AuthUser>, AuthError> {
        let users = query!(
            "select id, username, email, email_verified, permission_level, \
             created_at, modified_at, tfa_mode, hard_banned, anon_id from users \
             where id = any($1)",
            user_ids
        )
        .fetch(&self.base.pool);

        Ok(users
            .map_ok(|user| {
                (
                    user.id,
                    AuthUser {
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        email_verified: user.email_verified,
                        permission_level: PermissionLevel::try_from(user.permission_level)
                            .unwrap_or(PermissionLevel::User),
                        created_at: user.created_at,
                        modified_at: user.modified_at,
                        tfa_mode: user.tfa_mode.and_then(|num| TfaMode::try_from(num).ok()),
                        hard_banned: user.hard_banned,
                        anon: user.anon_id.is_some(),
                    },
                )
            })
            .try_collect()
            .await?)
    }
}

#[cfg(test)]
mod tests {
    use crate::AuthServer;
    use c_core::prelude::tarpc::context;
    use c_core::prelude::tokio;
    use c_core::services::auth::user::{AuthUser, PermissionLevel};
    use c_core::services::auth::{AuthService, RegisterEmailOptions};

    #[tokio::test]
    async fn get_by_id_and_name() {
        let server1 = AuthServer::load().await.unwrap();

        let server = server1.clone();
        assert_eq!(server.get_by_id(context::current(), 0).await.unwrap(), None);

        let server = server1.clone();
        let username = "NonexistingUser".to_string();
        assert_eq!(
            server
                .get_by_name(context::current(), username)
                .await
                .unwrap(),
            None
        );

        let username = "test_get_by_id1".to_string();
        let server = server1.clone();
        let user_id = server
            .register_email(
                context::current(),
                RegisterEmailOptions {
                    email: "test_get_by_id@bonfire.moe".to_string(),
                    password: "abcABC123!@#".to_string(),
                    username: username.to_owned(),
                    context: None,
                },
            )
            .await
            .unwrap();

        let server = server1.clone();
        let user1 = server
            .get_by_id(context::current(), user_id)
            .await
            .unwrap()
            .unwrap();
        let server = server1.clone();
        let user2 = server
            .get_by_name(context::current(), username)
            .await
            .unwrap()
            .unwrap();

        let expected_user = AuthUser {
            id: user_id,
            username: "test_get_by_id1".to_string(),
            email: Some("test_get_by_id@bonfire.moe".to_string()),
            email_verified: None,
            permission_level: PermissionLevel::User,
            created_at: user1.created_at,
            modified_at: user1.modified_at,
            tfa_mode: None,
            hard_banned: false,
        };
        assert_eq!(user1, expected_user);
        assert_eq!(user2, expected_user);

        // Cleanup
        let server = server1.clone();
        server
            .unsafe_delete_user(context::current(), user_id)
            .await
            .unwrap();
    }
}
