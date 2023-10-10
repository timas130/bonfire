//! The authentication service module
//!
//! This service handles all duties related to authenticating users.
//! This does not include permissions, profiles, etc.
//!
//! ## TFA flow
//!
//! `c-auth` supports sending emails (TOTP planned) to confirm
//! certain actions.
//!
//! The overview of the TFA flow is as follows:
//!
//! 1. Something initiates a TFA flow.
//! 2. `tfa_token` is sent to the user's email address.
//! 3. `tfa_wait_token` is sent to the client that started the request.
//! 4. The client polls for changes in the flow with [`AuthService::check_tfa_status`].
//! 5. Someone clicks on the link from the confirmation email.
//! 6. The client calls [`AuthService::get_tfa_info`] and displays some information to the user.
//! 7. After getting the user's approval, the client calls [`AuthService::tfa_approve`]
//! 8. The [`AuthService::check_tfa_status`] resolves to [`TfaStatus::Complete`].

pub mod jwt;
pub mod tfa;
pub mod user;

use crate::client_tcp;
pub use crate::services::auth::tfa::{TfaAction, TfaInfo, TfaResult, TfaStatus, TfaType};
use crate::services::auth::user::PermissionLevel;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use chrono::{DateTime, Utc};
use educe::Educe;
use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::net::IpAddr;
use std::ops::RangeInclusive;
use thiserror::Error;
use user::AuthUser;

/// An error related to authentication
#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum AuthError {
    // register_email() & login_email() & send_password_recovery()
    #[error("InvalidEmail: Invalid email address")]
    InvalidEmail,

    // register_email() & login_email()
    #[error("InvalidPassword: Password is too short (<{}) or too long (>{})", .0.start(), .0.end())]
    InvalidPassword(RangeInclusive<usize>),

    // register_email()
    #[error("InvalidUsername: Invalid username (only numbers, underscores and Latin letters are allowed)")]
    InvalidUsername,
    #[error("EmailTaken: The email is already taken by another account")]
    EmailTaken,
    #[error("UsernameTaken: This username is already taken by another account")]
    UsernameTaken,

    // login_email() & change_password()
    #[error("WrongPasswordOrEmail: Wrong password or email")]
    WrongPasswordOrEmail,

    // change_password() & login_email()
    #[error("UserNoEmail: This user doesn't have an email")]
    UserNoEmail,

    // login_email() & get_by_token()
    #[error("HardBanned: This account is hard-banned")]
    HardBanned,
    #[error("NotVerified: This account is not verified")]
    NotVerified,

    // resend_verification() & register_email() & change_email()
    #[error("VerificationEmailFail: Failed to send verification email")]
    VerificationEmailFail,

    // resend_verification() & verify_email()
    #[error("AlreadyVerified: This account is already verified or does not exist")]
    AlreadyVerified,

    // resend_verification()
    #[error("TryAgainLater: Try again in {0} seconds")]
    TryAgainLater(u64),

    // get_tfa_info() & check_tfa_status() & tfa_approve()
    #[error("TfaExpired: This link has expired or is completely wrong")]
    TfaExpired,

    // tfa_approve()
    #[error("TfaConfirmed: The action has already been confirmed")]
    TfaConfirmed,

    // tfa_approve_totp()
    #[error("InvalidTfa: You can't use TOTP for this action")]
    InvalidTfa,

    #[error("TooManyAttempts: Too many attempts, try again later")]
    TooManyAttempts,

    // tfa_approve_totp() & set_totp_tfa()
    #[error("InvalidTfaCode: This code is incorrect, try again")]
    InvalidTfaCode,

    // set_totp_tfa()
    #[error("AlreadyTfa: TOTP is already enabled for this account")]
    AlreadyTfa,

    // login_refresh() & clearance_confirm()
    #[error("TokenExpired: The refresh token expired or never existed in the first place")]
    TokenExpired,

    // terminate_session()
    #[error("NoSuchSession: There is no session with this ID")]
    NoSuchSession,
    #[error("SameSession: You can't terminate your own session (use `logout()`)")]
    SameSession,
    #[error("SessionTooNew: Your session must be at least {0} days old to do this")]
    SessionTooNew(u8),

    // cancel_email_change()
    #[error("SameEmail: You have already reset your email")]
    SameEmail,

    // check_recovery_token()
    #[error("RecoveryTokenInvalid: This recovery token is invalid")]
    RecoveryTokenInvalid,

    #[error("InvalidProvider: You can't use this provider here")]
    InvalidProvider,

    #[error("InvalidToken: This token looks kinda sus")]
    InvalidToken,

    #[error("AnotherAccountExists: Another user is already using this account")]
    AnotherAccountExists,

    // general
    #[error("UserNotFound: This user was not found")]
    UserNotFound,

    #[error("Unauthenticated: You are not authenticated")]
    Unauthenticated,

    #[error("AccessDenied: You don't have sufficient permissions to do this")]
    AccessDenied,

    #[error("LimitReached: You have reached the limit")]
    LimitReached,

    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        // rust macros moment
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), PartialEq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), PartialEq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// Information about the user's client.
///
/// This information is used to identify similar sessions
/// to allow for faster logins. It might also be visible
/// to the user for security purposes.
///
/// Usually, this struct is used as `Option<UserContext>`.
/// Remember: you must only use `None` when an internal
/// tool is the initiator of the request.
#[derive(Debug, Clone, Deserialize, Serialize)]
pub struct UserContext {
    /// User's IP address.
    pub ip: IpAddr,
    /// The *user agent* used.
    ///
    /// For browsers, the value is equal to the `User-Agent`
    /// header.
    ///
    /// Mobile clients might provide their own
    /// user agents in the form of `[Client Name]/[Version] [OS]/[Version]`.
    pub user_agent: String,
}

