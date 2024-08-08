use crate::{ApiKeyDelegate, GifServer};
use c_core::page_info::{Edge, PageInfo, Paginated};
use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::gif::{GifContext, GifError, GifItem, GifMediaFormats, GifMediaRef};
use google_tenor2::api::{GoogleSearchTenorV2MediaType, GoogleSearchTenorV2PostResult};

const MEDIA_FILTER: &str = "preview,gif,tinygif,mp4,tinymp4";

impl GifServer {
    pub(crate) fn get_delegate(&self) -> ApiKeyDelegate {
        ApiKeyDelegate::new(self.base.config.google.api_key.clone())
    }

    pub(crate) async fn _search_gif(
        &self,
        query: String,
        after: Option<String>,
        context: GifContext,
    ) -> Result<Paginated<GifItem, String>, GifError> {
        if !query.is_empty() {
            self.tenor_gif_search(query, after, context).await
        } else {
            self.tenor_featured_gifs(after, context).await
        }
    }

    async fn tenor_gif_search(
        &self,
        query: String,
        after: Option<String>,
        context: GifContext,
    ) -> Result<Paginated<GifItem, String>, GifError> {
        let resp = self
            .tenor
            .methods()
            .search()
            .delegate(&mut self.get_delegate())
            .q(&query)
            .country(&context.country)
            .locale(match &context.locale {
                None => "",
                Some(s) => s,
            })
            .media_filter(MEDIA_FILTER)
            .contentfilter("low")
            .client_key("melior")
            .limit(20)
            .pos(match &after {
                None => "",
                Some(s) => s,
            })
            .doit()
            .instrument(span!(Level::INFO, "searching tenor for gifs", query))
            .await;

        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(query, ?context, error = ?err, "error during tenor.search");

                return Err(GifError::UpstreamError);
            }
        };

        let results = resp
            .results
            .expect("no results from tenor")
            .into_iter()
            .map(convert_post)
            .collect();
        let next = resp.next.unwrap_or_default();

        Ok(Paginated {
            edges: Edge::map_indexed(results, |idx, _| {
                if idx == 0 {
                    after.clone().unwrap_or_default()
                } else {
                    next.clone()
                }
            }),
            page_info: PageInfo {
                has_next_page: !next.is_empty(),
                has_previous_page: false,
                start_cursor: after,
                end_cursor: Some(next),
            },
        })
    }

    async fn tenor_featured_gifs(
        &self,
        after: Option<String>,
        context: GifContext,
    ) -> Result<Paginated<GifItem, String>, GifError> {
        let resp = self
            .tenor
            .methods()
            .featured()
            .delegate(&mut self.get_delegate())
            .country(&context.country)
            .locale(match &context.locale {
                None => "",
                Some(s) => s,
            })
            .media_filter(MEDIA_FILTER)
            .contentfilter("low")
            .client_key("melior")
            .limit(20)
            .pos(match &after {
                None => "",
                Some(s) => s,
            })
            .doit()
            .instrument(span!(Level::INFO, "getting tenor featured gifs"))
            .await;

        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(?context, error = ?err, "error during tenor.featured");

                return Err(GifError::UpstreamError);
            }
        };

        let results = resp
            .results
            .unwrap()
            .into_iter()
            .map(convert_post)
            .collect();
        let next = resp.next.unwrap_or_default();

        Ok(Paginated {
            edges: Edge::map_indexed(results, |idx, _| {
                if idx == 0 {
                    after.clone().unwrap_or_default()
                } else {
                    next.clone()
                }
            }),
            page_info: PageInfo {
                has_next_page: !next.is_empty(),
                has_previous_page: false,
                start_cursor: after,
                end_cursor: Some(next),
            },
        })
    }
}

fn convert_post(value: GoogleSearchTenorV2PostResult) -> GifItem {
    let mut media_formats = value.media_formats.unwrap();
    GifItem {
        id: value.id.unwrap().to_string(),
        browser_url: value.itemurl.unwrap(),
        media: GifMediaFormats {
            preview: convert_media_type(media_formats.remove("preview").unwrap()),
            gif: convert_media_type(media_formats.remove("gif").unwrap()),
            tiny_gif: convert_media_type(media_formats.remove("tinygif").unwrap()),
            mp4: convert_media_type(media_formats.remove("mp4").unwrap()),
            tiny_mp4: convert_media_type(media_formats.remove("tinymp4").unwrap()),
        },
    }
}
fn convert_media_type(value: GoogleSearchTenorV2MediaType) -> GifMediaRef {
    GifMediaRef {
        url: value.url.unwrap(),
        resolution: value.dims.unwrap().try_into().unwrap(),
    }
}
