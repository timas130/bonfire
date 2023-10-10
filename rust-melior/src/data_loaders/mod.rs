mod auth;

pub use auth::AuthUserLoader;

#[macro_export]
macro_rules! loader_impl {
    ($loader: ty) => {
        impl $loader {
            pub fn new(ctx: $crate::context::GlobalContext) -> Self {
                Self { ctx }
            }

            pub fn data_loader(
                ctx: $crate::context::GlobalContext,
            ) -> async_graphql::dataloader::DataLoader<Self> {
                async_graphql::dataloader::DataLoader::new(
                    Self::new(ctx),
                    c_core::prelude::tokio::spawn,
                )
            }
        }
    };
}