/// Options for [`AuthService::register_email`]
#[derive(Debug, Deserialize, Serialize)]
pub struct RegisterEmailOptions {
    /// The email address to use for logging in
    pub email: String,
    /// The password to use for logging in
    pub password: String,
    /// The username to show to other users
    pub username: Option<String>,
    /// Information about the user registering.
    ///
    /// Only use `None` when the initiator is an
    /// internal service.
    pub context: Option<UserContext>,
}

/// The result of [`AuthService::login_email`]
#[derive(Debug, Deserialize, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum LoginEmailResponse {
    /// The login succeeded, the user may continue.
    Success {
        /// The access token
        access_token: String,
        /// The refresh token
        refresh_token: String,
    },
    /// A two-factor authentication flow has been created
    /// and the user must complete it to finish logging in.
    TfaRequired {
        /// Method of TFA that must be used.
        tfa_type: TfaType,
        /// Token usable in `check_tfa_status` to query login status.
        tfa_wait_token: String,
    },
}

/// Options for [`AuthService::login_email`]
#[derive(Debug, Deserialize, Serialize)]
pub struct LoginEmailOptions {
    /// The email used for logging in
    pub email: String,
    /// The password used for logging in
    pub password: String,
    /// Information about the user registering.
    ///
    /// Only use `None` when the initiator is an
    /// internal service.
    pub context: Option<UserContext>,
}

/// A login session
#[derive(Debug, Deserialize, Serialize)]
pub struct Session {
    /// ID of the session
    ///
    /// It is also specified in [`jwt::TokenClaims`] as the
    /// first part of `jti`.
    pub id: i64,
    /// Is the session still active (not expired)
    pub active: bool,
    /// Last login_refresh() call timestamp
    pub last_active: DateTime<Utc>,
    /// The first time the session has been created
    pub created_at: DateTime<Utc>,
    /// Details about the device and location of the session owner
    pub context: Option<UserContext>,
}

/// The IDs of default users, like `@system` and `@deleted`.
#[derive(Debug, Deserialize, Serialize)]
pub struct MetaUsers {
    /// The ID of `@system`.
    ///
    /// This user should be used for actions that
    /// require a trusted party to check or for actions
    /// that were performed automatically.
    pub system_id: i64,
    /// The ID of `@deleted`.
    ///
    /// This user should not be used by services.
    /// Its purpose is to replace values after
    /// deletion in the database.
    pub deleted_id: i64,
}

