//! # The GIF service
//!
//! This service essentially serves as a proxy to the Tenor API.

use crate::client_tcp;
use crate::page_info::Paginated;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use chrono::{DateTime, Utc};
use educe::Educe;
use serde::{Deserialize, Serialize};
use thiserror::Error;

#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum GifError {
    #[error("UpstreamError: Please try again in a few seconds.")]
    UpstreamError,

    #[error("InvalidRequest: Invalid request, check the parameters.")]
    InvalidRequest,

    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// Information about a GIF(-related) file
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GifMediaRef {
    /// URL where the GIF can be downloaded
    pub url: String,
    /// Resolution of the file
    pub resolution: [i32; 2],
}

/// Variants of a single GIF
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GifMediaFormats {
    /// Static preview image
    pub preview: GifMediaRef,
    /// Animated GIF (very big file)
    pub gif: GifMediaRef,
    /// Animated GIF of manageable size
    pub tiny_gif: GifMediaRef,
    /// The animation in MP4 format
    pub mp4: GifMediaRef,
    /// The animation in a more compressed MP4
    pub tiny_mp4: GifMediaRef,
}

/// A GIF
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GifItem {
    /// Unique identifier for this GIF
    pub id: String,
    /// URL of a page with information about this GIF
    pub browser_url: String,
    /// This GIF in different formats
    pub media: GifMediaFormats,
}

/// Various useful information about the user making the request
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GifContext {
    /// Two-letter country code where the user is from
    pub country: String,
    /// Two-letter (or `aa_BB`) language code to be used
    ///
    /// If present, the second part MUST be separated with an underscore.
    pub locale: Option<String>,
    /// Unique ID for this user
    pub user_id: i64,
}

#[tarpc::service]
pub trait GifService {
    /// Search for GIFs
    ///
    /// If `query` is empty, a list of featured GIFs is returned.
    ///
    /// ## Pagination
    ///
    /// The `after` parameter MUST NOT use the `cursor` provided
    /// in [`Edge`].
    /// You can only use information from [`PageInfo`] there.
    ///
    /// [`Edge`]: c_core::page_info::Edge
    /// [`PageInfo`]: c_core::page_info::PageInfo
    async fn search_gif(
        query: String,
        after: Option<String>,
        gif_context: GifContext,
    ) -> Result<Paginated<GifItem, String>, GifError>;

    /// Get some suggestions for a search query
    ///
    /// If `query` is empty, a list of searchable categories
    /// is returned.
    async fn get_search_suggestions(
        query: String,
        gif_context: GifContext,
    ) -> Result<Vec<String>, GifError>;

    /// Notify the server that a GIF has been shared
    ///
    /// You MUST call this method when sharing a GIF.
    async fn on_gif_share(
        id: String,
        query: Option<String>,
        gif_context: GifContext,
    ) -> Result<(), GifError>;

    /// Get up to 10 GIFs recently shared by the user
    async fn get_recent_gifs(gif_context: GifContext) -> Result<Vec<GifItem>, GifError>;

    /// Add a GIF to favourites
    async fn add_to_favourites(id: String, gif_context: GifContext) -> Result<(), GifError>;

    /// Remove a GIF from favourites if it exists
    async fn remove_from_favourites(id: String, gif_context: GifContext) -> Result<(), GifError>;

    /// Get a paginated list of favourite GIFs
    async fn get_favourite_gifs(
        gif_context: GifContext,
        after: Option<DateTime<Utc>>,
    ) -> Result<Paginated<GifItem, DateTime<Utc>>, GifError>;
}

pub struct Gif;
impl Gif {
    client_tcp!(GifServiceClient);
}
