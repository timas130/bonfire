use crate::context::{ContextExt, ReqContext};
use crate::error::RespError;
use crate::schema::notification::GNotificationPayload;
use crate::utils::ok::OkResp;
use async_graphql::{Context, InputObject, Object, OneofObject};
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel;
use c_core::services::notification::{NotificationInput, NotificationRecipient};
use o2o::o2o;

#[derive(Default)]
pub struct PostNotificationMutation;

#[derive(InputObject, o2o)]
#[owned_into(NotificationInput)]
#[graphql(name = "NotificationInput")]
struct GNotificationInput {
    #[into(~.into_iter().map(From::from).collect())]
    recipients: Vec<GNotificationRecipient>,
    #[into(~.into())]
    payload: GNotificationPayload,
    ephemeral: bool,
    online_only: bool,
}

#[derive(OneofObject)]
#[graphql(name = "NotificationRecipient")]
enum GNotificationRecipient {
    User(UserRecipient),
    Session(SessionRecipient),
}
impl From<GNotificationRecipient> for NotificationRecipient {
    fn from(value: GNotificationRecipient) -> Self {
        match value {
            GNotificationRecipient::User(x) => Self::User(x.user_id),
            GNotificationRecipient::Session(x) => Self::Session(x.session_id),
        }
    }
}
#[derive(InputObject)]
struct UserRecipient {
    user_id: i64,
}
#[derive(InputObject)]
struct SessionRecipient {
    session_id: i64,
}

#[Object]
impl PostNotificationMutation {
    /// Send a notification to a list of recipients
    async fn post_notification(
        &self,
        ctx: &Context<'_>,
        input: GNotificationInput,
    ) -> Result<OkResp, RespError> {
        ctx.require_permission_level(PermissionLevel::System)?;
        let req = ctx.data_unchecked::<ReqContext>();

        req.notification
            .post(context::current(), input.into())
            .await??;

        Ok(OkResp)
    }
}
