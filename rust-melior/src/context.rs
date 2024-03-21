use crate::error::RespError;
use async_graphql::Context;
use async_trait::async_trait;
use aws_config::SdkConfig;
use aws_sdk_s3::config::{Credentials, SharedCredentialsProvider};
use axum_extra::headers::UserAgent;
use axum_extra::TypedHeader;
use c_core::config::ImagesConfig;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::auth::user::{AuthUser, PermissionLevel};
use c_core::services::auth::{Auth, AuthError, AuthServiceClient, UserContext};
use c_core::services::level::{Level, LevelServiceClient};
use c_core::ServiceBase;
use std::net::IpAddr;
use std::sync::Arc;

#[derive(Clone)]
pub struct GlobalContext {
    pub base: Arc<ServiceBase>,
    pub auth: Arc<AuthServiceClient>,
    pub level: Arc<LevelServiceClient>,
}
impl GlobalContext {
    pub async fn new(base: ServiceBase) -> anyhow::Result<GlobalContext> {
        Ok(Self {
            auth: Arc::new(Auth::client_tcp(base.config.ports.auth).await?),
            level: Arc::new(Level::client_tcp(base.config.ports.level).await?),
            base: Arc::new(base),
        })
    }
}

#[derive(Clone)]
pub struct ReqContext {
    pub base: Arc<ServiceBase>,
    pub auth: Arc<AuthServiceClient>,
    pub level: Arc<LevelServiceClient>,
    pub user_context: UserContext,
    pub session_id: Option<i64>,
    pub user: Option<AuthUser>,
    pub user_auth_error: Option<AuthError>,
    pub access_token: Option<String>,
}
impl ReqContext {
    pub async fn new(
        global_context: GlobalContext,
        token: Option<String>,
        addr: IpAddr,
        user_agent: Option<TypedHeader<UserAgent>>,
    ) -> Self {
        let mut user_auth_error = None;
        let user: Option<(i64, AuthUser)> = match token.clone() {
            Some(token) => {
                match global_context
                    .auth
                    .get_by_token(tarpc::context::current(), token)
                    .await
                {
                    Ok(Ok(tuple)) => Some(tuple),
                    Ok(Err(auth_error)) => {
                        user_auth_error = Some(auth_error);
                        None
                    }
                    Err(_) => None,
                }
            }
            None => None,
        };

        Self {
            base: global_context.base,
            auth: global_context.auth,
            level: global_context.level,
            user_context: UserContext {
                ip: addr,
                user_agent: user_agent
                    .map(|header| header.to_string())
                    .unwrap_or_else(|| String::from("[?]")),
            },
            session_id: user.as_ref().map(|(session_id, _)| *session_id),
            user: user.map(|(_, auth)| auth),
            user_auth_error,
            access_token: token,
        }
    }

    pub fn require_user(&self) -> Result<&AuthUser, AuthError> {
        let Some(user) = &self.user else {
            return Err(self
                .user_auth_error
                .clone()
                .unwrap_or(AuthError::Unauthenticated));
        };
        Ok(user)
    }

    pub fn get_s3(&self) -> Result<(aws_sdk_s3::Client, String), RespError> {
        let ImagesConfig::S3 {
            endpoint,
            key_id,
            key_secret,
            bucket,
        } = &self.base.config.images
        else {
            return Err(anyhow!("invalid images storage provider").into());
        };

        let config = SdkConfig::builder()
            .endpoint_url(endpoint)
            .credentials_provider(SharedCredentialsProvider::new(Credentials::new(
                key_id,
                key_secret,
                None,
                None,
                "CoreConfig",
            )))
            .build();
        let client = aws_sdk_s3::Client::new(&config);

        Ok((client, bucket.clone()))
    }
}

#[async_trait]
pub trait ContextExt {
    fn has_permission_level<T: Into<PermissionLevel>>(&self, level: T) -> bool;
    fn require_permission_level<T: Into<PermissionLevel>>(&self, level: T)
        -> Result<(), AuthError>;
}

#[async_trait]
impl ContextExt for Context<'_> {
    #[must_use]
    fn has_permission_level<T: Into<PermissionLevel>>(&self, level: T) -> bool {
        let req = self.data_unchecked::<ReqContext>();

        matches!(&req.user, Some(user) if user.permission_level >= level.into())
    }

    fn require_permission_level<T: Into<PermissionLevel>>(
        &self,
        level: T,
    ) -> Result<(), AuthError> {
        if self.has_permission_level(level) {
            Ok(())
        } else {
            Err(AuthError::AccessDenied)
        }
    }
}
