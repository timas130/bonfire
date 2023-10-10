use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::level;
use o2o::o2o;

/// Details about a fandom's chance to appear
/// in a [`DailyTask`]
#[derive(SimpleObject, o2o)]
#[from(level::DailyTaskFandom)]
pub struct DailyTaskFandom {
    /// Numeric ID of the fandom
    #[from(id)]
    pub fandom_id: i64,
    /// Relative chance that the fandom may appear
    ///
    /// This goes from `0.0` (exclusive) and up.
    pub multiplier: f64,
}

impl User {
    pub(crate) async fn _daily_task_fandoms(
        &self,
        ctx: &Context<'_>,
    ) -> Result<Vec<DailyTaskFandom>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok(req
            .level
            .get_daily_task_fandoms(context::current(), self._id)
            .await??
            .into_iter()
            .map(From::from)
            .collect())
    }
}
