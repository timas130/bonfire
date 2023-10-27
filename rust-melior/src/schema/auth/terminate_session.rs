use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct TerminateSessionMutation;

#[Object]
impl TerminateSessionMutation {
    /// Terminate an auth session
    async fn terminate_session(&self, ctx: &Context<'_>, session_id: ID) -> Result<ID, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(access_token) = req.access_token.clone() else {
            return Err(AuthError::Unauthenticated.into());
        };

        let session_id = session_id.try_into().map_err(|_| RespError::InvalidId)?;

        req.auth
            .terminate_session(context::current(), access_token, session_id)
            .await??;

        Ok(session_id.into())
    }
}
