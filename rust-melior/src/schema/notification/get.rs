use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::notification::GNotification;
use async_graphql::{Context, Object};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct NotificationsQuery;

#[Object]
impl NotificationsQuery {
    /// List notifications received by you, newest first
    ///
    /// Use `before` for pagination, including the last notification's `created_at`.
    async fn notifications(
        &self,
        ctx: &Context<'_>,
        before: Option<DateTime<Utc>>,
        type_filter: Option<Vec<i32>>,
    ) -> Result<Vec<GNotification>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        Ok(req
            .notification
            .get_notifications(context::current(), user.id, before, type_filter)
            .await??
            .into_iter()
            .map(From::from)
            .collect())
    }
}
