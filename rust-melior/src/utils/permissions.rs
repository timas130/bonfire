use crate::context::ContextExt;
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
        if ctx.has_permission_level(self.0) {
            Ok(())
        } else {
            Err("HiDataMiner".into())
        }
    }
}
