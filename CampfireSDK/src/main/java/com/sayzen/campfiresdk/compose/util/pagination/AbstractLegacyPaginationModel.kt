package com.sayzen.campfiresdk.compose.util.pagination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

// when inheriting the class, you MUST call reload() in your constructor
abstract class AbstractLegacyPaginationModel<T> : PaginationModel<T>, ViewModel() {
    private val _items = MutableStateFlow<List<T>?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow<Boolean?>(null)

    override val items: StateFlow<List<T>?> = _items.asStateFlow()
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    override val isError: StateFlow<Boolean> = _isError.asStateFlow()
    override val hasMore: StateFlow<Boolean?> = _hasMore.asStateFlow()

    final override fun reload() {
        if (_items.value != null) {
            _items.value = null
        }
        _isLoading.value = true
        loadNextPage()
    }

    private var loadingMore: Boolean = false

    override fun loadNextPage() {
        viewModelScope.launch {
            if (loadingMore) return@launch
            loadingMore = true

            try {
                val moreItems = load(_items.value ?: emptyList())
                _items.getAndUpdate {
                    (it ?: emptyList()) + moreItems
                }
                _hasMore.emit(moreItems.isNotEmpty())
                _isError.emit(false)
            } catch (e: Exception) {
                e.printStackTrace()
                _isError.emit(true)
            } finally {
                _isLoading.emit(false)
                loadingMore = false
            }
        }
    }

    abstract suspend fun load(current: List<T>): List<T>
}
