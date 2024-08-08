use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::gif::{GifContext, GifError};
use crate::GifServer;

impl GifServer {
    pub(crate) async fn _get_search_suggestions(&self, query: String, gif_context: GifContext) -> Result<Vec<String>, GifError> {
        if !query.is_empty() {
            self.tenor_search_suggestions(query, gif_context).await
        } else {
            self.tenor_categories(gif_context).await
        }
    }
    
    async fn tenor_search_suggestions(&self, query: String, gif_context: GifContext) -> Result<Vec<String>, GifError> {
        let resp = self.tenor
            .methods()
            .search_suggestions()
            .delegate(&mut self.get_delegate())
            .q(&query)
            .country(&gif_context.country)
            .locale(match &gif_context.locale {
                None => "",
                Some(s) => s,
            })
            .client_key("melior")
            .limit(20)
            .doit()
            .instrument(span!(Level::INFO, "getting tenor search suggestions"))
            .await;
        
        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(query, ?err, "error getting tenor search suggestions");
                
                return Err(GifError::UpstreamError);
            }
        };
        
        Ok(resp.results.unwrap())
    }
    
    async fn tenor_categories(&self, gif_context: GifContext) -> Result<Vec<String>, GifError> {
        let resp = self.tenor
            .methods()
            .categories()
            .delegate(&mut self.get_delegate())
            .country(&gif_context.country)
            .locale(match &gif_context.locale {
                None => "",
                Some(s) => s,
            })
            .contentfilter("low")
            .client_key("melior")
            .doit()
            .instrument(span!(Level::INFO, "getting tenor search categories"))
            .await;

        let (_, resp) = match resp {
            Ok(resp) => resp,
            Err(err) => {
                warn!(?err, "error getting tenor search categories");

                return Err(GifError::UpstreamError);
            }
        };
        
        Ok(resp.tags
            .unwrap()
            .into_iter()
            .map(|cat| cat.name.unwrap())
            .collect())
    }
}
