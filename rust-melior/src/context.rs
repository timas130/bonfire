use std::sync::Arc;
use c_core::prelude::anyhow;
use c_core::ServiceBase;
use c_core::services::auth::{Auth, AuthServiceClient};
use c_core::services::level::{Level, LevelServiceClient};

#[derive(Clone)]
pub struct GlobalContext {
    pub base: Arc<ServiceBase>,
    pub auth: Arc<AuthServiceClient>,
    pub level: Arc<LevelServiceClient>,
}
impl GlobalContext {
    pub async fn new(base: ServiceBase) -> anyhow::Result<GlobalContext> {
        Ok(Self {
            base: Arc::new(base),
            auth: Arc::new(Auth::client_tcp(base.config.ports.auth)),
            level: Arc::new(Level::client_tcp(base.config.ports.level)),
        })
    }
}
