use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct OAuth2RevokeGrantMutation;

#[Object]
impl OAuth2RevokeGrantMutation {
    /// Revoke a previously authorised OAuth client
    async fn oauth2_revoke_grant(&self, ctx: &Context<'_>, grant_id: ID) -> Result<ID, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let grant_id = grant_id.try_into().map_err(|_| RespError::InvalidId)?;

        let user = req.require_user()?;

        req.auth
            .revoke_oauth2_grant(context::current(), user.id, grant_id)
            .await??;

        Ok(grant_id.into())
    }
}
