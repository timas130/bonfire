use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _hard_ban(&self, user_id: i64, banned: bool) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let rows_affected = sqlx::query!(
            "update users set hard_banned = $1 where id = $2",
            banned,
            user_id,
        )
        .execute(&mut *tx)
        .await?
        .rows_affected();

        if rows_affected < 1 {
            tx.rollback().await?;
            return Err(AuthError::UserNotFound);
        }

        let user_id = user_id.to_string();
        sqlx::query!("select pg_notify('user_hard_ban', $1)", user_id)
            .execute(&mut *tx)
            .await?;

        tx.commit().await?;

        Ok(())
    }
}
