use crate::context::GlobalContext;
use crate::error::RespError;
use crate::loader_impl;
use async_graphql::dataloader::Loader;
use async_trait::async_trait;
use c_core::prelude::tarpc::context;
use c_core::services::auth::user::AuthUser;
use std::collections::HashMap;

pub struct AuthUserLoader {
    ctx: GlobalContext,
}
loader_impl!(AuthUserLoader);

#[async_trait]
impl Loader<i64> for AuthUserLoader {
    type Value = AuthUser;
    type Error = RespError;

    async fn load(&self, keys: &[i64]) -> Result<HashMap<i64, Self::Value>, Self::Error> {
        Ok(self
            .ctx
            .auth
            .get_by_ids(context::current(), keys.to_vec())
            .await??)
    }
}

#[async_trait]
impl Loader<String> for AuthUserLoader {
    type Value = AuthUser;
    type Error = RespError;

    async fn load(&self, keys: &[String]) -> Result<HashMap<String, Self::Value>, Self::Error> {
        Ok(self
            .ctx
            .auth
            .get_by_names(context::current(), keys.to_vec())
            .await??)
    }
}
