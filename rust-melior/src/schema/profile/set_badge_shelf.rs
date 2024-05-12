use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::profile::customization::profile::GProfileCustomization;
use async_graphql::{Context, Object, ID};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct SetBadgeShelfMutation;

#[Object]
impl SetBadgeShelfMutation {
    /// Set the items of the badge shelf in the profile
    async fn set_badge_shelf(
        &self,
        ctx: &Context<'_>,
        badge_ids: [Option<ID>; 4],
    ) -> Result<GProfileCustomization, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let badge_ids = badge_ids.map(|id| id.and_then(|id| id.try_into().ok()));

        let profile = req
            .profile
            .set_badge_shelf(context::current(), user.id, badge_ids)
            .await??;

        Ok((user.id, profile).into())
    }

    /// Show or hide the badge shelf in the profile
    async fn show_badge_shelf(
        &self,
        ctx: &Context<'_>,
        show: bool,
    ) -> Result<GProfileCustomization, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let profile = req
            .profile
            .set_show_badge_shelf(context::current(), user.id, show)
            .await??;

        Ok((user.id, profile).into())
    }
}
