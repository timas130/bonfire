use async_graphql::extensions::{Extension, ExtensionContext, ExtensionFactory, NextExecute};
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

    // image upload
    #[error("MultipartError: Failed to upload file")]
    MultipartError,
    #[error("InvalidByteSize: This image is too big (or small) to process")]
    InvalidByteSize,
    #[error("InvalidFormat: Failed to detect image format")]
    InvalidFormat,
    #[error("IncorrectFormat: The image uses a format unsupported by this use case")]
    IncorrectFormat,
    #[error("ImageCorrupt: The image is corrupted and cannot be used")]
    ImageCorrupt,
    #[error("InvalidImageSize: This image is too big")]
    InvalidImageSize,
    #[error("InvalidAspectRatio: You need a different aspect ratio for this use case")]
    InvalidAspectRatio,
    #[error("TempImageNotFound: This image doesn't exist")]
    TempImageNotFound,
    #[error("IncorrectUploadType: Provided upload type doesn't match expected")]
    IncorrectUploadType,

    #[error("{0}")]
    Auth(#[from] AuthError),
    #[error("{0}")]
    Email(#[from] EmailError),
    #[error("{0}")]
    Level(#[from] LevelError),
    #[error("Rpc: An unknown error has occurred: {0}")]
    Rpc(#[from] Arc<RpcError>),
    #[error("Anyhow: An unknown error has occurred: {0}")]
    Anyhow(#[from] Arc<anyhow::Error>),
    #[error("Sqlx: Error communicating with the database")]
    Sqlx(#[from] Arc<sqlx::Error>),
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

impl From<sqlx::Error> for RespError {
    fn from(value: sqlx::Error) -> Self {
        Self::Sqlx(Arc::new(value))
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

pub struct LogErrorsMiddlewareFactory;

impl ExtensionFactory for LogErrorsMiddlewareFactory {
    fn create(&self) -> Arc<dyn Extension> {
        Arc::new(LogErrorsMiddleware)
    }
}
