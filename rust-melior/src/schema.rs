pub(crate) mod auth;
mod image;
mod internal;
pub(crate) mod level;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::image::{ImageMutation, ImageQuery};
use crate::schema::internal::{InternalMutation, InternalQuery};
use crate::schema::level::{LevelMutation, LevelQuery};
use async_graphql::{EmptySubscription, MergedObject, MergedSubscription};

#[derive(MergedObject, Default)]
pub struct Query(InternalQuery, LevelQuery, AuthQuery, ImageQuery);

#[derive(MergedObject, Default)]
pub struct Mutation(InternalMutation, LevelMutation, AuthMutation, ImageMutation);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
