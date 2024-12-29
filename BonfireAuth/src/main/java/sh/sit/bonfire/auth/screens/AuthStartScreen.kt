package sh.sit.bonfire.auth.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.sup.dev.android.tools.ToolsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.BuildConfig
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.ConsentControllerState
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.components.NoConsentException
import sh.sit.bonfire.auth.components.useConsentController
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.GoogleAuthFlow
import sh.sit.bonfire.auth.toUiString

@Composable
fun AuthStartScreen(
    loadBackground: suspend () -> Bitmap?,
    openEmail: () -> Unit,
    onLogin: () -> Unit,
) {
    val backgroundBitmap = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(Unit) {
        try {
            backgroundBitmap.value = withContext(Dispatchers.IO) {
                loadBackground()
            }
        } catch (e: Exception) {
            Log.e("AuthStartScreen", "failed to load background")
            e.printStackTrace()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val consentController = useConsentController()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        backgroundBitmap.value?.let {
            Image(
                it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f,
            )
        }

        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 16.dp)
        ) {
            val (headline, buttons, help) = createRefs()

            HelpButton(
                Modifier.constrainAs(help) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
            )

            Column(
                Modifier
                    .constrainAs(headline) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.start_welcome_top),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.start_welcome_bottom),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                Modifier
                    .constrainAs(buttons) {
                        top.linkTo(headline.bottom, margin = 16.dp)
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                    }
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GoogleAuthButton(
                    consentController = consentController,
                    onLogin = onLogin,
                    snackbarHostState = snackbarHostState
                )

                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            try {
                                consentController.waitForConsent()
                                openEmail()
                            } catch (e: NoConsentException) {
                                snackbarHostState.showSnackbar(context.getString(R.string.start_consent_requirement))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.start_email))
                }

                if (BuildConfig.DEBUG) {
                    OutlinedButton(
                        onClick = { runBlocking { AuthController.setConsent(false) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Сбросить согласие")
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpButton(modifier: Modifier = Modifier) {
    TextButton(
        onClick = { ToolsIntent.openLink("https://bonfire.moe/help") },
        modifier = modifier,
    ) {
        Text(stringResource(R.string.support_link))
    }
}

@Composable
private fun GoogleAuthButton(
    consentController: ConsentControllerState,
    onLogin: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isGoogleLoading by remember { mutableStateOf(false) }

    val onClick = {
        scope.launch {
            try {
                consentController.waitForConsent()
                isGoogleLoading = true
                GoogleAuthFlow(context).start()
                onLogin()
            } catch (e: AuthFlow.AuthException) {
                if (e.reason == AuthFlow.FailureReason.Cancelled) return@launch
                e.printStackTrace()
                launch { snackbarHostState.showSnackbar(e.toUiString(context)) }
            } catch (e: NoConsentException) {
                launch { snackbarHostState.showSnackbar(context.getString(R.string.start_consent_requirement)) }
            } finally {
                isGoogleLoading = false
            }
        }
        Unit
    }

    LoadingButton(
        onClick = onClick,
        enabled = !isGoogleLoading,
        modifier = Modifier
            .fillMaxWidth(),
        isLoading = isGoogleLoading,
    ) {
        Text(stringResource(R.string.start_google))
    }
}
