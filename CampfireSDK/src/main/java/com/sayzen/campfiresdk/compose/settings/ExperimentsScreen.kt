package com.sayzen.campfiresdk.compose.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import sh.sit.bonfire.auth.DecorFitsSystemWindowEffect
import sh.sit.bonfire.auth.components.BackButton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.exitProcess

private data class Experiment(
    val id: String,
    val name: Int,
    val description: Int,
    val restartRequired: Boolean = true,
)

private val activeExperiments = listOf(
    Experiment("compose_post", R.string.compose_post_title, R.string.compose_post_description),
    Experiment("compose_comment", R.string.compose_comment_title, R.string.compose_comment_description)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeExperimentsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    val someState = remember { mutableIntStateOf(0) }
    someState.intValue

    var isLoading by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    fun switch(experiment: Experiment, setTo: Boolean) {
        if (isLoading) return
        isLoading = true

        PostHog.capture(
            event = "\$feature_enrollment_update",
            properties = mapOf(
                "\$feature_flag" to experiment.id,
                "\$feature_enrollment" to setTo,
            ),
            userProperties = mapOf(
                "\$feature_enrollment/${experiment.id}" to setTo,
            )
        )
        PostHog.flush()

        scope.launch {
            val waitSnackbarJob = launch {
                snackbarHost.showSnackbar(
                    message = context.getString(R.string.experiment_wait_snackbar),
                    duration = SnackbarDuration.Indefinite,
                )
            }
            try {
                // FIXME: no way to wait until posthog finishes sending event
                delay(5000)
                val featureEnabled = withTimeout(5000) {
                    suspendCoroutine { continuation ->
                        PostHog.reloadFeatureFlags {
                            continuation.resume(PostHog.isFeatureEnabled(experiment.id))
                        }
                    }
                }

                someState.intValue++
                waitSnackbarJob.cancel()
                isLoading = false
                if (featureEnabled == setTo) {
                    if (experiment.restartRequired) {
                        showRestartDialog = true
                    }

                    if (featureEnabled) {
                        snackbarHost.showSnackbar(context.getString(R.string.experiment_enrolled_snackbar))
                    } else {
                        snackbarHost.showSnackbar(context.getString(R.string.experiment_left_snackbar))
                    }
                } else {
                    snackbarHost.showSnackbar(context.getString(R.string.experiment_error_snackbar))
                }
            } catch (e: TimeoutCancellationException) {
                someState.intValue++
                waitSnackbarJob.cancel()
                isLoading = false
                snackbarHost.showSnackbar(context.getString(R.string.experiment_error_snackbar))
            }
        }
    }

    if (showRestartDialog) {
        RestartDialog()
    }

    DecorFitsSystemWindowEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton()
                },
                title = {
                    Text(text = stringResource(R.string.experiments_title))
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHost)
        }
    ) { contentPadding ->
        LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
            item {
                if (PostHog.isOptOut()) {
                    Surface(Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = stringResource(R.string.experiments_opted_out),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            items(activeExperiments) { experiment ->
                val enabled = PostHog.isFeatureEnabled(experiment.id)

                ListItem(
                    headlineContent = {
                        Text(stringResource(experiment.name))
                    },
                    supportingContent = {
                        Text(stringResource(experiment.description))
                    },
                    trailingContent = {
                        Switch(
                            checked = enabled,
                            onCheckedChange = { switch(experiment, it) },
                            enabled = !PostHog.isOptOut() && !isLoading,
                        )
                    },
                    modifier = Modifier.clickable(enabled = !PostHog.isOptOut() && !isLoading) {
                        switch(experiment, !enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun RestartDialog() {
    val context = LocalContext.current

    AlertDialog(
        title = {
            Text(stringResource(R.string.restart_title))
        },
        text = {
            Text(stringResource(R.string.restart_text))
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
                mainIntent.setPackage(context.packageName)
                context.startActivity(mainIntent)
                exitProcess(0)
            }) {
                Text(stringResource(R.string.restart_button))
            }
        }
    )
}

class ExperimentsScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        ComposeExperimentsScreen()
    }
}
