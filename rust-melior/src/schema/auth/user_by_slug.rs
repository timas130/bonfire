use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object};

#[derive(Default)]
pub struct UserBySlugQuery;

#[Object]
impl UserBySlugQuery {
    /// Get a [`User`] by their ID or username
    ///
    /// Since users can't have a username consisting only
    /// of digits,
    async fn user_by_slug(
        &self,
        ctx: &Context<'_>,
        slug: String,
    ) -> Result<Option<User>, RespError> {
        let id = slug.parse::<i64>().ok();
        match id {
            Some(id) => User::by_id(ctx, id).await,
            None => User::by_name(ctx, slug).await,
        }
    }
}
