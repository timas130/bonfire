use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::gif::search_gif::GGifItem;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct RecentGifsQuery;

#[Object]
impl RecentGifsQuery {
    /// Get up to 10 GIFs recently shared by the user
    async fn recent_gifs(&self, ctx: &Context<'_>) -> Result<Vec<GGifItem>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let gif_context = req.get_gif_context()?;

        Ok(req
            .gif
            .get_recent_gifs(context::current(), gif_context)
            .await??
            .into_iter()
            .map(From::from)
            .collect())
    }
}
