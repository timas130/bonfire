use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Enum, InputObject, Object, SimpleObject, Union};
use c_core::prelude::tarpc::context;
use c_core::services::auth;
use c_core::services::auth::{LoginEmailOptions, LoginEmailResponse};

#[derive(Default)]
pub struct LoginEmailMutation;

/// Parameters for logging in via email
#[derive(InputObject)]
struct LoginEmailInput {
    /// User's email address
    email: String,
    /// User's password
    #[graphql(secret)]
    password: String,
}

/// The result of logging in via email and password
#[derive(Union)]
enum LoginResult {
    Success(LoginResultSuccess),
    TfaRequired(LoginResultTfaRequired),
}
impl From<LoginEmailResponse> for LoginResult {
    fn from(value: LoginEmailResponse) -> Self {
        match value {
            LoginEmailResponse::Success {
                access_token,
                refresh_token,
            } => LoginResultSuccess {
                access_token,
                refresh_token,
            }
            .into(),
            LoginEmailResponse::TfaRequired {
                tfa_type,
                tfa_wait_token,
            } => LoginResultTfaRequired {
                tfa_type: tfa_type.into(),
                tfa_wait_token,
            }
            .into(),
        }
    }
}

/// Successful one-step login
#[derive(SimpleObject)]
pub struct LoginResultSuccess {
    /// The access token
    pub access_token: String,
    /// The refresh token
    pub refresh_token: String,
}

/// TFA is required to finish logging in
#[derive(SimpleObject)]
struct LoginResultTfaRequired {
    /// Type of TFA required
    tfa_type: TfaType,
    /// Token to request the status of the TFA flow
    tfa_wait_token: String,
}

/// A two-factor authentication method
#[derive(Copy, Clone, Eq, PartialEq, Enum)]
pub enum TfaType {
    /// The user should input a one-time code from
    /// their authenticator app.
    Totp,
    /// The user should visit a link from the email
    /// sent to their address.
    EmailLink,
}
impl From<auth::TfaType> for TfaType {
    fn from(value: auth::TfaType) -> Self {
        match value {
            auth::TfaType::Totp => Self::Totp,
            auth::TfaType::EmailLink => Self::EmailLink,
        }
    }
}

#[Object]
impl LoginEmailMutation {
    /// Login via email and password
    async fn login_email(
        &self,
        ctx: &Context<'_>,
        input: LoginEmailInput,
    ) -> Result<LoginResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let input = LoginEmailOptions {
            email: input.email,
            password: input.password,
            context: Some(req.user_context.clone()),
        };

        let result = req.auth.login_email(context::current(), input).await??;

        Ok(result.into())
    }
}
