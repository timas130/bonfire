mod consts;
mod daily_task;
mod methods;

use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::level::{
    DailyTaskFandom, DailyTaskInfo, LevelCategory, LevelError, LevelRecountResult, LevelService,
};
use c_core::{host_tcp, ServiceBase};

#[derive(Clone)]
pub struct LevelServer {
    base: ServiceBase,
}
impl LevelServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        Ok(Self { base })
    }

    host_tcp!(level);
}

#[tarpc::server]
impl LevelService for LevelServer {
    async fn recount_level(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<LevelRecountResult, LevelError> {
        self._recount_level(user_id).await
    }

    async fn get_level_cached(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<(u64, LevelCategory), LevelError> {
        self._get_level_cached(user_id).await
    }

    async fn get_daily_task(self, _: Context, user_id: i64) -> Result<DailyTaskInfo, LevelError> {
        self._get_daily_task(user_id).await
    }

    async fn get_daily_task_fandoms(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<Vec<DailyTaskFandom>, LevelError> {
        self._get_daily_task_fandoms(user_id).await
    }

    async fn check_in(self, _: Context, user_id: i64) -> Result<(), LevelError> {
        self._check_in(user_id).await
    }
}
