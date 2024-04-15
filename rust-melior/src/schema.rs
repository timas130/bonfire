pub(crate) mod auth;
mod internal;
pub(crate) mod level;
mod notification;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::internal::{InternalMutation, InternalQuery};
use crate::schema::level::{LevelMutation, LevelQuery};
use crate::schema::notification::{NotificationMutation, NotificationQuery};
use async_graphql::{EmptySubscription, MergedObject, MergedSubscription};

#[derive(MergedObject, Default)]
pub struct Query(InternalQuery, LevelQuery, AuthQuery, NotificationQuery);

#[derive(MergedObject, Default)]
pub struct Mutation(
    InternalMutation,
    LevelMutation,
    AuthMutation,
    NotificationMutation,
);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
