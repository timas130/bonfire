use crate::ProfileServer;
use c_core::page_info::{Edge, PageInfo, Paginated};
use c_core::services::profile::{Badge, ProfileError};
use sqlx::types::chrono::{DateTime, Utc};

impl ProfileServer {
    pub(crate) async fn _get_user_badges(
        &self,
        user_id: i64,
        before: Option<DateTime<Utc>>,
    ) -> Result<Paginated<Badge, DateTime<Utc>>, ProfileError> {
        let badges = sqlx::query_as!(
            Badge,
            "select * from badges \
             where user_id = $1 and ($2::timestamptz is null or created_at < $2) \
             order by created_at desc limit 10",
            user_id,
            before,
        )
        .fetch_all(&self.base.pool)
        .await?;

        let has_next = if let Some(last) = badges.last() {
            sqlx::query_scalar!(
                "select id from badges where created_at < $1",
                last.created_at,
            )
            .fetch_optional(&self.base.pool)
            .await?
            .is_some()
        } else {
            false
        };
        let end_cursor = badges.last().map(|last| last.created_at);

        Ok(Paginated {
            edges: Edge::map(badges, |badge| badge.created_at),
            page_info: PageInfo {
                has_next_page: has_next,
                has_previous_page: false,
                start_cursor: None,
                end_cursor,
            },
        })
    }
}
