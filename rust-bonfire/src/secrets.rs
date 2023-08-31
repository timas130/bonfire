use serde::{Deserialize, Serialize};
use std::fs::File;

#[derive(Clone, Serialize, Deserialize)]
pub struct SecretsConfig {
    pub config: MainConfig,
    pub keys: KeysConfig,
}
impl SecretsConfig {
    pub fn load() -> anyhow::Result<Self> {
        let file = File::open("secrets/Secrets.json")?;
        Ok(serde_json::from_reader(file)?)
    }
}

#[derive(Clone, Serialize, Deserialize)]
pub struct MainConfig {
    pub database_login: String,
    pub database_password: String,
    pub database_name: String,
    pub database_address: String,
}

#[derive(Clone, Serialize, Deserialize)]
pub struct KeysConfig {
    pub internal_key: String,
}
