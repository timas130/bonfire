mod backends;
mod context;
mod data_loaders;
mod error;
mod models;
mod schema;
pub(crate) mod utils;

use crate::context::{GlobalContext, ReqContext};
use crate::data_loaders::AuthUserLoader;
use async_graphql::http::GraphiQLSource;
use async_graphql::{EmptySubscription, Schema};
use async_graphql_axum::{GraphQLRequest, GraphQLResponse};
use axum::extract::ConnectInfo;
use axum::headers::authorization::Bearer;
use axum::headers::{Authorization, UserAgent};
use axum::response::IntoResponse;
use axum::routing::get;
use axum::{response, Extension, Router, Server, TypedHeader};
use c_core::prelude::{anyhow, tokio};
use c_core::ServiceBase;
use sentry_tower::{NewSentryLayer, SentryHttpLayer};
use std::net::SocketAddr;
use axum_client_ip::XForwardedFor;
use tower_http::cors::CorsLayer;
use tracing_subscriber::filter::LevelFilter;
use tracing_subscriber::fmt::format::FmtSpan;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use crate::error::LogErrorsMiddleware;

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
    let req_context = ReqContext::new(
        global_context,
        auth_header.map(|header| header.token().to_string()),
        forwarded_for.0.first().cloned().unwrap_or(addr.ip()),
        user_agent,
    )
    .await;

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
    .finish();

    let app = Router::new()
        .route("/", get(graphiql).post(graphql_handler))
        .layer(NewSentryLayer::new_from_top())
        .layer(SentryHttpLayer::with_transaction())
        .layer(CorsLayer::permissive())
        .layer(Extension(global_context))
        .layer(Extension(schema))
        .layer(Extension(LogErrorsMiddleware));

    Server::bind(&"0.0.0.0:8000".parse().unwrap())
        .serve(app.into_make_service_with_connect_info::<SocketAddr>())
        .await?;

    Ok(())
}
