pub(crate) mod daily_task;
pub(crate) mod daily_task_fandoms;

use async_graphql::MergedObject;

#[derive(MergedObject, Default)]
pub struct LevelQuery();

#[derive(MergedObject, Default)]
pub struct LevelMutation();
