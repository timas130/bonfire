use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::schema::profile::badge::GBadge;
use crate::utils::connection::PaginatedExt;
use async_graphql::connection::Connection;
use async_graphql::Context;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;

impl User {
    pub(crate) async fn _badges(
        &self,
        ctx: &Context<'_>,
        after: Option<String>,
    ) -> Result<Connection<DateTime<Utc>, GBadge>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let after = match after {
            Some(after) => Some(after.parse().map_err(|_| RespError::InvalidId)?),
            None => None,
        };
        let badges = req
            .profile
            .get_user_badges(context::current(), self._id, after)
            .await??;

        Ok(badges.into_connection())
    }
}
