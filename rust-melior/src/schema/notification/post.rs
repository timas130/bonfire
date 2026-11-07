use crate::context::{ContextExt, ReqContext};
use crate::error::RespError;
use crate::schema::notification::GNotificationPayload;
use crate::utils::ok::OkResp;
use async_graphql::{Context, InputObject, Object, OneofObject};
use c_core::prelude::tarpc::context;
use c_core::prelude::tokio;
use c_core::services::auth::user::PermissionLevel;
use c_core::services::notification::{NotificationInput, NotificationRecipient};
use o2o::o2o;
use std::time::{Duration, Instant};
use tracing::error;

const POST_BASE_DEADLINE: Duration = Duration::from_secs(10);
const POST_DEADLINE_PER_RECIPIENT: Duration = Duration::from_millis(100);

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

        let input: NotificationInput = input.into();
        input.payload.get_type()?;

        let mut rpc_context = context::current();
        rpc_context.deadline = Instant::now()
            + POST_BASE_DEADLINE
            + POST_DEADLINE_PER_RECIPIENT * input.recipients.len() as u32;

        let notification = req.notification.clone();
        tokio::spawn(async move {
            let result = notification.post(rpc_context, input).await;
            if !matches!(result, Ok(Ok(_))) {
                error!(?result, "failed to post a notification");
            }
        });

        Ok(OkResp)
    }
}
