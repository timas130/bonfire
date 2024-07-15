//! Account security service

use crate::client_tcp;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use educe::Educe;
use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde::{Deserialize, Serialize};
use thiserror::Error;

#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum SecurityError {
    #[error("TooManyAttempts: You tried doing this too many times, create a new intention later")]
    TooManyAttempts,
    #[error("IntentionNotFound: This intention doesn't exist (or is older than stone)")]
    IntentionNotFound,
    #[error("IntentionExpired: This intention is too old, try again")]
    IntentionExpired,
    #[error("IntentionPassed: This intention has already been passed")]
    IntentionPassed,

    #[error("HashMismatch: Request hash doesn't match intention token")]
    HashMismatch,
    #[error("UnknownPackage: This package name is not allowed")]
    UnknownPackage,
    #[error("UpstreamError: Upstream provider returned an error")]
    UpstreamError,

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

/// Reason for starting an integrity/bot check
#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize, IntoPrimitive, TryFromPrimitive)]
#[repr(i32)]
pub enum IntentionType {
    /// Just for the sake of it
    Generic = 1,
}

/// The account security service
#[tarpc::service]
pub trait SecurityService {
    /// Indicate that the user intends to do an integrity/bot check for
    /// whatever reason.
    ///
    /// An intention token is returned, which should be used as the
    /// request hash (or equivalent)
    async fn create_intention(
        user_id: i64,
        intention_type: IntentionType,
    ) -> Result<String, SecurityError>;

    /// Consume an intention with a Play Integrity verdict
    ///
    /// The request hash must contain the intention token
    async fn save_play_integrity(
        user_id: i64,
        intention_token: String,
        package_name: String,
        token: String,
    ) -> Result<(), SecurityError>;
}

pub struct Security;
impl Security {
    client_tcp!(SecurityServiceClient);
}
