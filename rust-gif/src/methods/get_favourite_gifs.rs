use crate::methods::search_gif::{convert_post, MEDIA_FILTER};
use crate::GifServer;
use c_core::page_info::{Edge, PageInfo, Paginated};
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::gif::{GifContext, GifError, GifItem};
use itertools::Itertools;
use std::collections::HashMap;

impl GifServer {
    pub(crate) async fn _get_favourite_gifs(
        &self,
        gif_context: GifContext,
        after: Option<DateTime<Utc>>,
    ) -> Result<Paginated<GifItem, DateTime<Utc>>, GifError> {
        let gif_ids = sqlx::query!(
            "select gif_id, last_used_at from gif_favourites \
             where user_id = $1 and ($2::timestamptz is null or last_used_at < $2) \
             order by last_used_at \
             limit 50",
            gif_context.user_id,
            after,
        )
        .fetch_all(&self.base.pool)
        .await?;

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
            .ids(gif_ids.iter().map(|x| x.gif_id.as_str()).join(",").as_str())
            .doit()
            .instrument(span!(
                Level::INFO,
                "getting tenor favourite gifs",
                count = gif_ids.len()
            ))
            .await;

        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(
                    ?err,
                    count = gif_ids.len(),
                    "failed getting tenor favourite gifs"
                );

                return Err(GifError::UpstreamError);
            }
        };

        let resp = resp
            .results
            .unwrap()
            .into_iter()
            .map(convert_post)
            .collect::<Vec<GifItem>>();

        let cursors = gif_ids
            .into_iter()
            .map(|x| (x.gif_id, x.last_used_at))
            .collect::<HashMap<String, DateTime<Utc>>>();

        let has_next = if let Some(last_used_at) = resp.last().and_then(|x| cursors.get(&x.id)) {
            let id = sqlx::query_scalar!(
                "select gif_id from gif_favourites where last_used_at < $1",
                last_used_at
            )
            .fetch_optional(&self.base.pool)
            .await?;

            id.is_some()
        } else {
            false
        };

        Ok(Paginated {
            page_info: PageInfo {
                has_next_page: has_next,
                has_previous_page: false,
                start_cursor: resp.first().and_then(|x| cursors.get(&x.id).cloned()),
                end_cursor: resp.last().and_then(|x| cursors.get(&x.id).cloned()),
            },
            edges: Edge::map(resp, |x| {
                cursors
                    .get(&x.id)
                    .expect("tenor returned extra ids")
                    .clone()
            }),
        })
    }
}
