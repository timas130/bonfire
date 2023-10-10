mod level;

use crate::schema::internal::level::LevelQuery;
use async_graphql::MergedObject;

#[derive(MergedObject, Default)]
pub struct InternalQuery(LevelQuery);

#[derive(MergedObject, Default)]
pub struct InternalMutation();
