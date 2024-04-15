mod methods;
mod sender;

use crate::sender::{NotificationSender, SenderQueue};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::auth::{Auth, AuthServiceClient};
use c_core::services::notification::{
    Notification, NotificationError, NotificationInput, NotificationService, NotificationTokenType,
};
use c_core::{host_tcp, ServiceBase};

#[derive(Clone)]
pub struct NotificationServer {
    base: ServiceBase,
    auth: AuthServiceClient,

    queue: SenderQueue,
}
impl NotificationServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        let auth = Auth::client_tcp(base.config.ports.auth).await?;

        let sender = NotificationSender::new(base.clone()).await?;
        let queue = sender.start();

        Ok(Self { base, auth, queue })
    }

    host_tcp!(notification);
}

#[tarpc::server]
impl NotificationService for NotificationServer {
    async fn post(
        self,
        _: Context,
        input: NotificationInput,
    ) -> Result<Vec<Notification>, NotificationError> {
        self._post(input).await
    }

    async fn get_by_id(
        self,
        _: Context,
        user_id: i64,
        notification_id: i64,
    ) -> Result<Notification, NotificationError> {
        self._get_by_id(user_id, notification_id).await
    }

    async fn get_notifications(
        self,
        _: Context,
        user_id: i64,
        before: Option<DateTime<Utc>>,
        type_filter: Option<Vec<i32>>,
    ) -> Result<Vec<Notification>, NotificationError> {
        self._get_notifications(user_id, before, type_filter).await
    }

    async fn read(
        self,
        _: Context,
        user_id: i64,
        notification_id: i64,
    ) -> Result<(), NotificationError> {
        self._read(user_id, notification_id).await
    }

    async fn read_all(self, _: Context, user_id: i64) -> Result<(), NotificationError> {
        self._read_all(user_id).await
    }

    async fn get_do_not_disturb(
        self,
        _: Context,
        user_id: i64,
    ) -> Result<Option<DateTime<Utc>>, NotificationError> {
        self._get_do_not_disturb(user_id).await
    }

    async fn set_do_not_disturb(
        self,
        _: Context,
        user_id: i64,
        end_time: Option<DateTime<Utc>>,
    ) -> Result<(), NotificationError> {
        self._set_do_not_disturb(user_id, end_time).await
    }

    async fn set_token(
        self,
        _: Context,
        session_id: i64,
        token_type: NotificationTokenType,
        token: String,
    ) -> Result<(), NotificationError> {
        self._set_token(session_id, token_type, token).await
    }
}
