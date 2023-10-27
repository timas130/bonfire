use crate::login_email::{TFA_MODE_EMAIL, TFA_MODE_TOTP};
use crate::AuthServer;
use c_core::services::auth::{AuthError, OAuthProvider, OAuthSettings, SecuritySettings, TfaType};

impl AuthServer {
    pub(crate) async fn _get_security_settings(
        &self,
        user_id: i64,
    ) -> Result<SecuritySettings, AuthError> {
        let user = sqlx::query!("select tfa_mode from users where id = $1", user_id)
            .fetch_one(&self.base.pool)
            .await?;

        let auth_sources = sqlx::query!(
            "select provider, created_at from auth_sources where user_id = $1",
            user_id
        )
        .fetch_all(&self.base.pool)
        .await?;

        Ok(SecuritySettings {
            tfa_type: match user.tfa_mode {
                Some(TFA_MODE_TOTP) => Some(TfaType::Totp),
                Some(TFA_MODE_EMAIL) => Some(TfaType::EmailLink),
                _ => None,
            },
            oauth: auth_sources
                .into_iter()
                .filter_map(|source| {
                    OAuthProvider::try_from(source.provider)
                        .ok()
                        .map(|provider| {
                            (
                                provider,
                                OAuthSettings {
                                    created_at: source.created_at,
                                },
                            )
                        })
                })
                .collect(),
        })
    }
}
