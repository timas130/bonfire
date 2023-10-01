//! Utilities for two-way [`tarpc`] transport.

use futures::future::{AbortHandle, Abortable};
use futures::{Sink, SinkExt, Stream, StreamExt, TryFutureExt, TryStreamExt};
use tarpc::transport::channel::{ChannelError, UnboundedChannel};
use thiserror::Error;
use tokio::io;
use tracing::debug;
use tracing::log::warn;

/// A [`tarpc`] message that can be either a request or a response.
#[derive(serde::Serialize, serde::Deserialize)]
pub enum TwoWayMessage<Req, Resp> {
    Request(tarpc::ClientMessage<Req>),
    Response(tarpc::Response<Resp>),
}

#[derive(Error, Debug)]
pub enum ChannelOrIoError {
    #[error("{0}")]
    ChannelError(#[from] ChannelError),
    #[error("{0}")]
    IoError(#[from] io::Error),
}

/// Returns two transports that multiplex over the given transport.
/// The first transport can be used by a server: it receives requests and sends back responses.
/// The second transport can be used by a client: it sends requests and receives back responses.
pub fn spawn_twoway<Req1, Resp1, Req2, Resp2, T>(
    transport: T,
) -> (
    UnboundedChannel<tarpc::ClientMessage<Req1>, tarpc::Response<Resp1>>,
    UnboundedChannel<tarpc::Response<Resp2>, tarpc::ClientMessage<Req2>>,
)
where
    T: Stream<Item = Result<TwoWayMessage<Req1, Resp2>, io::Error>>,
    T: Sink<TwoWayMessage<Req2, Resp1>, Error = io::Error>,
    T: Unpin + Send + 'static,
    Req1: Send + 'static,
    Resp1: Send + 'static,
    Req2: Send + 'static,
    Resp2: Send + 'static,
{
    let (server, server_ret) = tarpc::transport::channel::unbounded();
    let (client, client_ret) = tarpc::transport::channel::unbounded();
    let (mut server_sink, server_stream) = server.split();
    let (mut client_sink, client_stream) = client.split();
    let (transport_sink, mut transport_stream) = transport.split();

    let (abort_handle, abort_registration) = AbortHandle::new_pair();

    // Task for inbound message handling
    tokio::spawn(async move {
        let e: Result<(), ChannelOrIoError> = async move {
            while let Some(msg) = transport_stream.next().await {
                match msg? {
                    TwoWayMessage::Request(req) => server_sink.send(req).await?,
                    TwoWayMessage::Response(resp) => client_sink.send(resp).await?,
                }
            }
            Ok(())
        }
        .await;

        match e {
            Ok(()) => debug!("transport_stream done"),
            Err(e) => warn!("Error in inbound multiplexing: {}", e),
        }

        abort_handle.abort();
    });

    let abortable_sink_channel = Abortable::new(
        futures::stream::select(
            server_stream.map_ok(TwoWayMessage::Response),
            client_stream.map_ok(TwoWayMessage::Request),
        )
        .map_err(ChannelOrIoError::ChannelError),
        abort_registration,
    );

    // Task for outbound message handling
    tokio::spawn(
        abortable_sink_channel
            .forward(transport_sink.sink_map_err(ChannelOrIoError::IoError))
            .inspect_ok(|_| debug!("transport_sink done"))
            .inspect_err(|e| warn!("Error in outbound multiplexing: {}", e)),
    );

    (server_ret, client_ret)
}
