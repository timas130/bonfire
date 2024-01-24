use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use c_core::services::auth::AuthError;

#[derive(Default)]
pub struct ChangeEmailMutation;

#[Object]
impl ChangeEmailMutation {
    async fn change_email(
        &self,
        ctx: &Context<'_>,
        new_email: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let access_token = match &req.access_token {
            Some(token) => token,
            None => return Err(AuthError::Unauthenticated.into()),
        };

        req.auth
            .change_email(context::current(), access_token.clone(), new_email)
            .await??;

        Ok(OkResp)
    }
}
