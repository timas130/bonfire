mod change_name;
mod check_in;
mod delete_user;
mod level;

use crate::schema::internal::change_name::InternalChangeNameMutation;
use crate::schema::internal::check_in::InternalCheckInMutation;
use crate::schema::internal::delete_user::InternalDeleteUserMutation;
use crate::schema::internal::level::InternalLevelQuery;
use async_graphql::MergedObject;

#[derive(MergedObject, Default)]
pub struct InternalQuery(InternalLevelQuery);

#[derive(MergedObject, Default)]
pub struct InternalMutation(
    InternalCheckInMutation,
    InternalChangeNameMutation,
    InternalDeleteUserMutation,
);
