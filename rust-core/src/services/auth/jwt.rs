use jsonwebtoken::Validation;
use nanoid::nanoid;
use serde::{Deserialize, Serialize};
use std::time::{Duration, SystemTime};
use tracing::info;

pub const JWT_ISS: &str = "https://bonfire.moe";

const JWT_VERIFY_AUD: &str = "verify-email";
const JWT_VERIFY_EXPIRY: Duration = Duration::from_secs(600);

const JWT_ACCESS_AUD: &str = "access";
const JWT_ACCESS_EXPIRY: Duration = Duration::from_secs(24 * 3600);

const JWT_TFA_AUD: &str = "tfa";
pub const JWT_TFA_EXPIRY_SECONDS: u64 = 600;
const JWT_TFA_EXPIRY: Duration = Duration::from_secs(JWT_TFA_EXPIRY_SECONDS);

const JWT_TFA_WAIT_AUD: &str = "tfa-wait";
const JWT_TFA_WAIT_EXPIRY: Duration = JWT_TFA_EXPIRY;

const JWT_CLEARANCE_AUD: &str = "clearance";
pub const JWT_CLEARANCE_EXPIRY: Duration = Duration::from_secs(300);

const JWT_CANCEL_EMAIL_CHANGE_AUD: &str = "cancel-email-change";
const JWT_CANCEL_EMAIL_CHANGE_EXPIRY: Duration = Duration::from_secs(3 * 24 * 3600);

const JWT_TFA_TOTP_AUD: &str = "tfa-totp";
const JWT_TFA_TOTP_EXPIRY: Duration = Duration::from_secs(600);

/// A generic struct for all tokens.
///
/// ## Example
/// ```
/// use jsonwebtoken::{Algorithm, Validation};
/// # use c_core::ServiceBase;
/// # use c_core::services::auth::jwt::TokenClaims;
///
/// # tokio_test::block_on(async {
/// // Normally you would use the ServiceBase provided in the service struct.
/// let base = ServiceBase::load().await.unwrap();
/// let decoding_key = base.jwt_decoding_key;
///
/// let tfa_token = "eyJ0eXAiOiJK...NSFRdThdU";
/// # let tfa_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2NhbmFuYS5zaXQuc2giLCJzdWIiOiI0IiwiYXVkIjoidGZhIiwiZXhwIjo5OTk5OTk5OTk5OSwiaWF0IjoxNjczNjI1NzQ0LCJqdGkiOiIxVDQxcEEtLWRFOXdWczhQaFY1cjYwRTFCOG9Vczg1bCJ9.rJluY9MJi7sqz9cm8P3HvAjXOBxwB-ysyKNSFRdThdU";
///
/// let _ = jsonwebtoken::decode::<TokenClaims>(
///     &tfa_token,
///     &decoding_key,
///     &TokenClaims::get_tfa_validation(Validation::new(Algorithm::HS256)),
/// )
/// .unwrap();
/// # });
/// ```
#[derive(Debug, Serialize, Deserialize)]
pub struct TokenClaims {
    /// The issuer (always `https://bonfire.moe`)
    iss: String,
    /// The user ID
    pub sub: String,
    /// The subject (one of `verify-email`, `access`, `tfa`, `tfa-wait`, etc)
    aud: String,
    /// Expiry time in Unix seconds
    exp: usize,
    /// Issued at in Unix seconds
    iat: usize,
    /// ID of the token, usually a 32-character [`mod@nanoid`]
    ///
    /// For access tokens, the structure is `[session id]:[token nanoid]`.
    pub jti: String,
}

impl TokenClaims {
    fn _new<T: ToString>(jti: String, sub: T, exp: Duration, aud: &str) -> Self {
        Self {
            iss: JWT_ISS.to_string(),
            sub: sub.to_string(),
            aud: aud.to_string(),
            exp: (SystemTime::now() + exp)
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs() as usize,
            iat: SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap()
                .as_secs() as usize,
            jti,
        }
    }

    pub fn new_access(token_id: String, session_id: i64, user_id: i64) -> Self {
        info!("issuing access token user={user_id} jti={session_id}:{token_id}");
        Self::_new(
            format!("{session_id}:{token_id}"),
            user_id,
            JWT_ACCESS_EXPIRY,
            JWT_ACCESS_AUD,
        )
    }
    pub fn get_access_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_ACCESS_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_verify(email: String) -> Self {
        let jti = nanoid!();
        info!("creating verify token id={jti} sub={email}");
        Self::_new(jti, email, JWT_VERIFY_EXPIRY, JWT_VERIFY_AUD)
    }
    pub fn get_verify_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_VERIFY_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_tfa(token_id: String, user_id: i64) -> Self {
        Self::_new(token_id, user_id, JWT_TFA_EXPIRY, JWT_TFA_AUD)
    }
    pub fn get_tfa_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_TFA_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_tfa_wait(token_id: String, user_id: i64) -> Self {
        Self::_new(token_id, user_id, JWT_TFA_WAIT_EXPIRY, JWT_TFA_WAIT_AUD)
    }
    pub fn get_tfa_wait_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_TFA_WAIT_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_clearance(clearance_id: i64) -> Self {
        let jti = nanoid!(32);
        info!("issuing clearance token jti={jti} sub={clearance_id}");
        Self::_new(
            jti,
            clearance_id.to_string(),
            JWT_CLEARANCE_EXPIRY,
            JWT_CLEARANCE_AUD,
        )
    }
    pub fn get_clearance_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_CLEARANCE_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_cancel_email_change(user_id: i64, old_email: &str) -> Self {
        let jti = nanoid!(32);
        info!("issuing cancel email change token jti={jti} user={user_id} old_email={old_email}");
        Self::_new(
            jti,
            format!("{user_id}:{old_email}"),
            JWT_CANCEL_EMAIL_CHANGE_EXPIRY,
            JWT_CANCEL_EMAIL_CHANGE_AUD,
        )
    }
    pub fn get_cancel_email_change_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_CANCEL_EMAIL_CHANGE_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }

    pub fn new_tfa_totp(secret: String) -> Self {
        Self::_new(
            secret,
            "gen".to_string(),
            JWT_TFA_TOTP_EXPIRY,
            JWT_TFA_TOTP_AUD,
        )
    }
    pub fn get_tfa_totp_validation(mut validation: Validation) -> Validation {
        validation.set_audience(&[JWT_TFA_TOTP_AUD]);
        validation.set_issuer(&[JWT_ISS]);
        validation
    }
}
