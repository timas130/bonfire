use async_graphql::extensions::{Extension, ExtensionContext, ExtensionFactory, NextExecute};
use async_graphql::Response;
use async_trait::async_trait;
use axum::http::{header, HeaderValue, StatusCode};
use axum::response::IntoResponse;
use axum::Json;
use c_core::prelude::anyhow;
use c_core::prelude::anyhow::Error;
use c_core::prelude::tarpc::client::RpcError;
use c_core::services::auth::AuthError;
use c_core::services::email::EmailError;
use c_core::services::level::LevelError;
use c_core::services::notification::NotificationError;
use c_core::services::profile::ProfileError;
use c_core::services::security::SecurityError;
use c_core::services::video::VideoError;
use serde_json::json;
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
    #[error("{0}")]
    Notification(#[from] NotificationError),
    #[error("{0}")]
    Profile(#[from] ProfileError),
    #[error("{0}")]
    Security(#[from] SecurityError),
    #[error("{0}")]
    Video(#[from] VideoError),
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

impl IntoResponse for RespError {
    fn into_response(self) -> axum::response::Response {
        let mut resp = Json(json!({
            "error": true,
            "errorMessage": self.to_string(),
        }))
        .into_response();

        *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;

        resp
    }
}

/// Error response of `/openid/token` as specified by RFC 6749
pub struct OAuthTokenError(RespError);

impl<E: Into<RespError>> From<E> for OAuthTokenError {
    fn from(value: E) -> Self {
        Self(value.into())
    }
}

impl IntoResponse for OAuthTokenError {
    fn into_response(self) -> axum::response::Response {
        let (status, code) = match &self.0 {
            RespError::Auth(
                AuthError::MissingRequiredParameter(_) | AuthError::ParameterTooLong(_),
            ) => (StatusCode::BAD_REQUEST, "invalid_request"),
            RespError::Auth(AuthError::OAuthClientNotFound | AuthError::InvalidClientSecret) => {
                (StatusCode::UNAUTHORIZED, "invalid_client")
            }
            RespError::Auth(
                AuthError::FlowNotFound
                | AuthError::InvalidToken
                | AuthError::InvalidRedirectUri
                | AuthError::UnsupportedCodeChallengeMethod,
            ) => (StatusCode::BAD_REQUEST, "invalid_grant"),
            RespError::Auth(AuthError::UnsupportedResponseType) => {
                (StatusCode::BAD_REQUEST, "unsupported_grant_type")
            }
            _ => (StatusCode::INTERNAL_SERVER_ERROR, "server_error"),
        };

        let mut resp = oauth_error_response(status, code, &self.0);
        if status == StatusCode::UNAUTHORIZED {
            resp.headers_mut()
                .insert(header::WWW_AUTHENTICATE, HeaderValue::from_static("Basic"));
        }
        resp
    }
}

/// Error response of `/openid/userinfo` as specified by RFC 6750
pub struct OAuthUserinfoError(RespError);

impl<E: Into<RespError>> From<E> for OAuthUserinfoError {
    fn from(value: E) -> Self {
        Self(value.into())
    }
}

impl IntoResponse for OAuthUserinfoError {
    fn into_response(self) -> axum::response::Response {
        if !matches!(self.0, RespError::Auth(AuthError::InvalidToken)) {
            return oauth_error_response(
                StatusCode::INTERNAL_SERVER_ERROR,
                "server_error",
                &self.0,
            );
        }

        let mut resp = oauth_error_response(StatusCode::UNAUTHORIZED, "invalid_token", &self.0);
        resp.headers_mut().insert(
            header::WWW_AUTHENTICATE,
            HeaderValue::from_static("Bearer error=\"invalid_token\""),
        );
        resp
    }
}

fn oauth_error_response(
    status: StatusCode,
    code: &str,
    error: &RespError,
) -> axum::response::Response {
    let message = error.to_string();
    let body = Json(json!({
        "error": code,
        "error_description": message,
        "errorMessage": message,
    }));
    (status, body).into_response()
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
