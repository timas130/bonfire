use num_enum::{IntoPrimitive, TryFromPrimitive};

#[derive(Hash, Debug, Copy, Clone, Eq, PartialEq, IntoPrimitive, TryFromPrimitive)]
#[repr(i64)]
pub enum ChatType {
    FandomRoot = 1,
    Private = 2,
    Conference = 3,
    FandomSub = 4,
}
