use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::auth::login_email::TfaType;
use async_graphql::{Context, Object, SimpleObject, Union, ID};
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::OAuthAuthorizeResult;

#[derive(Default)]
pub struct OAuth2AuthorizeAcceptMutation;

#[derive(Union)]
#[graphql(name = "OAuthAuthorizationResult")]
enum GOAuthAuthorizationResult {
    Redirect(OAuthAuthorizationRedirect),
    TfaRequired(OAuthAuthorizationTfaRequired),
}

/// The authorisation was successful and user can proceed to this URL
#[derive(SimpleObject)]
struct OAuthAuthorizationRedirect {
    /// The URL to redirect the user to
    redirect_uri: String,
}

/// Two-factor authentication is required to approve this request
#[derive(SimpleObject)]
struct OAuthAuthorizationTfaRequired {
    /// What TFA type to use
    tfa_type: TfaType,
    /// Token for requesting the TFA result with `check_tfa_status`
    tfa_wait_token: String,
}

#[Object]
impl OAuth2AuthorizeAcceptMutation {
    /// Accept the authorisation request and get the URI to redirect the client to
    ///
    /// Might also require two-factor authentication if sensitive scopes have been
    /// requested.
    async fn oauth2_authorize_accept(
        &self,
        ctx: &Context<'_>,
        flow_id: ID,
    ) -> Result<GOAuthAuthorizationResult, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        let flow_id = flow_id.try_into().map_err(|_| RespError::InvalidId)?;

        req.require_user()?;

        let access_token = req
            .access_token
            .clone()
            .ok_or(anyhow!("access token not set, but user is present"))?;
        let user_context = Some(req.user_context.clone());

        let result = req
            .auth
            .oauth2_authorize_accept(context::current(), flow_id, access_token, user_context)
            .await??;

        Ok(match result {
            OAuthAuthorizeResult::Redirect { redirect_uri } => {
                GOAuthAuthorizationResult::Redirect(OAuthAuthorizationRedirect { redirect_uri })
            }
            OAuthAuthorizeResult::TfaRequired {
                tfa_type,
                tfa_wait_token,
            } => GOAuthAuthorizationResult::TfaRequired(OAuthAuthorizationTfaRequired {
                tfa_type: tfa_type.into(),
                tfa_wait_token,
            }),
        })
    }
}
