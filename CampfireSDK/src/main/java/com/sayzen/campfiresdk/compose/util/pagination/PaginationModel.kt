package com.sayzen.campfiresdk.compose.util.pagination

import kotlinx.coroutines.flow.StateFlow

interface PaginationModel<T> {
    /** Show an error card at the bottom */
    val isError: StateFlow<Boolean>
    /** Display the PullToRefresh indicator */
    val isLoading: StateFlow<Boolean>
    /** Every item to display on-screen */
    val items: StateFlow<List<T>?>
    /** Show the bottom loader */
    val hasMore: StateFlow<Boolean?>

    /** Do a full refresh */
    fun reload()
    /** Load the next batch of items and add it to `items` */
    fun loadNextPage()
}
