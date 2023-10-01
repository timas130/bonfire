use chrono::{DateTime, Utc};
use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde::{Deserialize, Serialize};

#[derive(
    Debug,
    Copy,
    Clone,
    Deserialize,
    Serialize,
    IntoPrimitive,
    TryFromPrimitive,
    Ord,
    PartialOrd,
    PartialEq,
    Eq,
)]
#[repr(i32)]
pub enum PermissionLevel {
    User = 1,
    Supermod = 2,
    Admin = 3,
    System = 4,
}

#[derive(
    Debug, Copy, Clone, Deserialize, Serialize, IntoPrimitive, TryFromPrimitive, Eq, PartialEq,
)]
#[repr(i32)]
pub enum TfaMode {
    TOTP = 1,
}

#[derive(Debug, Clone, Deserialize, Serialize, Eq, PartialEq)]
pub struct AuthUser {
    pub id: i64,
    pub username: String,
    pub email: Option<String>,
    /// `None` if not verified, `Some(DateTime)` if verified.
    pub email_verified: Option<DateTime<Utc>>,
    pub permission_level: PermissionLevel,
    pub created_at: DateTime<Utc>,
    pub modified_at: DateTime<Utc>,
    pub tfa_mode: Option<TfaMode>,
    pub hard_banned: bool,
    pub anon: bool,
}
