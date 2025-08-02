pub mod auth;
pub mod email;
pub mod images;
pub mod level;
pub mod notification;
pub mod profile;
pub mod security;

#[macro_export]
macro_rules! host_tcp {
    ($service: ident) => {
        pub async fn host_tcp(&mut self) -> c_core::prelude::anyhow::Result<()> {
            use c_core::prelude::futures::StreamExt;
            use c_core::prelude::tarpc::serde_transport::tcp;
            use c_core::prelude::tarpc::server::{BaseChannel, Channel};
            use c_core::prelude::tarpc::tokio_serde::formats::Json;
            use c_core::prelude::*;
            use c_core::ServiceBase;
            use std::future::Future;
            use std::net::{IpAddr, Ipv4Addr};

            async fn spawn(fut: impl Future<Output = ()> + Send + 'static) {
                tokio::spawn(fut);
            }

            let server_addr = (
                IpAddr::V4(Ipv4Addr::LOCALHOST),
                self.base.config.ports.$service,
            );
            let mut listener = tcp::listen(&server_addr, Json::default).await?;
            listener.config_mut().max_frame_length(50 * 1024 * 1024);
            listener
                .filter_map(|r| futures::future::ready(r.ok()))
                .map(BaseChannel::with_defaults)
                .map(|channel| {
                    let server = self.clone();
                    channel.execute(server.serve()).for_each(spawn)
                })
                .buffer_unordered(100)
                .for_each(|_| async {})
                .await;
            Ok(())
        }
    };
}

#[macro_export]
macro_rules! client_tcp {
    ($client: ty) => {
        /// Connect to a [`$client`] server on `port`
        pub async fn client_tcp(port: u16) -> anyhow::Result<$client> {
            use std::iter;
            use std::net::{IpAddr, Ipv4Addr};
            use std::time::Duration;
            use stubborn_io::{ReconnectOptions, StubbornTcpStream};
            use tarpc::client;
            use tarpc::serde_transport::Transport;
            use tarpc::tokio_serde::formats::Json;

            let server_addr = (IpAddr::V4(Ipv4Addr::LOCALHOST), port);
            let socket_opts = ReconnectOptions::new()
                .with_exit_if_first_connect_fails(false)
                .with_retries_generator(|| iter::repeat(Duration::from_millis(300)));
            let stream = StubbornTcpStream::connect_with_options(server_addr, socket_opts).await?;

            let transport = Transport::from((stream, Json::default()));
            let client = <$client>::new(client::Config::default(), transport).spawn();

            Ok(client)
        }
    };
}
