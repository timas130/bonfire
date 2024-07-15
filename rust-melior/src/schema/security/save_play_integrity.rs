use crate::context::ReqContext;
use crate::error::RespError;
use crate::utils::ok::OkResp;
use async_graphql::{Context, Object};
use c_core::prelude::tarpc::context;

#[derive(Default)]
pub struct SavePlayIntegrityMutation;

#[Object]
impl SavePlayIntegrityMutation {
    /// Decode and save Play Integrity token
    async fn save_play_integrity(
        &self,
        ctx: &Context<'_>,
        package_name: String,
        intention_token: String,
        token: String,
    ) -> Result<OkResp, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        req.security
            .save_play_integrity(
                context::current(),
                user.id,
                intention_token,
                package_name,
                token,
            )
            .await??;

        Ok(OkResp)
    }
}
