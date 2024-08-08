mod gif_search_suggestions;
mod register_gif_share;
mod search_gif;

use crate::context::ReqContext;
use crate::schema::gif::gif_search_suggestions::GifSearchSuggestionsQuery;
use crate::schema::gif::register_gif_share::RegisterGifShareMutation;
use crate::schema::gif::search_gif::SearchGifQuery;
use async_graphql::MergedObject;
use c_core::services::gif::GifContext;

#[derive(Default, MergedObject)]
pub struct GifQuery(SearchGifQuery, GifSearchSuggestionsQuery);

#[derive(Default, MergedObject)]
pub struct GifMutation(RegisterGifShareMutation);

impl ReqContext {
    fn get_gif_context(&self) -> GifContext {
        GifContext {
            locale: Some(self.user_language.clone()),
            country: self
                .user_country_code
                .clone()
                .unwrap_or_else(|| String::from("ru")),
        }
    }
}
