use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::notification::GNotification;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct NotificationReadMutation;

#[Object]
impl NotificationReadMutation {
    /// Mark a single notification as read
    async fn read_notification(
        &self,
        ctx: &Context<'_>,
        notification_id: ID,
    ) -> Result<GNotification, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let notification_id = notification_id
            .try_into()
            .map_err(|_| RespError::InvalidId)?;

        req.notification
            .read(context::current(), user.id, notification_id)
            .await??;

        req.notification
            .get_by_id(context::current(), user.id, notification_id)
            .await?
            .map_err(From::from)
            .map(From::from)
    }
}
