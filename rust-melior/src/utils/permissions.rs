use crate::context::ReqContext;
use async_graphql::{Context, Guard};
use c_core::services::auth::user::PermissionLevel;

pub struct PermissionLevelGuard(PermissionLevel);

impl PermissionLevelGuard {
    pub const fn new(level: PermissionLevel) -> Self {
        Self(level)
    }
}

impl Guard for PermissionLevelGuard {
    async fn check(&self, ctx: &Context<'_>) -> async_graphql::Result<()> {
        let passes = ctx
            .data_unchecked::<ReqContext>()
            .user
            .as_ref()
            .map(|user| user.permission_level >= self.0)
            .unwrap_or(false);
        if passes {
            Ok(())
        } else {
            Err("HiDataMiner".into())
        }
    }
}