/// Misc security settings
#[derive(Debug, Deserialize, Serialize)]
pub struct SecuritySettings {
    /// The 2FA type enabled for the user
    pub tfa_type: Option<TfaType>,
}

/// An OAuth auth provider
#[derive(
    Copy, Clone, Debug, Deserialize, Serialize, Eq, PartialEq, IntoPrimitive, TryFromPrimitive,
)]
#[repr(i32)]
pub enum OAuthProvider {
    /// Old Firebase authentication method. Not actually OAuth
    LegacyFirebase = 1,
    /// `accounts.google.com`
    Google = 2,
}

/// Client information to log in with an OAuth provider
#[derive(Debug, Deserialize, Serialize)]
pub struct OAuthUrl {
    /// Full URL for the browser
    pub url: String,
    /// Requested scopes
    pub scope: String,
    /// Unique CSRF token from the URL
    pub state: String,
    /// Unique nonce from the URL
    pub nonce: String,
}

/// The result of an OAuth flow
#[derive(Debug, Deserialize, Serialize)]
pub enum OAuthResult {
    /// An account with this email exists, but uses another
    /// authentication provider
    SameEmailDifferentAccount,
    /// Authentication succeeded
    Success {
        /// The access token
        access_token: String,
        /// The refresh token
        refresh_token: String,
    },
}

/// Result of registering via email
#[derive(Debug, Deserialize, Serialize)]
pub struct RegisterEmailResponse {
    /// Newly registered user ID
    pub user_id: i64,
    /// The session access token
    pub access_token: String,
    /// The session refresh token
    pub refresh_token: String,
}

/// The authentication service interface
///
/// This service handles all operations related to authenticating
/// users. It is not responsible for giving users any privileges,
/// for managing profiles, etc.
#[tarpc::service]
pub trait AuthService {
    //// OAuth

    /// Get the necessary information to log in with an OAuth provider.
    async fn get_oauth_url(provider: OAuthProvider) -> Result<OAuthUrl, AuthError>;

    /// Complete authentication with an OAuth provider.
    async fn get_oauth_result(
        provider: OAuthProvider,
        nonce: String,
        code: String,
        user_context: Option<UserContext>,
    ) -> Result<OAuthResult, AuthError>;

    /// Link an OAuth account to a user.
    async fn bind_oauth(
        token: String,
        provider: OAuthProvider,
        nonce: String,
        code: String,
    ) -> Result<(), AuthError>;

    //// Registering

    /// Register an account via email.
    async fn register_email(opts: RegisterEmailOptions)
        -> Result<RegisterEmailResponse, AuthError>;

    /// Resend the verification email after registering.
    async fn resend_verification(address: String) -> Result<(), AuthError>;

    /// Consume the `token` received from the verification email and
    /// verify the account.
    async fn verify_email(
        token: String,
        user_context: Option<UserContext>,
    ) -> Result<i64, AuthError>;

    //// Logging in

    /// Login via email.
    async fn login_email(opts: LoginEmailOptions) -> Result<LoginEmailResponse, AuthError>;

    //// TFA Actions

    /// Query the login status with the `tfa_wait_token` received
    /// from other methods.
    async fn check_tfa_status(token: String) -> Result<TfaStatus, AuthError>;

    /// Query login details with the `tfa_token` from the email.
    async fn get_tfa_info(token: String) -> Result<TfaInfo, AuthError>;

    /// Approve login request with the `tfa_token` from the email.
    ///
    /// The result of [`AuthService::check_tfa_status`] changes
    /// to be [`TfaStatus::Complete`]. The `token` also becomes
    /// no longer valid.
    async fn tfa_approve(token: String) -> Result<(), AuthError>;

    /// Approve login request with a TOTP code.
    async fn tfa_approve_totp(wait_token: String, code: String) -> Result<(), AuthError>;

    //// Refreshing

    /// Receive a fresh access token using the refresh token.
    ///
    /// Only use `None` when the initiator is an
    /// internal service.
    async fn login_refresh(
        refresh_token: String,
        user_context: Option<UserContext>,
    ) -> Result<String, AuthError>;

