package sh.sit.bonfire.auth.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.FormScreen
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.SendPasswordRecoveryFlow
import sh.sit.bonfire.auth.toUiString

@Composable
fun PasswordRecoveryScreen(
    onSubmit: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val submit = suspend {
        try {
            isLoading = true
            SendPasswordRecoveryFlow(context, email).start()
            onSubmit()
        } catch (e: AuthFlow.AuthException) {
            e.printStackTrace()
            scope.launch { snackbarHostState.showSnackbar(e.toUiString(context)) }
        } finally {
            isLoading = false
        }
    }

    FormScreen(
        title = stringResource(R.string.forgot_password_button),
        snackbarHostState = snackbarHostState,
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                autoCorrect = false,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LoadingButton(
            modifier = Modifier.align(Alignment.End),
            onClick = { scope.launch { submit() } },
            isLoading = isLoading,
        ) {
            Text(stringResource(R.string.forgot_password_send))
        }
    }
}
