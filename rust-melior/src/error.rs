use c_core::prelude::anyhow;
use c_core::prelude::anyhow::Error;
use c_core::prelude::tarpc::client::RpcError;
use c_core::services::auth::AuthError;
use c_core::services::email::EmailError;
use c_core::services::level::LevelError;
use std::sync::Arc;
use thiserror::Error;

#[derive(Clone, Error, Debug)]
pub enum RespError {
    #[error("OutOfSync: Internal error")]
    OutOfSync,
    #[error("{0}")]
    Auth(#[from] AuthError),
    #[error("{0}")]
    Email(#[from] EmailError),
    #[error("{0}")]
    Level(#[from] LevelError),
    #[error("Rpc: An unknown error has occurred")]
    Rpc(#[from] Arc<RpcError>),
    #[error("Anyhow: An unknown error has occurred")]
    Anyhow(#[from] Arc<anyhow::Error>),
}

impl From<RpcError> for RespError {
    fn from(value: RpcError) -> Self {
        Self::Rpc(Arc::new(value))
    }
}

impl From<anyhow::Error> for RespError {
    fn from(value: Error) -> Self {
        Self::Anyhow(Arc::new(value))
    }
}
