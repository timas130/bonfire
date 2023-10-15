use async_graphql::{ComplexObject, Context, ID, SimpleObject};
use o2o::o2o;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::auth;
use c_core::services::auth::{AuthError, UserContext};
use crate::context::ReqContext;
use crate::error::RespError;

#[derive(Default)]
pub struct ActiveSessionsQuery;

#[derive(SimpleObject, o2o)]
#[graphql(complex)]
#[from_owned(auth::Session)]
pub struct Session {
    /// Internal numeric ID without the [`ID`] type
    #[graphql(skip)]
    #[from(id)]
    pub _id: i64,
    /// Whether the session is active and not expired
    pub active: bool,
    /// Last login_refresh call timestamp
    pub last_active: DateTime<Utc>,
    /// First login timestamp
    pub created_at: DateTime<Utc>,
    #[graphql(skip)]
    #[from(context)]
    pub _context: Option<UserContext>,
}
#[ComplexObject]
impl Session {
    /// The session ID
    async fn id(&self) -> ID {
        self._id.into()
    }

    /// Last IP that refreshed the session
    async fn ip(&self) -> Option<String> {
        self._context.map(|x| x.ip.to_string())
    }

    /// Full user agent string of the client that
    /// last refreshed the session
    async fn user_agent(&self) -> Option<String> {
        self._context.map(|x| x.user_agent)
    }
}

impl ActiveSessionsQuery {
    /// Get the list of currently active sessions
    pub(crate) async fn active_sessions(&self, ctx: &Context<'_>, #[graphql(default)] offset: i64) -> Result<Session, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(access_token) = req.access_token.clone() else {
            return Err(AuthError::Unauthenticated.into());
        };

        let sessions = req.auth
            .get_sessions(context::current(), access_token, offset)
            .await??;

        Ok(sessions.into_iter().map(From::from).collect())
    }
}
