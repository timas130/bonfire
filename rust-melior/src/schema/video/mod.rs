mod by_id;
mod upload_cancel;
mod upload_finish;
mod upload_limits;
mod upload_start;

use crate::schema::video::by_id::VideoByIdQuery;
use crate::schema::video::upload_cancel::VideoUploadCancelMutation;
use crate::schema::video::upload_finish::VideoUploadFinishMutation;
use crate::schema::video::upload_limits::VideoUploadLimitsQuery;
use crate::schema::video::upload_start::VideoUploadStartMutation;
use async_graphql::MergedObject;

#[derive(MergedObject, Default)]
pub struct VideoQuery(VideoByIdQuery, VideoUploadLimitsQuery);

#[derive(MergedObject, Default)]
pub struct VideoMutation(
    VideoUploadStartMutation,
    VideoUploadFinishMutation,
    VideoUploadCancelMutation,
);