    //// Password recovery

    /// Send a link to `email` with a password recovery link.
    ///
    /// This method doesn't fail when a user with the email
    /// doesn't exist. It just doesn't send a link in that case.
    ///
    /// Also, the method might be rate-limited, but that is
    /// not disclosed (it fails silently).
    async fn send_password_recovery(
        email: String,
        user_context: Option<UserContext>,
    ) -> Result<(), AuthError>;

    /// Check if the recovery token provided is valid (e.g.
    /// not expired).
    ///
    /// Returns the username of the user.
    async fn check_recovery_token(token: String) -> Result<i64, AuthError>;

    /// Reset the password to the specified value using the
    /// recovery token.
    async fn recover_password(token: String, password: String) -> Result<(), AuthError>;

    //// Session management

    /// Get the list of all sessions (active and inactive) for this account.
    async fn get_sessions(access_token: String, offset: i64) -> Result<Vec<Session>, AuthError>;

    /// Log out of a session.
    async fn terminate_session(access_token: String, id: i64) -> Result<(), AuthError>;

    /// Terminate all sessions but the one from the access token.
    async fn terminate_all_sessions(access_token: String) -> Result<(), AuthError>;

    //// Account management

    /// Change own user's password.
    ///
    /// This method can return a TFA wait token for [`AuthService::check_tfa_status`].
    async fn change_password(
        access_token: String,
        old_password: String,
        new_password: String,
        user_context: Option<UserContext>,
    ) -> Result<Option<String>, AuthError>;

    /// Change this user's email.
    ///
    /// An email will be sent to the old address with a recovery link,
    /// and an email is going to be sent to the new address with the
    /// verification email.
    async fn change_email(access_token: String, new_email: String) -> Result<(), AuthError>;

    /// Use a token sent to the old email from [`AuthService::change_email`]
    /// to revert the change.
    async fn cancel_email_change(token: String) -> Result<(), AuthError>;

    /// Generate a token with the TOTP secret in `jti`
    async fn generate_tfa_secret() -> Result<String, AuthError>;

    /// Configure TOTP TFA for the user
    async fn set_totp_tfa(user_id: i64, totp_token: String, code: String) -> Result<(), AuthError>;

    /// Get TFA settings for the user
    async fn get_security_settings(user_id: i64) -> Result<SecuritySettings, AuthError>;

    /// Enable email TFA for the user
    async fn enable_email_tfa(user_id: i64) -> Result<(), AuthError>;

    //// User info

    /// Get a user by their ID.
    async fn get_by_id(id: i64) -> Result<Option<AuthUser>, AuthError>;

    /// Get user info in bulk by their IDs.
    async fn get_by_ids(ids: Vec<i64>) -> Result<HashMap<i64, AuthUser>, AuthError>;

    /// Get a user by their username.
    async fn get_by_name(name: String) -> Result<Option<AuthUser>, AuthError>;

    /// Get users in bulk by their names.
    async fn get_by_names(name: Vec<String>) -> Result<HashMap<String, AuthUser>, AuthError>;

    /// Use an access token to get its session ID and
    /// the [`AuthUser`] associated with it.
    async fn get_by_token(token: String) -> Result<(i64, AuthUser), AuthError>;

    //// Administrative actions

    /// Completely delete a user by their ID.
    async fn unsafe_delete_user(id: i64) -> Result<(), AuthError>;

    /// Change the ban status of a user.
    async fn hard_ban(user_id: i64, banned: bool) -> Result<(), AuthError>;

    /// Update the [`PermissionLevel`] of a user.
    async fn set_permission_level(
        user_id: i64,
        permission_level: PermissionLevel,
    ) -> Result<(), AuthError>;

    /// Get the IDs of default users like `@system` and
    /// `@deleted`.
    async fn get_meta_users() -> Result<MetaUsers, AuthError>;

    /// Clean all leftover data that is no longer needed.
    async fn vacuum() -> Result<(), AuthError>;
}

/// Quick methods related to the authentication service
pub struct Auth;
impl Auth {
    client_tcp!(AuthServiceClient);
}
