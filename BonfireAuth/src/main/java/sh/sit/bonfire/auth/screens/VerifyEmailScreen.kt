package sh.sit.bonfire.auth.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sup.dev.android.libs.image_loader.ImageLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.AuthController.CanLoginResult
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.ResendVerificationMutation
import sh.sit.bonfire.auth.apollo
import sh.sit.bonfire.auth.components.FormScreen
import sh.sit.bonfire.auth.components.RemoteImage
import sh.sit.bonfire.auth.components.TextLoadingButton

@Composable
fun VerifyEmailScreen(
    email: String,
    imageLink: ImageLink,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val networkError = stringResource(R.string.error_network_error)
    val unknownError = stringResource(R.string.error_unknown)

    // updater
    LaunchedEffect(Unit) {
        while (true) {
            val canLoginResult = AuthController.getCanLogin()
            when (canLoginResult) {
                CanLoginResult.NoNetwork -> snackbarHostState.showSnackbar(networkError)
                CanLoginResult.UnknownError -> snackbarHostState.showSnackbar(unknownError)
                CanLoginResult.NotVerified -> {}
                else -> onBack()
            }
            delay(1000)
        }
    }

    var isResendingVerification by remember { mutableStateOf(false) }

    suspend fun resendVerification() {
        isResendingVerification = true
        val result = try {
            apollo.mutation(ResendVerificationMutation(email = email)).execute()
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(networkError)
            }
            isResendingVerification = false
            return
        }

        if (!result.hasErrors()) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.resend_verification_success))
            }
            isResendingVerification = false
            return
        }

        val errorCode = result.errors!!.first().message.split(':').first()

        val message = when (errorCode) {
            "AlreadyVerified" -> R.string.resend_verification_already
            "TryAgainLater" -> R.string.resend_verification_later
            else -> R.string.error_unknown
        }
        scope.launch { snackbarHostState.showSnackbar(context.getString(message)) }

        isResendingVerification = false
    }

    FormScreen(
        title = stringResource(R.string.verify_email_title),
        snackbarHostState = snackbarHostState,
    ) {
        RemoteImage(
            link = imageLink,
            contentDescription = null,
            modifier = Modifier
                .height(128.dp)
                .fillMaxWidth(),
        )

        for (resource in arrayOf(
            R.string.verify_email_text,
            R.string.verify_email_your_email,
        )) {
            Text(
                if (resource == R.string.verify_email_your_email) {
                    stringResource(resource).format(email)
                } else {
                    stringResource(resource)
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        CircularProgressIndicator(Modifier.padding(top = 8.dp, bottom = 8.dp))

        TextLoadingButton(
            onClick = {
                scope.launch {
                    resendVerification()
                }
            },
            isLoading = isResendingVerification
        ) {
            Text(stringResource(R.string.resend_verification_button))
        }
        TextButton(onClick = {
            scope.launch {
                AuthController.logout()
                onBack()
            }
        }) {
            Text(stringResource(R.string.logout_button))
        }
    }
}
