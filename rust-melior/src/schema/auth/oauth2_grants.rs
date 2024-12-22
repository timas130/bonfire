use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::schema::auth::oauth2_authorize_info::GOAuthClient;
use async_graphql::{ComplexObject, Context, Object, SimpleObject, ID};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::auth::OAuthGrant;
use o2o::o2o;

#[derive(Default)]
pub struct OAuth2GrantsQuery;

#[derive(SimpleObject, o2o)]
#[graphql(name = "OAuthGrant", complex)]
#[from_owned(OAuthGrant)]
pub struct GOAuthGrant {
    /// Unique ID for this grant
    #[from(~.into())]
    pub id: ID,
    /// User that was authorised
    #[graphql(skip)]
    #[from(@.user_id)]
    pub _user_id: i64,
    /// Information about the client authorised
    #[from(~.into())]
    pub client: GOAuthClient,
    /// What scopes this grant has already
    pub scope: Vec<String>,
    /// When the grant was initially created
    pub created_at: DateTime<Utc>,
    /// When was the last time this grant was reauthorised
    pub last_used_at: DateTime<Utc>,
}

#[ComplexObject]
impl GOAuthGrant {
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self._user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }
}

#[Object]
impl OAuth2GrantsQuery {
    /// Get the list of clients which were authorised by the currently authenticated user
    async fn oauth2_grants(
        &self,
        ctx: &Context<'_>,
        offset: u32,
        limit: u32,
    ) -> Result<Vec<GOAuthGrant>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let grants = req
            .auth
            .get_oauth2_grants(context::current(), user.id, offset as i64, limit as i64)
            .await??;

        Ok(grants.into_iter().map(Into::into).collect())
    }
}
