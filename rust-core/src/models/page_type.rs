use num_enum::{IntoPrimitive, TryFromPrimitive};
use serde_repr::{Deserialize_repr, Serialize_repr};

/// Kinds of pages in a post.
#[derive(
    Hash,
    Debug,
    Copy,
    Clone,
    Eq,
    PartialEq,
    IntoPrimitive,
    TryFromPrimitive,
    Serialize_repr,
    Deserialize_repr,
)]
#[repr(i64)]
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
