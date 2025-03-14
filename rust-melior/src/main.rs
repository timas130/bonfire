mod backends;
mod context;
mod data_loaders;
mod error;
mod models;
mod oauth;
mod schema;
pub(crate) mod utils;

use crate::context::{GlobalContext, ReqContext};
use crate::data_loaders::AuthUserLoader;
use crate::error::LogErrorsMiddlewareFactory;
use crate::oauth::authorize::oauth2_authorize;
use crate::oauth::jwks::oauth2_jwk_set;
use crate::oauth::openid_configuration::openid_configuration;
use crate::oauth::token::oauth2_token;
use crate::oauth::userinfo::oauth2_userinfo;
use async_graphql::http::GraphiQLSource;
use async_graphql::{EmptySubscription, Schema};
use async_graphql_axum::{GraphQLRequest, GraphQLResponse};
use axum::extract::ConnectInfo;
use axum::response::IntoResponse;
use axum::routing::{get, post};
use axum::{response, Extension, Router};
use axum_client_ip::XForwardedFor;
use axum_extra::headers::authorization::Bearer;
use axum_extra::headers::{Authorization, UserAgent};
use axum_extra::TypedHeader;
use c_core::prelude::chrono::{TimeDelta, Utc};
use c_core::prelude::tokio::net::TcpListener;
use c_core::prelude::{anyhow, tarpc, tokio};
use c_core::services::auth::user::PermissionLevel;
use c_core::ServiceBase;
use sentry_tower::{NewSentryLayer, SentryHttpLayer};
use std::net::SocketAddr;
use tower_http::cors::CorsLayer;
use tracing_subscriber::filter::LevelFilter;
use tracing_subscriber::fmt::format::FmtSpan;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;

type BSchema = Schema<schema::Query, schema::Mutation, EmptySubscription>;

async fn graphiql() -> impl IntoResponse {
    response::Html(
        GraphiQLSource::build()
            .endpoint("/")
            .subscription_endpoint("/ws")
            .finish(),
    )
}

async fn graphql_handler(
    Extension(schema): Extension<BSchema>,
    Extension(global_context): Extension<GlobalContext>,
    auth_header: Option<TypedHeader<Authorization<Bearer>>>,
    forwarded_for: XForwardedFor,
    ConnectInfo(addr): ConnectInfo<SocketAddr>,
    user_agent: Option<TypedHeader<UserAgent>>,
    req: GraphQLRequest,
) -> GraphQLResponse {
    // authenticating user and getting full req context
    let req_context = ReqContext::new(
        global_context,
        auth_header.map(|header| header.token().to_string()),
        forwarded_for.0.first().cloned().unwrap_or(addr.ip()),
        user_agent,
    )
    .await;

    // marking user as online
    if let Some(ref access_token) = req_context.access_token {
        if let Some(ref user) = req_context.user {
            // don't mark service accounts as online
            if user.permission_level < PermissionLevel::System {
                let auth = req_context.auth.clone();
                let access_token = access_token.clone();
                let user_id = user.id;
                let online_cache = req_context.online_cache.clone();

                tokio::spawn(async move {
                    let mut online_cache = online_cache.lock().await;

                    // ratelimit updates at 3 minutes (online period is 5 minutes)
                    let last_online = online_cache.get(&user_id);
                    if let Some(last_online) = last_online {
                        if Utc::now().signed_duration_since(last_online) < TimeDelta::minutes(3) {
                            return;
                        }
                    }

                    // record update time
                    online_cache.put(user_id, Utc::now());

                    // unlock mutex
                    drop(online_cache);

                    let _ = auth
                        .mark_online(tarpc::context::current(), access_token.clone())
                        .await;
                });
            }
        }
    }

    schema
        .execute(req.into_inner().data(req_context))
        .await
        .into()
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::builder()
                .with_default_directive(LevelFilter::INFO.into())
                .from_env_lossy(),
        )
        .with(tracing_subscriber::fmt::layer().with_span_events(FmtSpan::NEW | FmtSpan::CLOSE))
        .with(sentry::integrations::tracing::layer())
        .try_init()?;

    let base = ServiceBase::load().await?;

    sqlx::migrate!("../migrations").run(&base.pool).await?;

    #[cfg(not(debug_assertions))]
    let _guard = sentry::init((
        base.config.sentry_dsn.clone(),
        sentry::ClientOptions {
            release: sentry::release_name!(),
            traces_sample_rate: 1.0,
            ..Default::default()
        },
    ));

    backends::start_backends(&base);

    let global_context = GlobalContext::new(base).await?;

    let schema = BSchema::build(
        schema::Query::default(),
        schema::Mutation::default(),
        EmptySubscription,
    )
    .data(AuthUserLoader::data_loader(global_context.clone()))
    .extension(LogErrorsMiddlewareFactory)
    .finish();

    let app = Router::new()
        .route("/", get(graphiql).post(graphql_handler))
        .route(
            "/.well-known/openid-configuration",
            get(openid_configuration),
        )
        .route("/openid/authorize", get(oauth2_authorize))
        .route("/openid/jwks", get(oauth2_jwk_set))
        .route("/openid/token", post(oauth2_token))
        .route(
            "/openid/userinfo",
            get(oauth2_userinfo).post(oauth2_userinfo),
        )
        .layer(NewSentryLayer::new_from_top())
        .layer(SentryHttpLayer::with_transaction())
        .layer(CorsLayer::permissive())
        .layer(Extension(global_context))
        .layer(Extension(schema));

    let listener = TcpListener::bind("0.0.0.0:8000").await?;
    axum::serve(
        listener,
        app.into_make_service_with_connect_info::<SocketAddr>(),
    )
    .await?;

    Ok(())
}
