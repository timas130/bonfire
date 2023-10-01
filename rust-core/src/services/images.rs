use crate::client_tcp;
use crate::services::images::options::UploadOptions;
use crate::util::{anyhow_clone, anyhow_unknown, sqlx_clone, sqlx_unknown};
use base64_blob::Base64Blob;
use educe::Educe;
use serde::{Deserialize, Serialize};
use thiserror::Error;

pub mod base64_blob;
pub mod options;

#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum ImageError {
    #[error("InvalidImage: Failed to read image")]
    InvalidImage,

    #[error("AspectRatio: Invalid aspect ratio")]
    AspectRatio,

    #[error("Sqlx: Unknown error: {source}")]
    Sqlx {
        // rust macros moment
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "sqlx_unknown")]
        #[educe(Eq(ignore), PartialEq(ignore), Clone(method = "sqlx_clone"))]
        source: sqlx::Error,
    },
    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), PartialEq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

/// Information about an image from the images service.
#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Image {
    /// ID of the image.
    pub id: String,
    /// URL where the image can be accessed.
    pub url: String,
    /// Width of the image in pixels.
    pub w: u32,
    /// Height of the image in pixels.
    pub h: u32,
    /// The image's weight in bytes.
    pub file_size: usize,
    /// The mime type of the resource. Commonly `image/jpeg`.
    pub mime: String,
}

#[tarpc::service]
pub trait ImagesService {
    // TODO: Chunked uploading

    /// Upload an image to the server.
    async fn upload_image(blob: Base64Blob, options: UploadOptions) -> Result<Image, ImageError>;

    // TODO: Allow batch fetching
    /// Get information about an image by its ID.
    async fn get_image(id: String) -> Result<Option<Image>, ImageError>;

    /// Get the image data itself with the information about the image.
    async fn get_image_data(id: String) -> Result<Option<(Image, Base64Blob)>, ImageError>;

    /// Delete an image if it exists.
    ///
    /// Returns `true` if the image did exist, `false` otherwise.
    async fn delete_image(id: String) -> Result<bool, ImageError>;
}

pub struct Images;
impl Images {
    client_tcp!(ImagesServiceClient);
}
