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
}

// tests for this method are in get_by_id.rs
