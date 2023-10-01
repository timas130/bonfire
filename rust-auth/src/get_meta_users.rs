use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::{AuthError, MetaUsers};

impl AuthServer {
    pub(crate) async fn _get_meta_users(&self) -> Result<MetaUsers, AuthError> {
        let users = sqlx::query!(
            "select \
                (select id from users where username = 'system') as system_id, \
                (select id from users where username = 'deleted') as deleted_id"
        )
        .fetch_one(&self.base.pool)
        .await?;

        Ok(MetaUsers {
            system_id: users.system_id.ok_or(anyhow!("out of sync"))?,
            deleted_id: users.deleted_id.ok_or(anyhow!("out of sync"))?,
        })
    }
}
