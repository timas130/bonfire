package com.sayzen.campfiresdk.compose.util.pagination

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.exception.ApolloException
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
    private var nextPageJob: Job? = null

    private var nextPageDropped = false

    final override fun reload() {
        watchJob?.cancel()
        if (nextPageJob?.isActive == true) {
            nextPageJob?.cancel()
            nextPageDropped = true
        }
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
                    if (nextPageDropped) {
                        nextPageDropped = false
                        loadNextPage()
                    }
                }
        }
    }

    final override fun loadNextPage() {
        if (_query.value == null || hasMore.value == false) return
        if (_isLoading.value) {
            nextPageDropped = true
            return
        }
        if (nextPageJob?.isActive == true) return
        nextPageJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    ApolloController.apolloClient
                        .query(createQuery(items.value ?: emptyList()))
                        .execute()
                } catch (e: ApolloException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
