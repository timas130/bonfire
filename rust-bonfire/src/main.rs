use crate::context::GlobalContext;
use crate::routes::daily_task::check_in::dt_check_in;
use crate::routes::daily_task::fandom_chances::get_dt_fandom_choices;
use crate::routes::daily_task::task::get_daily_task;
use crate::routes::recount_level::recount_level_route;
use crate::secrets::SecretsConfig;
use axum::extract::State;
use axum::headers::authorization::Basic;
use axum::headers::Authorization;
use axum::http::{Request, StatusCode};
use axum::middleware::Next;
use axum::response::Response;
use axum::routing::{get, post};
use axum::{middleware, Extension, Router, TypedHeader};
use sqlx::postgres::{PgConnectOptions, PgPoolOptions};
use std::sync::Arc;
use tracing::info;

pub mod consts;
pub mod context;
pub mod mechanics;
pub mod models;
pub mod routes;
pub mod secrets;

async fn internal_auth_middleware<B>(
    State(global_context): State<GlobalContext>,
    auth: TypedHeader<Authorization<Basic>>,
    request: Request<B>,
    next: Next<B>,
) -> Result<Response, StatusCode> {
    // doesn't that name sound cool?
    if auth.username() != "J_SYSTEM" {
        return Err(StatusCode::FORBIDDEN);
    }

    if auth.password() != global_context.secrets.keys.internal_key {
        return Err(StatusCode::FORBIDDEN);
    }

    Ok(next.run(request).await)
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();

    let secrets = SecretsConfig::load()?;

    let options = PgConnectOptions::new()
        .username(&secrets.config.database_login)
        .password(&secrets.config.database_password)
        .database(&secrets.config.database_name)
        .host(&secrets.config.database_address);
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect_with(options)
        .await?;

    let global_context = GlobalContext {
        secrets: Arc::new(secrets),
        pool,
    };

    let app = Router::new()
        .route("/user/:id/recount-level", get(recount_level_route))
        .route("/user/:id/dt/fandoms", get(get_dt_fandom_choices))
        .route("/user/:id/dt/task", get(get_daily_task))
        .route("/user/:id/dt/check-in", post(dt_check_in))
        .route_layer(middleware::from_fn_with_state(
            global_context.clone(),
            internal_auth_middleware,
        ))
        .layer(Extension(global_context));

    info!("listening on 0.0.0.0:51681");
    axum::Server::bind(&"0.0.0.0:51681".parse().unwrap())
        .serve(app.into_make_service())
        .await?;

    Ok(())
}
