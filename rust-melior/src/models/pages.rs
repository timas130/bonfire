use async_graphql::Enum;
use c_core::models;

#[derive(Enum, Copy, Clone, Eq, PartialEq)]
pub enum PageType {
    Text = 1,
    Image = 2,
    Images = 3,
    Link = 4,
    Quote = 5,
    Spoiler = 6,
    Polling = 7,
    Video = 9,
    Table = 10,
    Download = 11,
    CampfireObject = 12,
    UserActivity = 13,
    LinkImage = 14,
    Code = 16,
}

impl From<models::PageType> for PageType {
    fn from(value: models::PageType) -> Self {
        match value {
            models::PageType::Text => Self::Text,
            models::PageType::Image => Self::Image,
            models::PageType::Images => Self::Images,
            models::PageType::Link => Self::Link,
            models::PageType::Quote => Self::Quote,
            models::PageType::Spoiler => Self::Spoiler,
            models::PageType::Polling => Self::Polling,
            models::PageType::Video => Self::Video,
            models::PageType::Table => Self::Table,
            models::PageType::Download => Self::Download,
            models::PageType::CampfireObject => Self::CampfireObject,
            models::PageType::UserActivity => Self::UserActivity,
            models::PageType::LinkImage => Self::LinkImage,
            models::PageType::Code => Self::Code,
        }
    }
}
