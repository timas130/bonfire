mod backends;
mod context;

use tracing_subscriber::filter::LevelFilter;
use tracing_subscriber::fmt::format::FmtSpan;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use c_core::prelude::{anyhow, tokio};
use c_core::ServiceBase;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::builder()
            .with_default_directive(LevelFilter::INFO.into())
            .from_env_lossy())
        .with(tracing_subscriber::fmt::layer().with_span_events(FmtSpan::NEW | FmtSpan::CLOSE))
        .with(sentry::integrations::tracing::layer())
        .try_init()?;

    let base = ServiceBase::load().await?;
    let base1 = base.clone();

    sqlx::migrate!("../migrations").run(&base.pool).await?;

    #[cfg(not(debug_assertions))]
    let _guard = sentry::init((
        base.config.sentry_dsn.clone(),
        sentry::ClientOptions {
            release: sentry::release_name!(),
            traces_sample_rate: 1.0,
            ..Default::default()
        }
    ));

    backends::start_backends(base);

    Ok(())
}
