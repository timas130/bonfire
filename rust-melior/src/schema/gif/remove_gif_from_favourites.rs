use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct RemoveGifFromFavouritesMutation;

#[Object]
impl RemoveGifFromFavouritesMutation {
    /// Remove a GIF from favourites if it exists
    async fn remove_gif_from_favourites(
        &self,
        ctx: &Context<'_>,
        id: ID,
    ) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user = req.require_user()?;
        let gif_context = req.get_gif_context()?;

        req.gif
            .remove_from_favourites(context::current(), id.0, gif_context)
            .await??;

        User::by_auth(ctx, user.clone()).await
    }
}
