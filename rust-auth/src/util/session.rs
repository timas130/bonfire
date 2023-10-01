use c_core::prelude::chrono::{DateTime, Utc};
use sqlx::types::ipnetwork::IpNetwork;

pub const SESSION_EXPIRE_DAYS: i64 = 30;

#[allow(dead_code)]
#[derive(Debug)]
pub struct Session {
    pub id: i64,
    pub user_id: i64,
    pub account_id: Option<i64>,
    pub provider: Option<String>,
    pub ip: Option<IpNetwork>,
    pub expires: Option<DateTime<Utc>>,
    pub last_refreshed: DateTime<Utc>,
    pub user_agent: Option<String>,
    pub refresh_token: String,
    pub created_at: DateTime<Utc>,
}
