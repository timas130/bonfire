use serde::{Deserialize, Serialize};

/// Options to use when processing an image on the server.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UploadOptions {
    /// Maximum allowed width for the image.
    pub max_w: u32,
    /// Maximum allowed height for the image.
    pub max_h: u32,
    /// Exact aspect ratio allowed for the image.
    pub enforce_aspect_ratio: Option<f32>,
}
impl Default for UploadOptions {
    fn default() -> Self {
        Self {
            max_w: u32::MAX,
            max_h: u32::MAX,
            enforce_aspect_ratio: None,
        }
    }
}

impl UploadOptions {
    /// Defaults used for encoding avatars.
    pub const AVATAR: UploadOptions = UploadOptions {
        max_w: 256,
        max_h: 256,
        enforce_aspect_ratio: Some(1.),
    };

    /// Defaults used for encoding game icons.
    pub const GAME_ICON: UploadOptions = UploadOptions {
        max_w: 256,
        max_h: 256,
        enforce_aspect_ratio: Some(1.),
    };

    /// Defaults for a chat message.
    pub const CHAT: UploadOptions = UploadOptions {
        max_w: 1280,
        max_h: 1280,
        enforce_aspect_ratio: None,
    };

    /// Create new [`UploadOptions`] using the default (non-restricting) values.
    pub fn new() -> Self {
        Self::default()
    }

    /// Set the maximum allowed width and height of the image to `side`.
    pub fn max_side(self, side: u32) -> Self {
        self.max_w(side).max_h(side)
    }

    /// Set the maximum allowed width of the image to `w`.
    pub fn max_w(mut self, w: u32) -> Self {
        self.max_w = w;
        self
    }
    /// Set the maximum allowed height of the image to `h`.
    pub fn max_h(mut self, h: u32) -> Self {
        self.max_h = h;
        self
    }

    /// Set the forced aspect ratio of the image. If the image does
    /// not satisfy this aspect ratio, the server errors with
    /// [`ImageError::AspectRatio`].
    ///
    /// ## Examples
    ///
    /// ```rust
    /// # use c_core::services::images::options::UploadOptions;
    ///
    /// // Only 16:9 images up to 1920x1080 pixels will be allowed.
    /// UploadOptions::new()
    ///     .max_w(1920)
    ///     .enforce_aspect_ratio(16. / 9.);
    /// ```
    ///
    /// [`ImageError::AspectRatio`]: super::ImageError::AspectRatio
    pub fn enforce_aspect_ratio(mut self, ratio: f32) -> Self {
        self.enforce_aspect_ratio = Some(ratio);
        self
    }
}
