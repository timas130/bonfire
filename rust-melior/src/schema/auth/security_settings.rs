use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::user::User;
use async_graphql::{Context, SimpleObject};
use c_core::prelude::tarpc::context;
use c_core::services::auth::{AuthError, OAuthProvider};

/// Various security settings of a [`User`]
#[derive(SimpleObject)]
#[graphql(name = "SecuritySettings")]
pub struct GSecuritySettings {
    /// Whether this user has a Google account linked
    pub google_linked: bool,
    /// Whether this user has been migrated from Firebase
    pub firebase_linked: bool,
}

impl User {
    pub(crate) async fn _security_settings(
        &self,
        ctx: &Context<'_>,
    ) -> Result<GSecuritySettings, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();
        let user = req.require_user()?;

        if user.id != self._id {
            return Err(AuthError::AccessDenied.into());
        }

        let settings = req
            .auth
            .get_security_settings(context::current(), self._id)
            .await??;

        Ok(GSecuritySettings {
            google_linked: settings.oauth.contains_key(&OAuthProvider::Google),
            firebase_linked: settings.oauth.contains_key(&OAuthProvider::LegacyFirebase),
        })
    }
}
