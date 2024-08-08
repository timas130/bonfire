use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct RegisterGifShareMutation;

#[Object]
impl RegisterGifShareMutation {
    /// Notify the server that a GIF has been shared
    ///
    /// You must call this method when sharing a GIF.
    /// If a GIF has been accessed from a search query, that
    /// query should be passed into `query`.
    async fn register_gif_share(
        &self,
        ctx: &Context<'_>,
        id: ID,
        query: Option<String>,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.require_user()?;

        req.gif
            .on_gif_share(context::current(), id.0, query, req.get_gif_context())
            .await??;

        Ok(OkResp)
    }
}
