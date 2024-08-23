use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct GifSearchSuggestionsQuery;

#[Object]
impl GifSearchSuggestionsQuery {
    /// Get some suggestions for a search query
    ///
    /// If `query` is empty, a list of searchable categories
    /// is returned.
    async fn gif_search_suggestions(
        &self,
        ctx: &Context<'_>,
        query: String,
    ) -> Result<Vec<String>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok(req
            .gif
            .get_search_suggestions(context::current(), query, req.get_gif_context()?)
            .await??)
    }
}
