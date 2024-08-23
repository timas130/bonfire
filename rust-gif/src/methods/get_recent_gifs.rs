use crate::methods::search_gif::{convert_post, MEDIA_FILTER};
use crate::GifServer;
use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::gif::{GifContext, GifError, GifItem};
use itertools::Itertools;

impl GifServer {
    pub(crate) async fn _get_recent_gifs(
        &self,
        gif_context: GifContext,
    ) -> Result<Vec<GifItem>, GifError> {
        let gif_ids = sqlx::query_scalar!(
            "select gif_id from gif_shares \
             where user_id = $1 \
             group by gif_id \
             order by max(created_at) desc \
             limit 15",
            gif_context.user_id,
        )
        .fetch_all(&self.base.pool)
        .await?;

        if gif_ids.is_empty() {
            return Ok(vec![]);
        }

        let resp = self
            .tenor
            .posts()
            .list()
            .delegate(&mut self.get_delegate())
            .country(&gif_context.country)
            .locale(match &gif_context.locale {
                None => "",
                Some(s) => s,
            })
            .media_filter(MEDIA_FILTER)
            .client_key("melior")
            .ids(gif_ids.iter().join(",").as_str())
            .doit()
            .instrument(span!(
                Level::INFO,
                "getting tenor recent gifs",
                count = gif_ids.len()
            ))
            .await;

        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(?err, count = gif_ids.len(), "failed getting recent gifs");

                return Err(GifError::UpstreamError);
            }
        };

        Ok(resp
            .results
            .unwrap()
            .into_iter()
            .map(convert_post)
            .collect())
    }
}
