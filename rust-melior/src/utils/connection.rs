use async_graphql::connection::{Connection, CursorType, Edge};
use async_graphql::OutputType;
use c_core::page_info::Paginated;
use std::fmt::Debug;

pub trait PaginatedExt<T, Cursor>
where
    Cursor: CursorType + Send + Sync,
{
    fn into_connection<TOut>(self) -> Connection<Cursor, TOut>
    where
        TOut: OutputType + From<T>;
}

impl<T, Cursor> PaginatedExt<T, Cursor> for Paginated<T, Cursor>
where
    Cursor: CursorType + Send + Sync + Debug,
    T: Debug,
{
    fn into_connection<TOut>(self) -> Connection<Cursor, TOut>
    where
        TOut: OutputType + From<T>,
    {
        let mut connection = Connection::new(
            self.page_info.has_previous_page,
            self.page_info.has_next_page,
        );
        connection.edges = self
            .edges
            .into_iter()
            .map(|edge| Edge::new(edge.cursor, TOut::from(edge.node)))
            .collect();
        connection
    }
}
