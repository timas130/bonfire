use crate::util::has_user_context::HasUserContext;
use crate::util::tfa::TfaActionExt;
use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::tfa::{TfaAction, TfaResult, TfaStatus};
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};
use sqlx::types::ipnetwork::IpNetwork;

#[derive(Debug)]
pub(crate) struct RawTfaFlow {
    pub id: String,
    pub user_id: i64,
    pub action_type: i32,
    pub action_data: String,
    pub completed: bool,
    pub expires: DateTime<Utc>,
    pub created_at: DateTime<Utc>,
    pub ip: Option<IpNetwork>,
    pub user_agent: Option<String>,
}

impl AuthServer {
    /// Get the [`RawTfaFlow`] from the database with the corresponding
    /// `token_id` (== `jti`) and `user_id` (== `sub`).
    pub(crate) async fn get_tfa_flow(
        &self,
        token_id: &str,
        user_id: i64,
    ) -> Result<RawTfaFlow, AuthError> {
        let tfa_flow = sqlx::query_as!(
            RawTfaFlow,
            "select * from tfa_flows \
             where id = $1 and user_id = $2 \
             limit 1",
            token_id,
            user_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let tfa_flow = match tfa_flow {
            Some(flow) => flow,
            None => return Err(AuthError::TfaExpired),
        };

        let expires: DateTime<Utc> = tfa_flow.expires;

        // If expired
        if Utc::now() > expires {
            return Err(AuthError::TfaExpired);
        }

        Ok(tfa_flow)
    }

    pub(crate) async fn _check_tfa_status(
        &self,
        tfa_wait_token: String,
    ) -> Result<TfaStatus, AuthError> {
        let tfa_wait_token = jsonwebtoken::decode::<TokenClaims>(
            &tfa_wait_token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_tfa_wait_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)?;

        let token_id = tfa_wait_token.claims.jti;
        let user_id = tfa_wait_token
            .claims
            .sub
            .parse::<i64>()
            .map_err(anyhow::Error::from)?;

        let tfa_flow = self.get_tfa_flow(&token_id, user_id).await?;

        // If still waiting
        if !tfa_flow.completed {
            return Ok(TfaStatus::Waiting);
        }

        // If the flow is finished, ...

        // Recover the TfaAction
        let tfa_action = TfaAction::from_db(tfa_flow.action_type, tfa_flow.action_data.clone())
            .map_err(|_| AuthError::from(anyhow::Error::msg("unknown action type")))?;

        let mut tx = self.base.pool.begin().await?;

        // Delete the flow since it is completed
        sqlx::query!("delete from tfa_flows where id = $1", token_id)
            .execute(&mut *tx)
            .await?;

        match tfa_action {
            TfaAction::Login => {
                let user_context = tfa_flow.user_context();
                let (access_token, refresh_token) = self
                    .create_session(user_id, user_context.as_ref(), None)
                    .await?;

                tx.commit().await?;

                Ok(TfaStatus::Complete(TfaResult::Login {
                    access_token,
                    refresh_token,
                }))
            }
            TfaAction::PasswordChange(_) => {
                tx.commit().await?;
                Ok(TfaStatus::Complete(TfaResult::PasswordChange))
            }
        }
    }
}
