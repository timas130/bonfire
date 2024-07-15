pub(crate) mod auth;
mod internal;
pub(crate) mod level;
mod notification;
pub(crate) mod profile;
mod security;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::internal::{InternalMutation, InternalQuery};
use crate::schema::level::{LevelMutation, LevelQuery};
use crate::schema::notification::{NotificationMutation, NotificationQuery};
use crate::schema::profile::{ProfileMutation, ProfileQuery};
use crate::schema::security::SecurityMutation;
use async_graphql::{EmptySubscription, MergedObject, MergedSubscription};

#[derive(MergedObject, Default)]
pub struct Query(
    InternalQuery,
    LevelQuery,
    AuthQuery,
    NotificationQuery,
    ProfileQuery,
);

#[derive(MergedObject, Default)]
pub struct Mutation(
    InternalMutation,
    LevelMutation,
    AuthMutation,
    NotificationMutation,
    ProfileMutation,
    SecurityMutation,
);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
