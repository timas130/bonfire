use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::schema::gif::search_gif::GGifItem;
use crate::utils::connection::PaginatedExt;
use async_graphql::connection::Connection;
use async_graphql::Context;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

impl User {
    pub(crate) async fn _favourite_gifs(
        &self,
        ctx: &Context<'_>,
        after: Option<String>,
    ) -> Result<Connection<DateTime<Utc>, GGifItem>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let gif_context = req.get_gif_context()?;

        let after = match after {
            Some(after) => Some(after.parse().map_err(|_| RespError::InvalidId)?),
            None => None,
        };

        if gif_context.user_id != self._id {
            return Err(AuthError::AccessDenied.into());
        }

        Ok(req
            .gif
            .get_favourite_gifs(context::current(), gif_context, after)
            .await??
            .into_connection())
    }
}
