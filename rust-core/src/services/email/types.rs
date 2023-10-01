use crate::services::auth::tfa::TfaAction;
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::net::IpAddr;
use strum_macros::IntoStaticStr;

#[derive(Clone, Debug, Serialize, Deserialize, IntoStaticStr)]
pub enum EmailTemplate {
    VerifyEmail {
        username: String,
        verify_link: String,
    },
    TfaEmail {
        username: String,
        link: String,
        action: TfaAction,
    },
    LoginAttempt {
        // TODO: Use this
        username: String,
        time: DateTime<Utc>,
        ip: IpAddr,
        user_agent: String,
    },
    PasswordChanged {
        username: String,
    },
    PasswordRecovery {
        username: String,
        link: String,
        ip: IpAddr,
        user_agent: String,
    },
    CancelEmailChange {
        username: String,
        link: String,
        email: String,
    },
}
