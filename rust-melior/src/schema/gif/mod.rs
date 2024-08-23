mod add_gif_to_favourites;
mod favourite_gifs;
mod gif_search_suggestions;
mod recent_gifs;
mod register_gif_share;
mod remove_gif_from_favourites;
pub(crate) mod search_gif;

use crate::context::ReqContext;
use crate::error::RespError;
use crate::schema::gif::add_gif_to_favourites::AddGifToFavouritesMutation;
use crate::schema::gif::gif_search_suggestions::GifSearchSuggestionsQuery;
use crate::schema::gif::recent_gifs::RecentGifsQuery;
use crate::schema::gif::register_gif_share::RegisterGifShareMutation;
use crate::schema::gif::remove_gif_from_favourites::RemoveGifFromFavouritesMutation;
use crate::schema::gif::search_gif::SearchGifQuery;
use async_graphql::MergedObject;
use c_core::services::gif::GifContext;

#[derive(Default, MergedObject)]
pub struct GifQuery(SearchGifQuery, GifSearchSuggestionsQuery, RecentGifsQuery);

#[derive(Default, MergedObject)]
pub struct GifMutation(
    RegisterGifShareMutation,
    AddGifToFavouritesMutation,
    RemoveGifFromFavouritesMutation,
);

impl ReqContext {
    fn get_gif_context(&self) -> Result<GifContext, RespError> {
        Ok(GifContext {
            locale: Some(self.user_language.clone()),
            country: self
                .user_country_code
                .clone()
                .unwrap_or_else(|| String::from("ru"))
                .replace('-', "_"),
            user_id: self.require_user()?.id,
        })
    }
}
