use crate::error::RespError;
use async_graphql::Context;
use async_trait::async_trait;
use aws_sdk_s3::config::{Credentials, Region};
use axum_extra::headers::UserAgent;
use axum_extra::TypedHeader;
use c_core::config::ImagesConfig;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tokio::sync::Mutex;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::auth::user::{AuthUser, PermissionLevel};
use c_core::services::auth::{Auth, AuthError, AuthServiceClient, UserContext};
use c_core::services::level::{Level, LevelServiceClient};
use c_core::services::notification::{NotificationServiceClient, Notifications};
use c_core::services::profile::{ProfileServiceClient, Profiles};
use c_core::services::security::{Security, SecurityServiceClient};
use c_core::ServiceBase;
use lru::LruCache;
use std::net::IpAddr;
use std::num::NonZeroUsize;
use std::sync::Arc;
use tracing::error;

#[derive(Clone)]
pub struct GlobalContext {
    pub base: Arc<ServiceBase>,
    pub auth: Arc<AuthServiceClient>,
    pub level: Arc<LevelServiceClient>,
    pub notification: Arc<NotificationServiceClient>,
    pub profile: Arc<ProfileServiceClient>,
    pub security: Arc<SecurityServiceClient>,

    pub online_cache: Arc<Mutex<LruCache<i64, DateTime<Utc>>>>,
}
impl GlobalContext {
    pub async fn new(base: ServiceBase) -> anyhow::Result<GlobalContext> {
        Ok(Self {
            auth: Arc::new(Auth::client_tcp(base.config.ports.auth).await?),
            level: Arc::new(Level::client_tcp(base.config.ports.level).await?),
            notification: Arc::new(
                Notifications::client_tcp(base.config.ports.notification).await?,
            ),
            profile: Arc::new(Profiles::client_tcp(base.config.ports.profile).await?),
            security: Arc::new(Security::client_tcp(base.config.ports.security).await?),
            base: Arc::new(base),

            online_cache: Arc::new(Mutex::new(LruCache::new(NonZeroUsize::new(10000).unwrap()))),
        })
    }
}

#[derive(Clone)]
pub struct ReqContext {
    pub base: Arc<ServiceBase>,
    pub auth: Arc<AuthServiceClient>,
    pub level: Arc<LevelServiceClient>,
    pub notification: Arc<NotificationServiceClient>,
    pub profile: Arc<ProfileServiceClient>,
    pub security: Arc<SecurityServiceClient>,
    pub online_cache: Arc<Mutex<LruCache<i64, DateTime<Utc>>>>,

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

        let user_context = UserContext {
            ip: addr,
            user_agent: user_agent
                .map(|header| header.to_string())
                .unwrap_or_else(|| String::from("[?]")),
        };

        // only allow having system permissions on localhost
        let user = user.filter(|(_, user)| {
            user.permission_level < PermissionLevel::System || user_context.is_internal()
        });

        Self {
            base: global_context.base,
            auth: global_context.auth,
            level: global_context.level,
            notification: global_context.notification,
            profile: global_context.profile,
            security: global_context.security,
            online_cache: global_context.online_cache,
            user_context,
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

    pub fn get_s3(&self) -> Result<(aws_sdk_s3::Client, &str), RespError> {
        let ImagesConfig::S3 {
            endpoint,
            bucket,
            key_id,
            key_secret,
            region,
        } = &self.base.config.images
        else {
            error!("misconfiguration: images config expected s3, but it's not");
            return Err(RespError::OutOfSync);
        };

        let config = aws_sdk_s3::Config::builder()
            .endpoint_url(endpoint)
            .credentials_provider(Credentials::new(key_id, key_secret, None, None, "Melior"))
            .region(Some(Region::new(region.clone())))
            .force_path_style(true)
            .build();
        let client = aws_sdk_s3::Client::from_conf(config);

        Ok((client, bucket))
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
