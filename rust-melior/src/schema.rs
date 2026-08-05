pub(crate) mod auth;
mod internal;
pub(crate) mod level;
mod notification;
pub(crate) mod profile;
mod security;
mod video;

use crate::schema::auth::{AuthMutation, AuthQuery};
use crate::schema::internal::{InternalMutation, InternalQuery};
use crate::schema::level::{LevelMutation, LevelQuery};
use crate::schema::notification::{NotificationMutation, NotificationQuery};
use crate::schema::profile::{ProfileMutation, ProfileQuery};
use crate::schema::security::SecurityMutation;
use crate::schema::video::{VideoMutation, VideoQuery};
use async_graphql::{EmptySubscription, MergedObject, MergedSubscription};

#[derive(MergedObject, Default)]
pub struct Query(
    InternalQuery,
    LevelQuery,
    AuthQuery,
    NotificationQuery,
    ProfileQuery,
    VideoQuery,
);

#[derive(MergedObject, Default)]
pub struct Mutation(
    InternalMutation,
    LevelMutation,
    AuthMutation,
    NotificationMutation,
    ProfileMutation,
    SecurityMutation,
    VideoMutation,
);

#[derive(MergedSubscription, Default)]
pub struct Subscription(EmptySubscription);
