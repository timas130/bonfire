use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use crate::utils::permissions::{is_system_caller, PermissionLevelGuard};
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel::System;

#[derive(Default)]
pub struct InternalCheckInMutation;

#[Object]
impl InternalCheckInMutation {
    /// Mark a user as logged in today
    #[graphql(
        visible = "is_system_caller",
        guard = "PermissionLevelGuard::new(System)"
    )]
    async fn internal_check_in(&self, ctx: &Context<'_>, user_id: ID) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id = user_id.try_into().map_err(|_| RespError::InvalidId)?;

        req.level.check_in(context::current(), user_id).await??;

        Ok(OkResp)
    }
}
