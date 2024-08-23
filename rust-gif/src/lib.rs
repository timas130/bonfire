mod methods;

use c_core::page_info::Paginated;
use c_core::prelude::chrono::{DateTime, Utc};
use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::gif::{GifContext, GifError, GifItem, GifService};
use c_core::{google_api, host_tcp, ServiceBase};
use google_tenor2::hyper::client::HttpConnector;
use google_tenor2::hyper_rustls::HttpsConnector;
use google_tenor2::{Delegate, Tenor};
use std::sync::Arc;

#[derive(Clone)]
pub struct GifServer {
    base: ServiceBase,

    tenor: Arc<Tenor<HttpsConnector<HttpConnector>>>,
}
impl GifServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        let tenor = Arc::new(google_api!(base, google_tenor2, Tenor));

        Ok(Self { base, tenor })
    }

    host_tcp!(gif);
}

pub(crate) struct ApiKeyDelegate(Option<String>);
impl ApiKeyDelegate {
    pub fn new(api_key: String) -> Self {
        Self(Some(api_key))
    }
}
impl Delegate for ApiKeyDelegate {
    fn api_key(&mut self) -> Option<String> {
        self.0.take()
    }
}

#[tarpc::server]
impl GifService for GifServer {
    async fn search_gif(
        self,
        _: Context,
        query: String,
        after: Option<String>,
        gif_context: GifContext,
    ) -> Result<Paginated<GifItem, String>, GifError> {
        self._search_gif(query, after, gif_context).await
    }

    async fn get_search_suggestions(
        self,
        _: Context,
        query: String,
        gif_context: GifContext,
    ) -> Result<Vec<String>, GifError> {
        self._get_search_suggestions(query, gif_context).await
    }

    async fn on_gif_share(
        self,
        _: Context,
        id: String,
        query: Option<String>,
        gif_context: GifContext,
    ) -> Result<(), GifError> {
        self._on_gif_share(id, query, gif_context).await
    }

    async fn get_recent_gifs(
        self,
        _: Context,
        gif_context: GifContext,
    ) -> Result<Vec<GifItem>, GifError> {
        self._get_recent_gifs(gif_context).await
    }

    async fn add_to_favourites(
        self,
        _: Context,
        id: String,
        gif_context: GifContext,
    ) -> Result<(), GifError> {
        self._add_to_favourites(id, gif_context).await
    }

    async fn remove_from_favourites(
        self,
        _: Context,
        id: String,
        gif_context: GifContext,
    ) -> Result<(), GifError> {
        self._remove_from_favourites(id, gif_context).await
    }

    async fn get_favourite_gifs(
        self,
        _: Context,
        gif_context: GifContext,
        after: Option<DateTime<Utc>>,
    ) -> Result<Paginated<GifItem, DateTime<Utc>>, GifError> {
        self._get_favourite_gifs(gif_context, after).await
    }
}
