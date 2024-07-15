use crate::context::{ContextExt, ReqContext};
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::Context;
use c_core::prelude::chrono::NaiveDate;
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::PermissionLevel;
use c_core::services::auth::AuthError;

impl User {
    pub(crate) async fn _birthday(
        &self,
        ctx: &Context<'_>,
    ) -> Result<Option<NaiveDate>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        if user.id != self._id && !ctx.has_permission_level(PermissionLevel::System) {
            return Err(AuthError::AccessDenied.into());
        }

        Ok(req
            .profile
            .get_birthday(context::current(), self._id)
            .await??)
    }

    pub(crate) async fn _is_years_old(
        &self,
        ctx: &Context<'_>,
        age: u32,
    ) -> Result<Option<bool>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        if user.id != self._id && !ctx.has_permission_level(PermissionLevel::System) {
            return Err(AuthError::AccessDenied.into());
        }

        Ok(req
            .profile
            .is_age_at_least(context::current(), self._id, age)
            .await??)
    }
}
