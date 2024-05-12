package sh.sit.bonfire.auth.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sup.dev.android.tools.ToolsIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.R

class NoConsentException : Exception()

class ConsentControllerState {
    private val _isOpen = MutableStateFlow(false)
    val isOpen = _isOpen.asStateFlow()

    private val _allowAnalytics = MutableStateFlow(false)
    val allowAnalytics = _allowAnalytics.asStateFlow()

    private val _consentStatus = MutableStateFlow<Boolean?>(null)

    suspend fun waitForConsent() {
        if (AuthController.haveConsent.first()) {
            // consent already received
            return
        }

        _allowAnalytics.emit(AuthController.haveAnalyticsConsent.first())

        _consentStatus.emit(null)
        _isOpen.emit(true)

        val status = _consentStatus.first { it != null } ?: throw IllegalStateException()

        _isOpen.emit(false)
        if (status) {
            AuthController.setConsent(true)
            //AuthController.setAnalyticsConsent(_allowAnalytics.value)
            AuthController.setAnalyticsConsent(true)
        } else {
            AuthController.setConsent(false)
            AuthController.setAnalyticsConsent(false)
            throw NoConsentException()
        }
    }

    internal suspend fun decline() {
        _consentStatus.emit(false)
    }

    internal suspend fun accept() {
        _consentStatus.emit(true)
    }

    internal fun toggleAnalytics() {
        _allowAnalytics.getAndUpdate { !it }
    }
}

@Composable
fun useConsentController(): ConsentControllerState {
    val state = remember { ConsentControllerState() }
    ConsentController(state)
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsentController(state: ConsentControllerState) {
    val scope = rememberCoroutineScope()

    val isOpen = state.isOpen.collectAsState().value
    val allowAnalytics = state.allowAnalytics.collectAsState().value

    val decline = {
        scope.launch {
            state.decline()
        }
        Unit
    }
    val accept = {
        scope.launch {
            state.accept()
        }
        Unit
    }

    BetterModalBottomSheet(open = isOpen, onDismissRequest = decline, windowInsets = WindowInsets(0)) {
        Column(
            Modifier.padding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues())
        ) {
            Text(
                stringResource(R.string.start_consent_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp),
            )

            ListItem(
                leadingContent = { Icon(Icons.Default.Shield, "") },
                headlineContent = { Text(stringResource(R.string.start_privacy_policy)) },
                modifier = Modifier.clickable {
                    ToolsIntent.openLink("https://bonfire.moe/page/privacy")
                }
            )
            ListItem(
                leadingContent = { Icon(Icons.Default.HistoryEdu, "") },
                headlineContent = { Text(stringResource(R.string.start_terms)) },
                modifier = Modifier.clickable {
                    ToolsIntent.openLink("https://bonfire.moe/page/terms")
                }
            )
            ListItem(
                leadingContent = { Icon(Icons.Default.Gavel, "") },
                headlineContent = { Text(stringResource(R.string.start_app_rules)) },
                modifier = Modifier.clickable {
                    AuthController.openRules()
                }
            )
            /*
            ListItem(
                leadingContent = { Icon(Icons.Default.BarChart, "") },
                headlineContent = { Text(stringResource(R.string.consent_analytics)) },
                supportingContent = { Text(stringResource(R.string.consent_analytics_desc)) },
                trailingContent = {
                    Switch(
                        checked = allowAnalytics,
                        onCheckedChange = { state.toggleAnalytics() }
                    )
                },
                modifier = Modifier.clickable {
                    state.toggleAnalytics()
                }
            )
            */

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                OutlinedButton(onClick = decline, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.start_decline))
                }
                Button(onClick = accept, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.start_accept))
                }
            }
        }
    }
}
