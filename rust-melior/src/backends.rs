use b_level::LevelServer;
use b_notification::NotificationServer;
use b_profile::ProfileServer;
use c_auth::AuthServer;
use c_core::ServiceBase;
use c_email::EmailServer;

macro_rules! start_svc {
    ($base: expr, $server: ty) => {{
        use c_core::prelude::tokio;
        use tracing::{error, info};

        let base1 = $base.clone();
        tokio::spawn(async {
            let server = <$server>::with_base(base1).await;
            let mut server = match server {
                Ok(server) => server,
                Err(err) => {
                    error!("failed to start {}: {err:?}", stringify!($server));
                    std::process::exit(1);
                }
            };

            info!("starting {}", stringify!($server));
            let result = server.host_tcp().await;
            error!("{} stopped! exit(1): {result:?}", stringify!($server));
            std::process::exit(1);
        });
    }};
}

pub fn start_backends(base: &ServiceBase) {
    start_svc!(base, EmailServer);
    start_svc!(base, AuthServer);
    start_svc!(base, LevelServer);
    start_svc!(base, NotificationServer);
    start_svc!(base, ProfileServer);
}
