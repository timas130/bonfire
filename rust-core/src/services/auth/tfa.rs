use crate::services::auth::UserContext;
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

/// A two-factor authentication method
#[derive(Debug, Copy, Clone, Deserialize, Serialize, PartialEq, Eq)]
pub enum TfaType {
    /// The user should input a one-time code from
    /// their authenticator app.
    Totp,
    /// The user should visit a link from the email
    /// sent to their address.
    EmailLink,
}

/// Status of a two-factor authentication flow.
///
/// Returned by [`AuthService::check_tfa_status`]
///
/// [`AuthService::check_tfa_status`]: super::AuthService::check_tfa_status
#[derive(Debug, Deserialize, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum TfaStatus {
    /// The request has not been approved yet.
    Waiting,
    /// Authentication was successful.
    Complete(TfaResult),
}
impl From<TfaStatus> for Option<TfaResult> {
    fn from(val: TfaStatus) -> Option<TfaResult> {
        match val {
            TfaStatus::Waiting => None,
            TfaStatus::Complete(result) => Some(result),
        }
    }
}

/// The result of completing a TFA flow.
#[derive(Debug, Deserialize, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum TfaResult {
    /// The login attempt has been approved and
    /// you can continue logging in
    Login {
        /// Access token used for making requests.
        access_token: String,
        /// Refresh token used for retrieving the access
        /// token after it expires.
        refresh_token: String,
    },
    /// The password has been changed
    PasswordChange,
    /// The OAuth authorisation has been approved and the client
    /// can be redirected
    OAuthRedirect {
        /// Where to redirect the client
        redirect_uri: String,
    },
}

/// The action that initiated the TFA flow
#[derive(Debug, Clone, Deserialize, Serialize, Eq, PartialEq)]
pub enum TfaAction {
    /// Logging in
    Login,
    /// Changing the password
    ///
    /// ## Note
    /// The inner [`String`] is already hashed.
    /// It should just be stored in the database.
    PasswordChange(String),
    /// Authorize an external OAuth flow
    OAuthAuthorize {
        /// Unique ID for the external OAuth flow
        flow_id: i64,
        /// Name of the service that the user is trying to authorise
        service_name: String,
    },
}
impl TfaAction {
    /// Return a [`TfaAction`] with the same type, but without any sensitive data.
    ///
    /// ## Example
    /// ```
    /// # use c_core::services::auth::TfaAction;
    /// let action = TfaAction::PasswordChange("helloworld".to_string());
    /// let stripped = action.strip_data();
    /// assert_eq!(stripped, TfaAction::PasswordChange("".to_string()));
    /// ```
    pub fn strip_data(&self) -> Self {
        match self {
            Self::PasswordChange(_) => Self::PasswordChange(String::new()),
            other => other.clone(),
        }
    }

    // this is a bodge for c-email because of this ructe issue (ructe is not great unfortunately):
    // https://github.com/kaj/ructe/issues/103
    /// If this is a [`TfaAction::OAuthAuthorize`], return its `service_name`
    pub fn service_name(&self) -> Option<&str> {
        match self {
            Self::OAuthAuthorize { service_name, .. } => Some(service_name),
            _ => None,
        }
    }
}

/// Information about a two-factor authentication flow.
#[derive(Debug, Deserialize, Serialize)]
pub struct TfaInfo {
    /// Information about the client that started the flow
    pub context: Option<UserContext>,
    /// When was the flow created
    pub created: DateTime<Utc>,
    /// Action that is performed when the request gets approved
    pub action: TfaAction,
}
