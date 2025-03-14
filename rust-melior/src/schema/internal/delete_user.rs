use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use crate::utils::permissions::PermissionLevelGuard;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel::System;

#[derive(Default)]
pub struct InternalDeleteUserMutation;

#[Object]
impl InternalDeleteUserMutation {
    /// Terminate user's sessions and delete all their data
    #[graphql(guard = "PermissionLevelGuard::new(System)")]
    async fn internal_delete_user(
        &self,
        ctx: &Context<'_>,
        user_id: ID,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id = user_id.try_into().map_err(|_| RespError::InvalidId)?;

        req.auth.delete_user(context::current(), user_id).await??;

        Ok(OkResp)
    }
}
