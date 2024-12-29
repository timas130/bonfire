package com.sayzen.campfiresdk.compose.auth

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.optimisticUpdates
import com.sayzen.campfiresdk.OAuthGrantsQuery
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.RevokeOAuthGrantMutation
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.util.combineStates
import com.sayzen.campfiresdk.compose.util.pagination.AbstractGQLPaginationModel
import com.sayzen.campfiresdk.compose.util.pagination.PaginationScreen
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.sup.dev.java.tools.ToolsDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.apollo

class OAuthGrantsModel(application: Application) : AbstractGQLPaginationModel<OAuthGrantsQuery.Oauth2Grant, OAuthGrantsQuery.Data>(application) {
    init {
        reload()
    }

    companion object {
        private const val LIMIT = 30
    }

    override fun hasNextPage(data: OAuthGrantsQuery.Data): Boolean {
        return data.oauth2Grants.size >= LIMIT
    }

    override fun toItems(data: OAuthGrantsQuery.Data): List<OAuthGrantsQuery.Oauth2Grant> {
        return data.oauth2Grants
    }

    override fun createQuery(items: List<OAuthGrantsQuery.Oauth2Grant>): Query<OAuthGrantsQuery.Data> {
        return OAuthGrantsQuery(LIMIT, items.size)
    }

    private val _errorSnackbar = MutableStateFlow<String?>(null)
    val errorSnackbar = _errorSnackbar.asStateFlow()

    private val _deletedGrants = MutableStateFlow(listOf<String>())

    override val items: StateFlow<List<OAuthGrantsQuery.Oauth2Grant>?>
        get() = super.items.combineStates(_deletedGrants) { items, deletedGrants ->
            items?.filterNot { deletedGrants.contains(it.id) }
        }

    fun clearSnackbar() {
        _errorSnackbar.value = null
    }

    fun revokeAccess(grantId: String) {
        viewModelScope.launch {
            try {
                val result = apollo.mutation(RevokeOAuthGrantMutation(grantId))
                    .optimisticUpdates(RevokeOAuthGrantMutation.Data(grantId))
                    .execute()
                if (result.hasErrors()) {
                    _errorSnackbar.value = getApplication<Application>().getString(R.string.error_unknown)
                }

                _deletedGrants.update {
                    it + grantId
                }
            } catch (e: Exception) {
                _errorSnackbar.value = getApplication<Application>().getString(R.string.error_network_error)
            }
        }
    }
}

@Composable
fun OAuthGrantsScreenC() {
    val model = viewModel(key = "OAuthGrantsModel") {
        OAuthGrantsModel(get(APPLICATION_KEY)!!)
    }

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(model) {
        model.errorSnackbar
            .filterNotNull()
            .collect {
                launch {
                    snackbarHost.showSnackbar(it)
                }
                model.clearSnackbar()
            }
    }

    PaginationScreen(
        model = model,
        title = {
            Text(stringResource(R.string.oauth_grants_title))
        },
        item = { item, shimmer ->
            ListItem(
                headlineContent = {
                    Row {
                        Text(
                            text = item?.client?.displayName ?: ".............",
                            modifier = Modifier.shimmerExt(item == null, shimmer)
                        )

                        if (item?.client?.official == true) {
                            Icon(
                                painter = painterResource(R.drawable.verified_24px),
                                contentDescription = stringResource(R.string.oauth_grants_official),
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                    }
                },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.oauth_grant_created_at)
                                .format(ToolsDate.dateToString(item?.createdAt?.millis ?: 0)),
                            modifier = Modifier
                                .shimmerExt(item == null, shimmer)
                        )

                        Text(
                            text = stringResource(R.string.oauth_grant_last_used_at)
                                .format(ToolsDate.dateToString(item?.createdAt?.millis ?: 0)),
                            modifier = Modifier
                                .shimmerExt(item == null, shimmer)
                        )
                    }
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            model.revokeAccess(item!!.id)
                        },
                        enabled = item != null,
                    ) {
                        Icon(Icons.Default.Delete, stringResource(R.string.oauth_grant_revoke))
                    }
                }
            )
        },
    )
}

class OAuthGrantsScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        OAuthGrantsScreenC()
    }
}
