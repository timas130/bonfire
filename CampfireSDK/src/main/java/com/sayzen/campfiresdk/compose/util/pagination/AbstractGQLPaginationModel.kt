package com.sayzen.campfiresdk.compose.util.pagination

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Query
import com.sayzen.campfiresdk.compose.util.combineStates
import com.sayzen.campfiresdk.compose.util.mapState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.sit.bonfire.auth.ApolloController
import sh.sit.bonfire.auth.watchExt

// when inheriting the class, you MUST call reload() in your constructor
abstract class AbstractGQLPaginationModel<T, D : Query.Data>(application: Application) : AndroidViewModel(application),
    PaginationModel<T> {
    abstract fun hasNextPage(data: D): Boolean?
    abstract fun toItems(data: D): List<T>?
    abstract fun createQuery(items: List<T>): Query<D>

    private val _query = MutableStateFlow<ApolloResponse<D>?>(null)
    private val _isLoading = MutableStateFlow(false)

    override val isError = _query.mapState { !it?.errors.isNullOrEmpty() }
    override val isLoading = _query
        .mapState { it == null }
        .combineStates(_isLoading) { a, b -> a || b }
    override val items = _query.mapState { resp ->
        resp?.data?.let { toItems(it) }
    }
    override val hasMore = _query.mapState {
        it?.data?.let { it1 -> hasNextPage(it1) }
    }

    private var watchJob: Job? = null
    private var loadingMore: Boolean = false

    final override fun reload() {
        watchJob?.cancel()
        if (_query.value != null) {
            _isLoading.tryEmit(true)
        }

        watchJob = viewModelScope.launch {
            ApolloController.apolloClient
                .query(createQuery(emptyList()))
                .watchExt(getApplication<Application>().applicationContext)
                .collect {
                    _isLoading.emit(false)
                    _query.emit(it)
                }
        }
    }

    final override fun loadNextPage() {
        viewModelScope.launch {
            if (loadingMore) return@launch
            loadingMore = true
            withContext(Dispatchers.IO) {
                try {
                    ApolloController.apolloClient
                        .query(createQuery(items.value ?: emptyList()))
                        .execute()
                } finally {
                    loadingMore = false
                }
            }
        }
    }
}
