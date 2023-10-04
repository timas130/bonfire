use c_core::ServiceBase;
use c_core::services::auth::AuthService;
use c_core::services::email::EmailService;
use c_core::services::level::LevelService;

macro_rules! start_svc {
    ($base: expr, $server: ty) => {
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
    };
}

pub fn start_backends(base: ServiceBase) {
    start_svc!(base, EmailService);
    start_svc!(base, AuthService);
    start_svc!(base, LevelService);
}
