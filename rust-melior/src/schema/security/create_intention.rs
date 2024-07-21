use crate::context::ReqContext;
use crate::error::RespError;
use async_graphql::{Context, Enum, Object};
use c_core::prelude::tarpc::context;
use c_core::services::security::IntentionType;

#[derive(Default)]
pub struct CreateIntentionMutation;

/// Reason for starting an integrity/bot check
#[derive(Enum, Debug, Copy, Clone, Eq, PartialEq, Ord, PartialOrd)]
#[graphql(name = "IntentionType")]
enum GIntentionType {
    /// Just for the sake of it
    Generic,
}

impl From<GIntentionType> for IntentionType {
    fn from(value: GIntentionType) -> Self {
        match value {
            GIntentionType::Generic => Self::Generic,
        }
    }
}

#[Object]
impl CreateIntentionMutation {
    /// Indicate that the user intends to do an integrity/bot check for
    /// whatever reason.
    ///
    /// An intention token is returned, which should be used as the
    /// request hash (or equivalent)
    async fn create_security_intention(
        &self,
        ctx: &Context<'_>,
        intention_type: GIntentionType,
    ) -> Result<String, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        let token = req
            .security
            .create_intention(context::current(), user.id, intention_type.into())
            .await??;

        Ok(token)
    }
}
