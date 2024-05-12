use async_graphql::Object;

/// A color in various formats
#[derive(Copy, Clone)]
pub struct Color(u32);

#[Object]
impl Color {
    /// As an Android `Int`: `0xAARRGGBB`
    async fn int(&self) -> i32 {
        self.0 as i32
    }

    /// As a CSS hex or rgba: `#RRGGBB` or `rgba(r g b, a)`
    async fn css(&self) -> String {
        if self.0 & 0xFF000000 == 0xFF000000 {
            format!("#{:6x}", self.0)
        } else {
            let r = self.0 >> 16 & 0xFF;
            let g = self.0 >> 8 & 0xFF;
            let b = self.0 & 0xFF;
            let a = (self.0 >> 24) as f32 / 255.0;
            format!("rgba({r} {g} {b}, {a})")
        }
    }
}

impl From<u32> for Color {
    fn from(value: u32) -> Self {
        Self(value)
    }
}
