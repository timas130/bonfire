use async_graphql::connection::{
    Connection, ConnectionNameType, CursorType, Edge, EdgeNameType, EmptyFields,
};
use async_graphql::OutputType;
use c_core::page_info::Paginated;
use std::fmt::Debug;

pub trait PaginatedExt<T, Cursor>
where
    Cursor: CursorType + Send + Sync,
{
    fn into_connection<TOut, Name, EdgeName>(
        self,
    ) -> Connection<Cursor, TOut, EmptyFields, EmptyFields, Name, EdgeName>
    where
        TOut: OutputType + From<T>,
        Name: ConnectionNameType,
        EdgeName: EdgeNameType;
}

impl<T, Cursor> PaginatedExt<T, Cursor> for Paginated<T, Cursor>
where
    Cursor: CursorType + Send + Sync + Debug,
    T: Debug,
{
    fn into_connection<TOut, Name, EdgeName>(
        self,
    ) -> Connection<Cursor, TOut, EmptyFields, EmptyFields, Name, EdgeName>
    where
        TOut: OutputType + From<T>,
        Name: ConnectionNameType,
        EdgeName: EdgeNameType,
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
