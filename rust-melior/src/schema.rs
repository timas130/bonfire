pub(crate) mod auth;
mod gif;
mod internal;
pub(crate) mod level;
mod notification;
pub(crate) mod profile;
mod security;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::gif::{GifMutation, GifQuery};
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
    GifQuery,
);

#[derive(MergedObject, Default)]
pub struct Mutation(
    InternalMutation,
    LevelMutation,
    AuthMutation,
    NotificationMutation,
    ProfileMutation,
    SecurityMutation,
    GifMutation,
);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
