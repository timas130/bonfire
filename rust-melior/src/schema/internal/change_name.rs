use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::utils::permissions::{is_system_caller, PermissionLevelGuard};
use async_graphql::{Context, Object, ID};
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel::System;

#[derive(Default)]
pub struct InternalChangeNameMutation;

#[Object]
impl InternalChangeNameMutation {
    /// Change the name of a user
    #[graphql(
        visible = "is_system_caller",
        guard = "PermissionLevelGuard::new(System)"
    )]
    async fn internal_change_name(
        &self,
        ctx: &Context<'_>,
        user_id: ID,
        new_name: String,
    ) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user_id = user_id.try_into().map_err(|_| RespError::InvalidId)?;

        req.auth
            .change_name(context::current(), user_id, new_name)
            .await??;

        Ok(User::by_id(ctx, user_id)
            .await?
            .ok_or(anyhow!("out of sync"))?)
    }
}
