use std::collections::HashMap;
use crate::AuthServer;
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::AuthUser;
use c_core::services::auth::{AuthError, AuthService};

impl AuthServer {
    pub(crate) async fn _get_by_name(self, name: String) -> Result<Option<AuthUser>, AuthError> {
        let id: Option<i64> =
            sqlx::query_scalar!("select id from users where username = $1 limit 1", name)
                .fetch_optional(&self.base.pool)
                .await?;
        Ok(match id {
            Some(id) => self.get_by_id(context::current(), id).await?,
            None => None,
        })
    }

    pub(crate) async fn _get_by_names(&self, names: &[String]) -> Result<HashMap<String, AuthUser>, AuthError> {
        let ids = sqlx::query_scalar!("select id from users where username = any($1)", names)
            .fetch_all(&self.base.pool)
            .await?;

        Ok(self
            ._get_by_ids(&ids).await?
            .into_iter()
            .map(|(_, user)| (user.username.clone(), user))
            .collect())
    }
}

// tests for this method are in get_by_id.rs
