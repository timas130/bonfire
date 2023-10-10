mod auth;
mod internal;
pub(crate) mod level;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::internal::{InternalMutation, InternalQuery};
use crate::schema::level::{LevelMutation, LevelQuery};
use async_graphql::{EmptySubscription, MergedObject, MergedSubscription};

#[derive(MergedObject, Default)]
pub struct Query(InternalQuery, LevelQuery, AuthQuery);

#[derive(MergedObject, Default)]
pub struct Mutation(InternalMutation, LevelMutation, AuthMutation);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
