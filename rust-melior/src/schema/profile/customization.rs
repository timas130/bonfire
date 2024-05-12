use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use crate::schema::profile::customization::profile::GProfileCustomization;
use account::GAccountCustomization;
use async_graphql::Context;
use c_core::prelude::tarpc::context;

pub(crate) mod account;
pub(crate) mod profile;

impl User {
    pub(crate) async fn _customization(
        &self,
        ctx: &Context<'_>,
    ) -> Result<GAccountCustomization, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok((
            self._id,
            req.profile
                .get_account_customization(context::current(), self._id)
                .await??,
        )
            .into())
    }

    pub(crate) async fn _profile(
        &self,
        ctx: &Context<'_>,
    ) -> Result<GProfileCustomization, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        Ok((
            self._id,
            req.profile
                .get_profile_customization(context::current(), self._id)
                .await??,
        )
            .into())
    }
}
