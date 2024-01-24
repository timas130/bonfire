use async_graphql::extensions::{Extension, ExtensionContext, NextExecute};
use async_graphql::Response;
use async_trait::async_trait;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::Error;
use c_core::prelude::tarpc::client::RpcError;
use c_core::services::auth::AuthError;
use c_core::services::email::EmailError;
use c_core::services::level::LevelError;
use std::sync::Arc;
use thiserror::Error;
use tracing::warn;

#[derive(Clone, Error, Debug)]
pub enum RespError {
    #[error("OutOfSync: Internal error")]
    OutOfSync,
    #[error("InvalidId: This ID is invalid")]
    InvalidId,
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

#[derive(Clone)]
pub struct LogErrorsMiddleware;

#[async_trait]
impl Extension for LogErrorsMiddleware {
    async fn execute(
        &self,
        ctx: &ExtensionContext<'_>,
        operation_name: Option<&str>,
        next: NextExecute<'_>,
    ) -> Response {
        let result = next.run(ctx, operation_name).await;
        for error in &result.errors {
            warn!("error while running op={operation_name:?}: {error:?}");
        }

        result
    }
}
