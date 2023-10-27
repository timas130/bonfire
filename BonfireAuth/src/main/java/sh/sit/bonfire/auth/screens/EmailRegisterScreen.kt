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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.FormScreen
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.RegisterEmailFlow
import sh.sit.bonfire.auth.toUiString

@Composable
fun EmailRegisterScreen(onRegister: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val submit = {
        scope.launch {
            if (password != passwordRepeat) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.error_password_dont_match)
                )
                return@launch
            }

            try {
                isLoading = true
                RegisterEmailFlow(context, email, password).start()
                onRegister()
            } catch (e: AuthFlow.AuthException) {
                e.printStackTrace()
                launch { snackbarHostState.showSnackbar(e.toUiString(context)) }
            } finally {
                isLoading = false
            }
        }
        Unit
    }

    FormScreen(
        title = stringResource(R.string.register_title),
        snackbarHostState = snackbarHostState,
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                autoCorrect = false,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.label_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
        )

        TextField(
            value = passwordRepeat,
            onValueChange = { passwordRepeat = it },
            label = { Text(stringResource(R.string.label_repeat_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
        )

        LoadingButton(
            onClick = submit,
            modifier = Modifier.align(Alignment.End),
            isLoading = isLoading,
        ) {
            Text(stringResource(R.string.register_button))
        }
    }
}
