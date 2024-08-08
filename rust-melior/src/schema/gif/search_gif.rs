use crate::context::ReqContext;
use crate::error::RespError;
use crate::models::image::ImageLink;
use crate::utils::connection::PaginatedExt;
use async_graphql::connection::Connection;
use async_graphql::{Context, Object, SimpleObject, ID};
use c_core::prelude::tarpc::context;
use c_core::services::gif::{GifItem, GifMediaFormats, GifMediaRef};
use o2o::o2o;

#[derive(Default)]
pub struct SearchGifQuery;

impl From<GifMediaRef> for ImageLink {
    fn from(value: GifMediaRef) -> Self {
        Self {
            u: value.url,
            w: Some(value.resolution[0] as i64),
            h: Some(value.resolution[1] as i64),
            i: -1,
        }
    }
}

/// Variants of a single GIF
#[derive(Debug, Clone, SimpleObject, o2o)]
#[graphql(name = "GifMediaFormats")]
#[from_owned(GifMediaFormats)]
pub struct GGifMediaFormats {
    /// Static preview image
    #[from(~.into())]
    pub preview: ImageLink,
    /// Animated GIF (very big file)
    #[from(~.into())]
    pub gif: ImageLink,
    /// Animated GIF of manageable size
    #[from(~.into())]
    pub tiny_gif: ImageLink,
    /// The animation in MP4 format
    #[from(~.into())]
    pub mp4: ImageLink,
    /// The animation in a more compressed MP4
    #[from(~.into())]
    pub tiny_mp4: ImageLink,
}

/// A GIF
#[derive(Debug, Clone, SimpleObject, o2o)]
#[graphql(name = "GifItem")]
#[from_owned(GifItem)]
pub struct GGifItem {
    /// Unique identifier for this GIF
    #[from(~.into())]
    pub id: ID,
    /// URL of a page with information about this GIF
    pub browser_url: String,
    /// This GIF in different formats
    #[from(~.into())]
    pub media: GGifMediaFormats,
}

#[Object]
impl SearchGifQuery {
    /// Search for GIFs
    ///
    /// If `query` is empty, a list of featured GIFs is returned.
    ///
    /// ## Pagination
    ///
    /// The `after` parameter MUST NOT use the `cursor` provided in `GifItemEdge`.
    /// You should only use information from `PageInfo` there.
    async fn search_gif(
        &self,
        ctx: &Context<'_>,
        term: String,
        after: Option<String>,
    ) -> Result<Connection<String, GGifItem>, RespError> {
        let req = ctx.data_unchecked::<ReqContext>();

        req.require_user()?;

        Ok(req
            .gif
            .search_gif(context::current(), term, after, req.get_gif_context())
            .await??
            .into_connection())
    }
}
