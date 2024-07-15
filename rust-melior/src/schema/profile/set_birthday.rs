use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, Object};
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::NaiveDate;
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct SetBirthdayMutation;

#[Object]
impl SetBirthdayMutation {
    /// Set own birthday for age verification purposes
    async fn set_birthday(
        &self,
        ctx: &Context<'_>,
        birthday: NaiveDate,
    ) -> Result<User, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        req.profile
            .set_birthday(context::current(), user.id, birthday)
            .await??;

        Ok(User::by_id(ctx, user.id)
            .await?
            .ok_or(anyhow!("out of sync"))?)
    }
}
