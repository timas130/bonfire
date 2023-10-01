use serde::{Deserialize, Serialize};
use std::fmt::Debug;

#[derive(Debug, Serialize, Deserialize)]
pub struct Paginated<T: Debug, Cursor: Debug> {
    pub edges: Vec<Edge<T, Cursor>>,
    pub page_info: PageInfo<Cursor>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Edge<T: Debug, Cursor: Debug> {
    pub node: T,
    pub cursor: Cursor,
}
impl<T: Debug, Cursor: Debug> Edge<T, Cursor> {
    pub fn map<Mapper>(list: Vec<T>, mapper: Mapper) -> Vec<Edge<T, Cursor>>
    where
        Mapper: Fn(&T) -> Cursor,
    {
        list.into_iter()
            .map(|item| Self {
                cursor: mapper(&item),
                node: item,
            })
            .collect()
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct PageInfo<Cursor: Debug> {
    pub has_next_page: bool,
    pub has_previous_page: bool,
    pub start_cursor: Option<Cursor>,
    pub end_cursor: Option<Cursor>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct PaginationRequest<Cursor: Debug> {
    pub after: Option<Cursor>,
    pub before: Option<Cursor>,
    pub first: Option<i32>,
    pub last: Option<i32>,
}
