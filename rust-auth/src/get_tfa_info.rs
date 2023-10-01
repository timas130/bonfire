use crate::check_tfa_status::RawTfaFlow;
use crate::util::has_user_context::HasUserContext;
use crate::util::tfa::TfaActionExt;
use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::tfa::{TfaAction, TfaInfo};
use c_core::services::auth::AuthError;
use jsonwebtoken::{Algorithm, Validation};

impl AuthServer {
    pub(crate) async fn get_tfa_flow_by_token(
        &self,
        tfa_token: String,
    ) -> Result<RawTfaFlow, AuthError> {
        let tfa_token = jsonwebtoken::decode::<TokenClaims>(
            &tfa_token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_tfa_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)?;

        let token_id = tfa_token.claims.jti;
        let user_id = tfa_token
            .claims
            .sub
            .parse::<i64>()
            .map_err(anyhow::Error::from)?;

        self.get_tfa_flow(&token_id, user_id).await
    }

    pub(crate) async fn _get_tfa_info(&self, tfa_token: String) -> Result<TfaInfo, AuthError> {
        let tfa_flow = self.get_tfa_flow_by_token(tfa_token).await?;

        let tfa_info = TfaInfo {
            context: tfa_flow.user_context(),
            created: tfa_flow.created_at,
            action: TfaAction::from_db(tfa_flow.action_type, String::new())
                .map_err(|_| AuthError::from(anyhow::Error::msg("unknown action type")))?,
        };

        Ok(tfa_info)
    }
}
