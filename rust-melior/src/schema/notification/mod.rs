mod get;
mod post;
mod read;
mod read_all;
mod set_token;

use crate::error::RespError;
use crate::models::user::User;
use crate::schema::notification::get::NotificationsQuery;
use crate::schema::notification::post::PostNotificationMutation;
use crate::schema::notification::read::NotificationReadMutation;
use crate::schema::notification::read_all::NotificationReadAllMutation;
use crate::schema::notification::set_token::SetNotificationTokenMutation;
use async_graphql::{
    ComplexObject, Context, InputObject, MergedObject, OneofObject, SimpleObject, Union, ID,
};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::services::notification::{Notification, NotificationPayload};
use o2o::o2o;

#[derive(MergedObject, Default)]
pub struct NotificationQuery(NotificationsQuery);

#[derive(MergedObject, Default)]
pub struct NotificationMutation(
    NotificationReadMutation,
    NotificationReadAllMutation,
    PostNotificationMutation,
    SetNotificationTokenMutation,
);

/// A notification
#[derive(SimpleObject, Clone, o2o)]
#[from_owned(Notification)]
#[graphql(name = "Notification", complex)]
struct GNotification {
    #[graphql(skip)]
    #[map(id)]
    pub _id: i64,
    #[graphql(skip)]
    #[map(user_id)]
    pub _user_id: i64,

    /// Notification payload
    #[from(~.into())]
    pub payload: GNotificationPayload,
    /// Time when notification was sent
    pub created_at: DateTime<Utc>,
    /// Whether notification has been read
    pub read: bool,
}
#[ComplexObject]
impl GNotification {
    /// Unique ID for this notification (unique across users)
    async fn id(&self) -> ID {
        self._id.into()
    }

    /// User to whom this notification is directed
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self._user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }
}

/// Different types of notifications
#[derive(Union, OneofObject, Clone)]
#[graphql(name = "NotificationPayload", input_name = "NotificationPayloadInput")]
pub enum GNotificationPayload {
    Legacy(LegacyNotification),
}
impl From<NotificationPayload> for GNotificationPayload {
    fn from(value: NotificationPayload) -> Self {
        match value {
            NotificationPayload::Legacy(content) => Self::Legacy(LegacyNotification { content }),
        }
    }
}
impl From<GNotificationPayload> for NotificationPayload {
    fn from(value: GNotificationPayload) -> Self {
        match value {
            GNotificationPayload::Legacy(content) => Self::Legacy(content.content),
        }
    }
}

/// Legacy JSON notification
#[derive(SimpleObject, InputObject, Clone)]
#[graphql(input_name = "LegacyNotificationInput")]
pub struct LegacyNotification {
    /// Notification JSON
    pub content: serde_json::Value,
}
