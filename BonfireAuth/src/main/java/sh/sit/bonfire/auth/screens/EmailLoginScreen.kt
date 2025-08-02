package sh.sit.bonfire.auth.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.FormScreen
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.EmailAuthFlow
import sh.sit.bonfire.auth.toUiString

@Composable
fun EmailLoginScreen(
    toForgotPassword: () -> Unit,
    toRegister: () -> Unit,
    onLogin: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val submit = {
        scope.launch {
            try {
                isLoading = true
                EmailAuthFlow(context, email, password).start()
                onLogin()
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
        title = stringResource(R.string.login_email_title),
        snackbarHostState = snackbarHostState,
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentType = ContentType.EmailAddress
                },
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.label_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions {
                submit()
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentType = ContentType.Password
                },
            visualTransformation = PasswordVisualTransformation()
        )

        TextButton(onClick = { toForgotPassword() }, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.forgot_password_button))
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = { toRegister() }) {
                Text(stringResource(R.string.register_button))
            }

            LoadingButton(onClick = submit, isLoading = isLoading) {
                Text(stringResource(R.string.login_button))
            }
        }
    }
}
