use crate::check_tfa_status::RawTfaFlow;
use crate::util::session::Session as RawSession;
use c_core::services::auth::UserContext;
use std::net::IpAddr;

/// Represents a struct that might contain a [`UserContext`].
pub trait HasUserContext {
    fn ip(&self) -> Option<IpAddr>;
    fn user_agent(&self) -> Option<String>;

    /// Get the [`UserContext`] from this struct.
    fn user_context(&self) -> Option<UserContext> {
        self.ip()
            .and_then(|ip| self.user_agent().map(|user_agent| (ip, user_agent)))
            .map(|(ip, user_agent)| UserContext { ip, user_agent })
    }
}

impl HasUserContext for RawSession {
    fn ip(&self) -> Option<IpAddr> {
        self.ip.map(|net| net.ip())
    }
    fn user_agent(&self) -> Option<String> {
        self.user_agent.clone()
    }
}

impl HasUserContext for RawTfaFlow {
    fn ip(&self) -> Option<IpAddr> {
        self.ip.map(|net| net.ip())
    }
    fn user_agent(&self) -> Option<String> {
        self.user_agent.clone()
    }
}
