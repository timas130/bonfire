use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::profile::customization::account::GAccountCustomization;
use async_graphql::{Context, Enum, Object};
use c_core::prelude::tarpc::context;
use c_core::services::profile::NicknameColorPreset;
use o2o::o2o;

#[derive(Default)]
pub struct SetNicknameColorMutation;

/// Presets for nickname colors, unlockable after
#[derive(Copy, Clone, Enum, Eq, PartialEq, o2o)]
#[owned_into(NicknameColorPreset)]
enum GNicknameColorPreset {
    Green,
    Purple,
    Aqua,
    Blue,
    Orange,
    Red,
    Bonfire,
    Pink,
}

#[Object]
impl SetNicknameColorMutation {
    /// Set a custom nickname color from the given presets
    async fn set_nickname_color(
        &self,
        ctx: &Context<'_>,
        preset: Option<GNicknameColorPreset>,
    ) -> Result<GAccountCustomization, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let account = req
            .profile
            .set_nickname_color(context::current(), user.id, preset.map(Into::into))
            .await??;

        Ok((user.id, account).into())
    }
}
