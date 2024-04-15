use crate::client_tcp;
use crate::services::auth::AuthError;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use anyhow::anyhow;
use chrono::{DateTime, Utc};
use educe::Educe;
use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use thiserror::Error;

/// Error when working with [`NotificationService`]
#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum NotificationError {
    #[error("UpstreamError: Upstream notifications provider call resulted in an error")]
    UpstreamError,

    #[error("NotificationNotFound: This notification does not exist")]
    NotificationNotFound,

    #[error("{0}")]
    Auth(#[from] AuthError),

    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        // rust macros moment
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// Information for sending a notification
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NotificationInput {
    /// User or session that receives the notification
    pub recipients: Vec<NotificationRecipient>,
    /// What the notification is about
    pub payload: NotificationPayload,
    /// Whether the notification should show up in the notification list
    /// and saved to the database
    pub ephemeral: bool,
    /// Whether the notification must only be sent if the user is online
    pub online_only: bool,
}

/// Who the notification is addressed to
#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NotificationRecipient {
    /// Sends the notification to every session of a user
    User(i64),
    /// Sends the notification to a single session
    Session(i64),
}

/// Notification data
#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum NotificationPayload {
    /// Legacy notification infrastructure raw JSON data
    Legacy(Value),
}
impl NotificationPayload {
    /// Get a numeric representation of the payload type
    ///
    /// For [`NotificationPayload::Legacy`], this value corresponds
    /// to the underlying notification's type.
    pub fn get_type(&self) -> Result<i32, NotificationError> {
        Ok(match self {
            NotificationPayload::Legacy(value) => {
                value["J_N_TYPE"].as_i64().ok_or(anyhow!("out of sync"))? as i32
            } // start future notification types with type id 100
              // (max legacy is currently 66)
        })
    }
}

/// A notification as seen by the client
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Notification {
    /// Unique per-user identifier for the notification
    ///
    /// Might be `0` if the notification is ephemeral.
    /// See [`NotificationInput`].
    pub id: i64,
    /// User to whom this notification is addressed
    pub user_id: i64,
    /// Notification payload
    pub payload: NotificationPayload,
    /// When the notification was sent
    pub created_at: DateTime<Utc>,
    /// Whether the user has read this notification
    pub read: bool,
}

/// Kinds of upstream services for sending notifications
#[derive(Debug, Copy, Clone, Serialize, Deserialize, IntoPrimitive, TryFromPrimitive)]
#[repr(i32)]
pub enum NotificationTokenType {
    /// Firebase Cloud Messaging
    Fcm,
}

/// Service for sending and reading notifications
#[tarpc::service]
pub trait NotificationService {
    /// Send a notification using a [`NotificationInput`]
    ///
    /// This returns the notifications created if
    /// multiple [`NotificationRecipient`]s are specified.
    async fn post(input: NotificationInput) -> Result<Vec<Notification>, NotificationError>;

    /// Get all notifications received by a user, new first
    async fn get_notifications(
        user_id: i64,
        before: Option<DateTime<Utc>>,
        type_filter: Option<Vec<i32>>,
    ) -> Result<Vec<Notification>, NotificationError>;

    /// Get a single notification by its ID
    async fn get_by_id(
        user_id: i64,
        notification_id: i64,
    ) -> Result<Notification, NotificationError>;

    /// Mark a notification as read
    async fn read(user_id: i64, notification_id: i64) -> Result<(), NotificationError>;

    /// Mark all notifications sent to a user as read
    async fn read_all(user_id: i64) -> Result<(), NotificationError>;

    /// Get "do not disturb" mode status and `end_time`
    async fn get_do_not_disturb(user_id: i64) -> Result<Option<DateTime<Utc>>, NotificationError>;

    /// Set or reset "do not disturb" mode for a user
    ///
    /// DND mode prevents any notifications from being pushed.
    /// To disable DND, set `end_time` to `None`.
    async fn set_do_not_disturb(
        user_id: i64,
        end_time: Option<DateTime<Utc>>,
    ) -> Result<(), NotificationError>;

    /// Set the notification token and type of a user session
    ///
    /// A session can only have a single notification token.
    async fn set_token(
        session_id: i64,
        token_type: NotificationTokenType,
        token: String,
    ) -> Result<(), NotificationError>;
}

pub struct Notifications;
impl Notifications {
    client_tcp!(NotificationServiceClient);
}
