use async_graphql::{Enum, Object, SimpleObject};
use image::ImageFormat;
use strum::{EnumString, IntoStaticStr};

#[derive(Default)]
pub struct UploadLimitsQuery;

#[Object]
impl UploadLimitsQuery {
    /// Get limits for a specified upload type
    async fn upload_limits(&self, upload_type: UploadType) -> UploadLimits {
        upload_type.limits()
    }
}

/// Limits for an upload
#[derive(SimpleObject, Clone)]
pub struct UploadLimits {
    /// Max size in bytes
    pub size: usize,
    /// Expected aspect ratio
    pub ratio: Option<f32>,
    /// Maximum resolution (per side)
    pub max_side: u32,
    /// File format
    pub format: UploadFormat,
}

impl UploadLimits {
    fn square(size_kb: usize, side: u32) -> Self {
        Self {
            size: size_kb * 1024,
            ratio: Some(1f32),
            max_side: side,
            format: UploadFormat::Jpeg,
        }
    }

    fn square_gif(size_kb: usize, side: u32) -> Self {
        Self {
            format: UploadFormat::Gif,
            ..Self::square(size_kb, side)
        }
    }

    fn square_png(size_kb: usize, side: u32) -> Self {
        Self {
            format: UploadFormat::Png,
            ..Self::square(size_kb, side)
        }
    }

    fn jpeg(size_kb: usize, side: u32) -> Self {
        Self {
            format: UploadFormat::Jpeg,
            size: size_kb * 1024,
            max_side: side,
            ratio: None,
        }
    }

    fn gif(size_kb: usize, side: u32) -> Self {
        Self {
            format: UploadFormat::Gif,
            ..Self::jpeg(size_kb, side)
        }
    }
}

/// Image/file format
#[derive(Enum, Eq, PartialEq, Copy, Clone)]
pub enum UploadFormat {
    /// Animated GIF
    Gif,
    /// PNG or JPEG
    Png,
    /// JPEG
    Jpeg,
}

impl UploadFormat {
    pub(crate) fn check(&self, image_format: ImageFormat) -> bool {
        match self {
            UploadFormat::Gif => image_format == ImageFormat::Gif,
            UploadFormat::Png => {
                image_format == ImageFormat::Png || image_format == ImageFormat::Jpeg
            }
            UploadFormat::Jpeg => image_format == ImageFormat::Jpeg,
        }
    }
}

/// Purpose for the uploaded image, which defines its limits
#[derive(Enum, Copy, Clone, Eq, PartialEq, Debug, EnumString, IntoStaticStr)]
pub enum UploadType {
    AccountAvatar,
    AccountAvatarGif,
    AccountTitle,
    AccountTitleGif,
    FandomAvatar,
    FandomAvatarGif,
    FandomTitle,
    FandomTitleGif,
    ChatBackground,
    FandomGallery,
    ChatAvatar,
    ChatAvatarGif,
    WikiAvatar,
    WikiAvatarGif,
    WikiTitle,
    WikiTitleGif,
    ChatMessage,
    ChatMessageGif,
    Tag,
    StickerPackAvatar,
    Sticker,
    StickerGif,
    PageImage,
    PageImageGif,
    PageImagesMini,
    PageImagesMiniGif,
    PageImages,
    PageImagesGif,
    PageImageLink,
    QuestImage,
}

impl UploadType {
    pub fn limits(&self) -> UploadLimits {
        match self {
            UploadType::AccountAvatar => UploadLimits::square(32, 384),
            UploadType::AccountAvatarGif => UploadLimits::square_gif(256, 92),
            UploadType::AccountTitle => UploadLimits {
                size: 384 * 1024,
                ratio: Some(2f32),
                max_side: 1200,
                format: UploadFormat::Jpeg,
            },
            UploadType::AccountTitleGif => UploadLimits {
                size: 3 * 512 * 1024,
                ratio: Some(2f32),
                max_side: 400,
                format: UploadFormat::Gif,
            },
            UploadType::FandomAvatar => Self::AccountAvatar.limits(),
            UploadType::FandomAvatarGif => Self::AccountAvatarGif.limits(),
            UploadType::FandomTitle => Self::AccountTitle.limits(),
            UploadType::FandomTitleGif => Self::AccountTitleGif.limits(),
            UploadType::ChatBackground => UploadLimits {
                size: 256 * 1024,
                ratio: Some(0.5625f32),
                max_side: 1280,
                format: UploadFormat::Jpeg,
            },
            UploadType::FandomGallery => UploadLimits::jpeg(512, 1920),
            UploadType::ChatAvatar => Self::AccountAvatar.limits(),
            UploadType::ChatAvatarGif => Self::AccountAvatarGif.limits(),
            UploadType::WikiAvatar => Self::AccountAvatar.limits(),
            UploadType::WikiAvatarGif => Self::AccountAvatarGif.limits(),
            UploadType::WikiTitle => Self::AccountTitle.limits(),
            UploadType::WikiTitleGif => Self::AccountTitleGif.limits(),
            UploadType::ChatMessage => UploadLimits::jpeg(768, 1280),
            UploadType::ChatMessageGif => UploadLimits::gif(1024, 400),
            UploadType::Tag => UploadLimits::square(8, 64),
            UploadType::StickerPackAvatar => Self::AccountAvatar.limits(),
            UploadType::Sticker => UploadLimits::square_png(64, 512),
            UploadType::StickerGif => UploadLimits::square_gif(256, 300),
            UploadType::PageImage => UploadLimits::jpeg(1024, 1500),
            UploadType::PageImageGif => UploadLimits::gif(6 * 1024, 500),
            UploadType::PageImagesMini => UploadLimits::jpeg(128, 500),
            UploadType::PageImagesMiniGif => UploadLimits::gif(384, 128),
            UploadType::PageImages => UploadLimits::jpeg(1024, 1920),
            UploadType::PageImagesGif => UploadLimits::gif(6 * 1024, 400),
            UploadType::PageImageLink => Self::AccountTitle.limits(),
            UploadType::QuestImage => Self::AccountTitle.limits(),
        }
    }
}
