package com.sayzen.campfiresdk.compose.profile.badges.list

import android.app.Application
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.sayzen.campfiresdk.BadgeListQuery
import com.sayzen.campfiresdk.compose.util.pagination.AbstractGQLPaginationModel

class BadgeListScreenModel(
    application: Application,
    private val userId: String,
) : AbstractGQLPaginationModel<BadgeListQuery.Edge, BadgeListQuery.Data>(application) {
    init {
        reload()
    }

    override fun hasNextPage(data: BadgeListQuery.Data): Boolean? {
        return data.userById?.badges?.pageInfo?.hasNextPage
    }

    override fun toItems(data: BadgeListQuery.Data): List<BadgeListQuery.Edge>? {
        return data.userById?.badges?.edges
    }

    override fun createQuery(items: List<BadgeListQuery.Edge>): Query<BadgeListQuery.Data> {
        return BadgeListQuery(userId, Optional.presentIfNotNull(items.lastOrNull()?.cursor))
    }
}
