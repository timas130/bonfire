use crate::util::has_user_context::HasUserContext;
use c_core::prelude::chrono::{DateTime, Utc};
use sqlx::types::ipnetwork::IpNetwork;

pub const SESSION_EXPIRE_DAYS: i64 = 30;

#[allow(dead_code)]
#[derive(Debug)]
pub struct Session {
    pub id: i64,
    pub user_id: i64,
    pub account_id: Option<i64>,
    pub provider: Option<i32>,
    pub ip: Option<IpNetwork>,
    pub expires: Option<DateTime<Utc>>,
    pub last_refreshed: DateTime<Utc>,
    pub user_agent: Option<String>,
    pub refresh_token: String,
    pub created_at: DateTime<Utc>,
    pub last_online: DateTime<Utc>,
}

impl From<Session> for c_core::services::auth::Session {
    fn from(raw: Session) -> Self {
        Self {
            id: raw.id,
            user_id: raw.user_id,
            active: raw
                .expires
                .map(|expires| Utc::now() < expires)
                .unwrap_or(true),
            last_active: raw.last_refreshed,
            last_online: raw.last_online,
            created_at: raw.created_at,
            context: raw.user_context(),
        }
    }
}
