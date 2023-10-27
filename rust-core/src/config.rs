use config::{Environment, File};
use serde::Deserialize;

#[derive(Clone, Debug, Deserialize)]
pub struct GlobalConfig {
    pub database_url: String,
    pub jwt_secret: String,
    pub sentry_dsn: String,
    pub internal_key: String,

    pub urls: UrlsConfig,
    pub ports: PortsConfig,
    pub email: EmailConfig,
    pub images: ImagesConfig,
    pub google: OAuthConfig,
    pub firebase: FirebaseConfig,
}
impl GlobalConfig {
    pub(crate) fn load() -> anyhow::Result<Self> {
        Ok(config::Config::builder()
            .add_source(File::with_name("config"))
            .add_source(Environment::with_prefix("canana"))
            .build()?
            .try_deserialize()?)
    }
}

#[derive(Clone, Debug, Deserialize)]
pub struct UrlsConfig {
    pub verify_link: String,
    pub email_tfa_link: String,
    pub recovery_link: String,
    pub cancel_email_change_link: String,
    pub image_proxy_link: String,
    pub oauth_redirect_link: String,
}

#[derive(Clone, Debug, Deserialize)]
pub struct PortsConfig {
    pub auth: u16,
    pub email: u16,
    pub images: u16,
    pub level: u16,
}

#[derive(Clone, Debug, Deserialize)]
pub struct EmailConfig {
    pub host: String,
    pub port: u16,
    pub username: String,
    pub password: String,
    pub tls: bool,
    pub from: String,
}

#[derive(Clone, Debug, Deserialize)]
#[serde(tag = "type", rename_all = "snake_case")]
pub enum ImagesConfig {
    Local { root: String },
}

#[derive(Clone, Debug, Deserialize)]
pub struct OAuthConfig {
    pub client_id: String,
    pub client_secret: String,
}

#[derive(Clone, Debug, Deserialize)]
pub struct FirebaseConfig {
    pub project_id: String,

    pub scrypt_signer_key: String,
    pub scrypt_salt_separator: String,
    pub scrypt_rounds: u32,
    pub scrypt_mem_cost: u32,
}
