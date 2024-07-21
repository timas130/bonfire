mod create_intention;
mod save_play_integrity;

use crate::schema::security::create_intention::CreateIntentionMutation;
use crate::schema::security::save_play_integrity::SavePlayIntegrityMutation;
use async_graphql::MergedObject;

#[derive(Default, MergedObject)]
pub struct SecurityMutation(SavePlayIntegrityMutation, CreateIntentionMutation);
