use crate::secrets::SecretsConfig;
use sqlx::{Pool, Postgres};
use std::sync::Arc;

#[derive(Clone)]
pub struct GlobalContext {
    pub secrets: Arc<SecretsConfig>,
    pub pool: Pool<Postgres>,
}
