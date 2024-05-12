use crate::context::{ContextExt, ReqContext};
use crate::error::RespError;
use crate::schema::profile::customization::account::GAccountCustomization;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel;

#[derive(Default)]
pub struct InternalAccountCustomizationQuery;

#[Object]
impl InternalAccountCustomizationQuery {
    /// Internal method for fetching AccountCustomization skipping loading the User
    async fn internal_account_customization(
        &self,
        ctx: &Context<'_>,
        user_id: i64,
    ) -> Result<GAccountCustomization, RespError> {
        ctx.require_permission_level(PermissionLevel::System)?;

        let req = ctx.data_unchecked::<ReqContext>();

        Ok((
            user_id,
            req.profile
                .get_account_customization(context::current(), user_id)
                .await??,
        )
            .into())
    }
}
