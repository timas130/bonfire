use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct ChangePasswordMutation;

#[Object]
impl ChangePasswordMutation {
    async fn change_password(
        &self,
        ctx: &Context<'_>,
        #[graphql(secret)] old_password: String,
        #[graphql(secret)] new_password: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let Some(access_token) = req.access_token.clone() else {
            return Err(AuthError::Unauthenticated.into());
        };

        req.auth
            .change_password(
                context::current(),
                access_token,
                old_password,
                new_password,
                Some(req.user_context.clone()),
            )
            .await??;

        Ok(OkResp)
    }
}
