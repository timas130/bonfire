use crate::sender::NotificationSender;
use c_core::prelude::anyhow;
use c_core::prelude::tracing::{error, info, span, warn, Instrument, Level};
use c_core::services::notification::{
    Notification, NotificationError, NotificationPayload, NotificationTokenType,
};
use google_fcm1::api::{Message, SendMessageRequest};
use google_fcm1::Error;
use serde::Deserialize;
use std::collections::HashMap;

#[derive(Deserialize)]
struct FcmErrorResponse {
    error: FcmError,
}
#[derive(Deserialize)]
struct FcmError {
    status: FcmErrorStatus,
}
#[derive(Debug, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
enum FcmErrorStatus {
    InvalidArgument,
    NotFound,
    SenderIdMismatch,
    QuotaExceeded,
    Unavailable,
    ThirdPartyAuthError,
    #[serde(other)]
    Unknown,
    // + UnspecifiedError
    // + Internal
}

impl NotificationSender {
    async fn remove_fcm_token(&self, token: &str) -> Result<(), NotificationError> {
        sqlx::query!(
            "delete from notification_tokens where service = $1 and token = $2",
            i32::from(NotificationTokenType::Fcm),
            token,
        )
        .execute(&self.base.pool)
        .await?;
        Ok(())
    }

    pub(super) async fn send_fcm(
        &self,
        notification: &Notification,
        token: &str,
    ) -> Result<(), NotificationError> {
        // create the message for fcm
        let message = Message {
            token: Some(token.to_owned()),
            data: Some({
                let mut hm = HashMap::new();
                #[allow(irrefutable_let_patterns)] // future-proofing, remove when unnecessary
                if let NotificationPayload::Legacy(data) = &notification.payload {
                    // backwards compat
                    hm.insert(
                        String::from("my_data"),
                        serde_json::to_string(data).map_err(anyhow::Error::from)?,
                    );
                } else {
                    // message is too big if we send both
                    hm.insert(
                        String::from("melior_payload"),
                        serde_json::to_string(&notification.payload)
                            .map_err(anyhow::Error::from)?,
                    );
                }
                hm
            }),
            ..Default::default()
        };

        // we have to specify it despite project_id being in the service account
        let parent = format!(
            "projects/{}",
            self.base
                .config
                .firebase
                .service_account
                .project_id
                .as_ref()
                .expect("project id not specified")
        );

        // sending
        let result = self
            .fcm_hub
            .projects()
            .messages_send(
                SendMessageRequest {
                    message: Some(message),
                    ..Default::default()
                },
                &parent,
            )
            .doit()
            .instrument(span!(Level::INFO, "sending fcm"))
            .await;

        // handing error
        let (_, resp) = match result {
            Ok(resp) => resp,
            Err(raw_err) => {
                let err = match &raw_err {
                    Error::BadRequest(value) => {
                        serde_json::from_value::<FcmErrorResponse>(value.clone())
                            .ok()
                            .map(|resp| resp.error.status)
                    }
                    _ => None,
                };

                return match err {
                    Some(FcmErrorStatus::NotFound) => {
                        self.remove_fcm_token(token).await?;
                        // the caller doesn't have to know
                        Ok(())
                    }
                    Some(FcmErrorStatus::InvalidArgument) => {
                        error!(
                            notification_id = notification.id,
                            user_id = notification.user_id,
                            raw_err = ?raw_err,
                            "fcm invalid argument error",
                        );
                        Ok(())
                    }
                    _ => {
                        warn!(
                            notification_id = notification.id,
                            user_id = notification.user_id,
                            error_status = ?err,
                            raw_err = ?raw_err,
                            "fcm upstream error"
                        );
                        Err(NotificationError::UpstreamError)
                    }
                };
            }
        };

        let message_id = resp.name.unwrap();
        info!(
            notification_id = notification.id,
            user_id = notification.user_id,
            message_id,
            "delivered notification with fcm"
        );

        Ok(())
    }
}
