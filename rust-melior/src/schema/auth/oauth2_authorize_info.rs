use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Object, SimpleObject, Union, ID};
use c_core::prelude::tarpc::context;
use c_core::services::auth::{OAuthAuthorizeInfo, OAuthClientInfo};
use o2o::o2o;
use std::collections::HashMap;

#[derive(Default)]
pub struct OAuth2AuthorizeInfoQuery;

#[derive(Union)]
#[graphql(name = "OAuthAuthorizeInfo")]
pub enum GOAuthAuthorizeInfo {
    AlreadyAuthorized(OAuthAlreadyAuthorized),
    Prompt(OAuthAuthorizationPrompt),
}

/// The client has already been authorised, it's safe to just redirect them
#[derive(SimpleObject)]
pub struct OAuthAlreadyAuthorized {
    /// Where to send the user
    redirect_uri: String,
}

/// A confirmation prompt is required to authorise this client
#[derive(SimpleObject)]
pub struct OAuthAuthorizationPrompt {
    /// ID for the flow created for this authorisation request
    flow_id: Option<i64>,
    /// What scopes the client is requesting
    scopes: Vec<String>,
    /// Information about the OAuth client
    client: GOAuthClient,
}

/// Information about some OAuth client
#[derive(SimpleObject, o2o)]
#[from_owned(OAuthClientInfo)]
#[graphql(name = "OAuthClient")]
pub struct GOAuthClient {
    /// Unique ID of this client
    #[from(~.into())]
    pub id: ID,
    /// Name of the client to be displayed to the user
    pub display_name: String,
    /// Link to the privacy policy of this client
    pub privacy_policy_url: String,
    /// Link to this client's terms of service
    pub tos_url: Option<String>,
    /// Whether this client is an official Bonfire app
    pub official: bool,
}

#[Object]
impl OAuth2AuthorizeInfoQuery {
    /// Get info for authorising an OAuth client
    async fn oauth_authorize_info(
        &self,
        ctx: &Context<'_>,
        query: HashMap<String, String>,
    ) -> Result<GOAuthAuthorizeInfo, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let access_token = req.access_token.clone();

        let info = req
            .auth
            .oauth2_authorize_info(context::current(), query, access_token)
            .await??;

        Ok(match info {
            OAuthAuthorizeInfo::AlreadyAuthorized { redirect_uri } => {
                GOAuthAuthorizeInfo::AlreadyAuthorized(OAuthAlreadyAuthorized { redirect_uri })
            }
            OAuthAuthorizeInfo::Prompt {
                flow_id,
                scopes,
                client,
            } => GOAuthAuthorizeInfo::Prompt(OAuthAuthorizationPrompt {
                flow_id,
                scopes,
                client: client.into(),
            }),
        })
    }
}
