use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Enum, Object};
use c_core::prelude::tarpc::context;
use c_core::services::notification::NotificationTokenType;
use o2o::o2o;

#[derive(Default)]
pub struct SetNotificationTokenMutation;

/// Kinds of upstream services for sending notifications
#[derive(Enum, o2o, Eq, PartialEq, Copy, Clone)]
#[into(NotificationTokenType)]
#[graphql(name = "NotificationTokenType")]
enum GNotificationTokenType {
    /// Firebase Cloud Messaging
    Fcm,
}

#[Object]
impl SetNotificationTokenMutation {
    /// Set the notification token and type of the currently authenticated session
    ///
    /// A session can only have a single notification token.
    async fn set_notification_token(
        &self,
        ctx: &Context<'_>,
        token_type: GNotificationTokenType,
        token: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        req.require_user()?;

        let session_id = req.session_id.unwrap();

        req.notification
            .set_token(context::current(), session_id, token_type.into(), token)
            .await??;

        Ok(OkResp)
    }
}
