package com.sayzen.campfiresdk.compose.profile.badges.shelf

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.optimisticUpdates
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.exception.CacheMissException
import com.sayzen.campfiresdk.BadgeShelfQuery
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.SetBadgeShelfMutation
import com.sayzen.campfiresdk.SetBadgeShelfVisibleMutation
import com.sayzen.campfiresdk.compose.profile.badges.list.BadgeListScreen
import com.sayzen.campfiresdk.fragment.BadgeShelfIconImpl
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.ApolloController
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.watchExt

class BadgeShelfModel(
    application: Application,
    private val userId: String,
) : AndroidViewModel(application) {
    private val _query = MutableStateFlow<ApolloResponse<BadgeShelfQuery.Data>?>(null)
    private val _currentUser = AuthController.currentUser

    val isVisible = _query.combine(_currentUser) { query, user ->
        query == null || query.data?.userById?.profile?.badgeShelf != null
    }
    val isError = _query.map { it?.hasErrors() == true }
    val shelf = _query.map { it?.data?.userById?.profile?.badgeShelf }
    val isEditingAllowed = _currentUser.map { userId == it?.id }

    val isShowButtonVisible = _query.combine(_currentUser) { query, user ->
        userId == user?.id && query != null && query.data?.userById?.profile?.badgeShelf == null
    }

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            ApolloController.apolloClient
                .query(BadgeShelfQuery(userId))
                .watchExt(getApplication<Application>().applicationContext)
                .collect {
                    _query.emit(it)
                }
        }
    }

    fun onEditBadge(replaceIdx: Int) {
        viewModelScope.launch {
            if (!isEditingAllowed.first()) return@launch

            val selectScreen = BadgeListScreen(userId) { newBadge ->
                viewModelScope.launch {
                    onEdit(replaceIdx, newBadge?.id)
                }
            }

            Navigator.to(selectScreen)
        }
    }

    private suspend fun onEdit(replaceIdx: Int, newId: String?) {
        val newShelf = shelf.first()!!
            .mapIndexed { idx, badge ->
                if (idx == replaceIdx) newId
                else badge?.id
            }

        // trying to get some optimistic updates
        val newBadge = try {
            ApolloController.apolloClient.apolloStore
                .readFragment(
                    BadgeShelfIconImpl(),
                    CacheKey(newId!!),
                    ApolloController.apolloClient.customScalarAdapters
                )
        } catch (e: CacheMissException) {
            null
        } catch (e: NullPointerException) {
            null
        }
        val optimisticUpdate = newBadge?.let {
            SetBadgeShelfMutation.Data(
                setBadgeShelf = SetBadgeShelfMutation.SetBadgeShelf(
                    userId = userId,
                    badgeShelf = shelf.first()!!
                        .mapIndexed { idx, badge ->
                            if (idx == replaceIdx) {
                                SetBadgeShelfMutation.BadgeShelf(
                                    id = newBadge.id,
                                    badgeShelfIcon = newBadge,
                                    __typename = "Badge"
                                )
                            } else {
                                badge?.let {
                                    SetBadgeShelfMutation.BadgeShelf(
                                        id = it.id,
                                        badgeShelfIcon = it.badgeShelfIcon,
                                        __typename = it.__typename,
                                    )
                                }
                            }
                        },
                    __typename = "ProfileCustomization",
                )
            )
        }

        try {
            val resp = ApolloController.apolloClient
                .mutation(SetBadgeShelfMutation(newShelf))
                .apply { optimisticUpdate?.let { optimisticUpdates(it) } }
                .execute()
            if (resp.hasErrors()) {
                ToolsToast.show(R.string.app_error)
            }
        } catch (e: ApolloException) {
            ToolsToast.show(R.string.error_network_error)
        }
    }

    fun toList() {
        Navigator.to(BadgeListScreen(userId))
    }

    fun hide() {
        viewModelScope.launch {
            try {
                val resp = ApolloController.apolloClient
                    .mutation(SetBadgeShelfVisibleMutation(false))
                    .optimisticUpdates(
                        SetBadgeShelfVisibleMutation.Data(
                            showBadgeShelf = SetBadgeShelfVisibleMutation.ShowBadgeShelf(
                                userId = userId,
                                badgeShelf = null,
                                __typename = "Badge"
                            )
                        )
                    )
                    .execute()
                if (resp.hasErrors()) {
                    ToolsToast.show(R.string.app_error)
                }
            } catch (e: ApolloException) {
                ToolsToast.show(R.string.error_network_error)
            }
        }
    }

    private val _isLoadingShow = MutableStateFlow(false)
    val isLoadingShow = _isLoadingShow.asStateFlow()

    fun show() {
        viewModelScope.launch {
            try {
                _isLoadingShow.emit(true)
                val resp = ApolloController.apolloClient
                    .mutation(SetBadgeShelfVisibleMutation(true))
                    .execute()
                if (resp.hasErrors()) {
                    ToolsToast.show(R.string.app_error)
                }
            } catch (e: ApolloException) {
                ToolsToast.show(R.string.error_network_error)
            } finally {
                _isLoadingShow.emit(false)
            }
        }
    }
}

class BadgeShelfModelFactory(private val userId: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return BadgeShelfModel(extras[APPLICATION_KEY]!!, userId) as T
    }
}

