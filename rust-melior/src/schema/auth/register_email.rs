use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{ComplexObject, Context, InputObject, Object, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::auth::{RegisterEmailOptions, RegisterEmailResponse};
use o2o::o2o;

#[derive(Default)]
pub struct RegisterEmailMutation;

/// Parameters for registering a new account with email
#[derive(InputObject)]
struct RegisterEmailInput {
    /// Email address for the new account
    email: String,
    /// Password for the new account
    password: String,
}

/// New account registration result
#[derive(SimpleObject, o2o)]
#[graphql(complex)]
#[from_owned(RegisterEmailResponse)]
struct RegisterEmailResult {
    /// The registered user ID
    #[graphql(skip)]
    user_id: i64,
    /// Access token for the user
    access_token: String,
    /// Refresh token for the user
    refresh_token: String,
}
#[ComplexObject]
impl RegisterEmailResult {
    /// Newly registered user
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self.user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }
}

#[Object]
impl RegisterEmailMutation {
    /// Create a new account with email and password
    async fn register_email(
        &self,
        ctx: &Context<'_>,
        input: RegisterEmailInput,
    ) -> Result<RegisterEmailResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let input = RegisterEmailOptions {
            email: input.email,
            password: input.password,
            username: None,
            context: Some(req.user_context.clone()),
        };

        let resp = req.auth.register_email(context::current(), input).await??;

        Ok(resp.into())
    }
}
