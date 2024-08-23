use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct AddGifToFavouritesMutation;

#[Object]
impl AddGifToFavouritesMutation {
    /// Add a GIF to favourites
    async fn add_gif_to_favourites(
        &self,
        ctx: &Context<'_>,
        gif_id: ID,
    ) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let user = req.require_user()?;
        let gif_context = req.get_gif_context()?;

        req.gif
            .add_to_favourites(context::current(), gif_id.0, gif_context)
            .await??;

        User::by_auth(ctx, user.clone()).await
    }
}
