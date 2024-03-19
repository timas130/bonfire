mod check;
mod limits;
mod upload;

use crate::schema::image::check::CheckImageMutation;
use crate::schema::image::limits::UploadLimitsQuery;
use crate::schema::image::upload::UploadImageMutation;
use async_graphql::MergedObject;

#[derive(Default, MergedObject)]
pub struct ImageQuery(UploadLimitsQuery);

#[derive(Default, MergedObject)]
pub struct ImageMutation(UploadImageMutation, CheckImageMutation);
