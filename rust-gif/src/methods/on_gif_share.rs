use crate::GifServer;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::gif::{GifContext, GifError};
use google_tenor2::api::GoogleSearchTenorV2RegisterShareRequest;

impl GifServer {
    pub(crate) async fn _on_gif_share(
        &self,
        id: String,
        query: Option<String>,
        context: GifContext,
    ) -> Result<(), GifError> {
        let req = GoogleSearchTenorV2RegisterShareRequest {
            id: Some(id.parse().map_err(|_| anyhow!("invalid id format"))?),
            country: Some(context.country),
            locale: context.locale,
            q: query,
            ..Default::default()
        };

        let resp = self
            .tenor
            .methods()
            .registershare(req)
            .delegate(&mut self.get_delegate())
            .doit()
            .instrument(span!(Level::INFO, "registering tenor share", id))
            .await;

        match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(id, error = ?err, "failed to register tenor share");

                return Err(GifError::UpstreamError);
            }
        };

        Ok(())
    }
}
