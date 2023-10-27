use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object, ID};
use c_core::prelude::anyhow::anyhow;

#[derive(Default)]
pub struct UserByIDQuery;

#[Object]
impl UserByIDQuery {
    /// Get a [`User`] by their ID
    async fn user_by_id(&self, ctx: &Context<'_>, id: ID) -> Result<Option<User>, RespError> {
        User::by_id(
            ctx,
            id.try_into().map_err(|_| anyhow!("failed to parse ID"))?,
        )
        .await
    }
}
