use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object};
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct MeQuery;

#[Object]
impl MeQuery {
    /// Get currently authenticated user
    async fn me(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(user) = &req.user else {
            return Err(AuthError::Unauthenticated.into());
        };

        User::by_auth(ctx, user.clone()).await
    }
}
