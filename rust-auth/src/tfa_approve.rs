use crate::AuthServer;
use c_core::services::auth::AuthError;

impl AuthServer {
    pub(crate) async fn _tfa_approve(&self, tfa_token: String) -> Result<(), AuthError> {
        let tfa_flow = self.get_tfa_flow_by_token(tfa_token).await?;

        if tfa_flow.completed {
            return Err(AuthError::TfaConfirmed);
        }

        let mut tx = self.base.pool.begin().await?;

        sqlx::query!(
            "update tfa_flows set completed = true where id = $1",
            tfa_flow.id,
        )
        .execute(&mut *tx)
        .await?;

        tfa_flow.do_exactly_that(self).await?;

        tx.commit().await?;

        Ok(())
    }
}
