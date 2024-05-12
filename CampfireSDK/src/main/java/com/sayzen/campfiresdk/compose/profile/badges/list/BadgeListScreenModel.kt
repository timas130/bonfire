package com.sayzen.campfiresdk.compose.profile.badges.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.sayzen.campfiresdk.BadgeListQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    val isError = _query.map { !it?.errors.isNullOrEmpty() }
    val isLoading = _query.map { it == null }.combine(_isLoading) { a, b -> a || b }
    val badges = _query.map { it?.data?.userById?.badges?.edges }
    val hasMore = _query.map { it?.data?.userById?.badges?.pageInfo?.hasNextPage }

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

class BadgeListScreenModelFactory(
    private val userId: String
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return BadgeListScreenModel(extras[APPLICATION_KEY]!!, userId) as T
    }
}

