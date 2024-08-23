use crate::GifServer;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tokio;
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
        if id.parse::<i64>().is_err() {
            return Err(GifError::InvalidRequest);
        }
        if query.as_ref().map(String::len).unwrap_or(0) > 256 {
            return Err(GifError::InvalidRequest);
        }

        sqlx::query!(
            "insert into gif_shares (user_id, gif_id, search_query, country, locale) \
             values ($1, $2, $3, $4, $5)",
            context.user_id,
            &id,
            query.as_ref(),
            &context.country,
            context.locale.as_ref(),
        )
        .execute(&self.base.pool)
        .await?;

        sqlx::query!(
            "update gif_favourites set last_used_at = now() \
             where user_id = $1 and gif_id = $2",
            context.user_id,
            &id,
        )
        .execute(&self.base.pool)
        .await?;

        let req = GoogleSearchTenorV2RegisterShareRequest {
            id: Some(id.parse().map_err(|_| anyhow!("invalid id format"))?),
            country: Some(context.country),
            locale: context.locale,
            q: query,
            ..Default::default()
        };
        let mut delegate = self.get_delegate();
        let tenor = self.tenor.clone();

        tokio::spawn(async move {
            let resp = tenor
                .methods()
                .registershare(req)
                .delegate(&mut delegate)
                .doit()
                .instrument(span!(Level::INFO, "registering tenor share", id))
                .await;

            if let Err(err) = resp {
                warn!(id, ?err, "failed to register tenor share");
            }
        });

        Ok(())
    }
}
