package com.sayzen.campfiresdk.compose.profile.badges.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.sayzen.campfiresdk.BadgeListQuery
import com.sayzen.campfiresdk.compose.util.combineStates
import com.sayzen.campfiresdk.compose.util.mapState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.sit.bonfire.auth.ApolloController
import sh.sit.bonfire.auth.watchExt

class BadgeListScreenModel(
    application: Application,
    private val userId: String,
) : AndroidViewModel(application) {
    private val _query = MutableStateFlow<ApolloResponse<BadgeListQuery.Data>?>(null)
    private val _isLoading = MutableStateFlow(false)

    val isError = _query.mapState { !it?.errors.isNullOrEmpty() }
    val isLoading = _query
        .mapState { it == null }
        .combineStates(_isLoading) { a, b -> a || b }
    val badges = _query.mapState { it?.data?.userById?.badges?.edges }
    val hasMore = _query.mapState { it?.data?.userById?.badges?.pageInfo?.hasNextPage }

    private var watchJob: Job? = null
    private var loadingMore: Boolean = false

    init {
        reload()
    }

    fun reload() {
        watchJob?.cancel()
        if (_query.value != null) {
            _isLoading.tryEmit(true)
        }

        watchJob = viewModelScope.launch {
            ApolloController.apolloClient
                .query(BadgeListQuery(userId))
                .watchExt(getApplication<Application>().applicationContext)
                .collect {
                    _isLoading.emit(false)
                    _query.emit(it)
                }
        }
    }

    fun nextPage() {
        viewModelScope.launch {
            if (loadingMore) return@launch
            loadingMore = true
            val after = _query.value?.data?.userById?.badges?.edges?.lastOrNull()?.cursor
            withContext(Dispatchers.IO) {
                try {
                    ApolloController.apolloClient
                        .query(BadgeListQuery(userId, Optional.presentIfNotNull(after)))
                        .execute()
                } finally {
                    loadingMore = false
                }
            }
        }
    }
}
