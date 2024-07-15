use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::image::ImageLink;
use crate::models::user::User;
use async_graphql::{ComplexObject, Context, Object, SimpleObject, ID};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context;
use c_core::services::profile::Badge;
use o2o::o2o;

/// A badge displayed in the profile or near a username
#[derive(SimpleObject, o2o)]
#[from_owned(Badge)]
#[graphql(complex, name = "Badge")]
pub struct GBadge {
    /// Unique ID for this badge (unique for every occurrence)
    #[from(~.into())]
    pub id: ID,
    #[graphql(skip)]
    #[from(user_id)]
    pub _user_id: i64,
    /// Badge name
    pub name: String,
    /// Badge description (inline-only Markdown supported)
    pub description: String,
    #[graphql(skip)]
    pub mini_image_id: i64,
    #[graphql(skip)]
    pub image_id: i64,
    /// Fandom related to this badge, if any
    pub fandom_id: Option<i64>,
    /// Arbitrary link with more information about the badge or something related to it
    ///
    /// Usually leads to `https://bonfire.moe/r/<something>`.
    pub link: Option<String>,
    /// When the badge was given to the user
    pub created_at: DateTime<Utc>,
}

#[ComplexObject]
impl GBadge {
    /// User who owns this badge
    async fn user(&self, ctx: &Context<'_>) -> Result<User, RespError> {
        User::by_id(ctx, self._user_id)
            .await?
            .ok_or(RespError::OutOfSync)
    }

    /// Image to be shown beside a username and in other small spaces
    async fn mini_image(&self, ctx: &Context<'_>) -> Result<ImageLink, RespError> {
        ImageLink::by_id(ctx, self.mini_image_id).await
    }

    /// The badge's image in its full glory and size
    async fn image(&self, ctx: &Context<'_>) -> Result<ImageLink, RespError> {
        ImageLink::by_id(ctx, self.image_id).await
    }
}

#[derive(Default)]
pub struct BadgeQuery;

#[Object]
impl BadgeQuery {
    /// Get a single Badge by its ID
    async fn badge(&self, ctx: &Context<'_>, id: ID) -> Result<Option<GBadge>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let id = id.try_into().map_err(|_| RespError::InvalidId)?;

        Ok(req
            .profile
            .get_badge(context::current(), id)
            .await??
            .map(Into::into))
    }
}
