use num_enum::{IntoPrimitive, TryFromPrimitive};

#[derive(Hash, Debug, Copy, Clone, Eq, PartialEq, IntoPrimitive, TryFromPrimitive)]
#[repr(i64)]
pub enum PublicationType {
    Comment = 1,
    ChatMessage = 8,
    Post = 9,
    Tag = 10,
    Moderation = 11,
    EventUser = 12,
    StickersPack = 15,
    Sticker = 16,
    EventModer = 17,
    EventAdmin = 18,
    EventFandom = 19,
    Quest = 21,
}
