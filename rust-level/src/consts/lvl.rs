use c_core::services::level::LevelCategory;

pub const MODERATOR_BLOCK_KARMA: i64 = 30000;

pub trait LevelCategoryExt {
    fn from_level(level: u64) -> Self;
}

impl LevelCategoryExt for LevelCategory {
    fn from_level(level: u64) -> Self {
        match level {
            0..=299 => Self::User,
            300..=449 => Self::Trusted,
            450..=599 => Self::Experienced,
            600..=699 => Self::Curator,
            700..=849 => Self::Moderator,
            850..=999 => Self::Admin,
            1000..=1199 => Self::Superadmin,
            1200..=49999 => Self::Expert,
            // not going to risk it
            _ => Self::User,
        }
    }
}
