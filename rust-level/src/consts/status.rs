use num_enum::{IntoPrimitive, TryFromPrimitive};

#[derive(Hash, Debug, Copy, Clone, Eq, PartialEq, IntoPrimitive, TryFromPrimitive)]
#[repr(i64)]
pub enum Status {
    Draft = 1,
    Public = 2,
    Blocked = 3,
    DeepBlocked = 4,
    Pending = 5,
    Archive = 6,
    Removed = 7,
}
