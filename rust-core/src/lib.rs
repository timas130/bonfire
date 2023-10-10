use crate::config::GlobalConfig;
use jsonwebtoken::{Algorithm, DecodingKey, EncodingKey};
use sqlx::postgres::PgPoolOptions;
use sqlx::{Pool, Postgres};
use std::sync::Arc;

pub mod config;
pub mod models;
pub mod page_info;
pub mod prelude;
pub mod services;
pub mod util;

pub type DBPool = Pool<Postgres>;

#[derive(Clone)]
pub struct ServiceBase {
    pub config: Arc<GlobalConfig>,
    pub pool: DBPool,

    pub jwt_header: jsonwebtoken::Header,
    pub jwt_encoding_key: EncodingKey,
    pub jwt_decoding_key: DecodingKey,
}
impl ServiceBase {
    pub async fn load() -> anyhow::Result<Self> {
        let config = GlobalConfig::load()?;
        let pool = PgPoolOptions::new()
            .max_connections(100)
            .connect(&config.database_url)
            .await?;

        let jwt_header = jsonwebtoken::Header::new(Algorithm::HS256);
        let jwt_encoding_key = EncodingKey::from_secret(config.jwt_secret.as_bytes());
        let jwt_decoding_key = DecodingKey::from_secret(config.jwt_secret.as_bytes());

        Ok(Self {
            config: Arc::new(config),
            pool,
            jwt_header,
            jwt_encoding_key,
            jwt_decoding_key,
        })
    }
}
