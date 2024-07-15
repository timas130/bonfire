package sh.sit.bonfire.auth.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.cache.normalized.watch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.tools.ToolsDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.*
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.BackButton
import sh.sit.bonfire.auth.components.BetterModalBottomSheet
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.components.LoadingSplash
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.BindGoogleFlow
import sh.sit.bonfire.networking.OkHttpController

@Composable
fun ChangeBar(
    onClose: () -> Unit,
    onChange: () -> Unit,
    isLoading: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        TextButton(
            onClick = onClose,
            enabled = !isLoading,
        ) {
            Text(stringResource(R.string.cancel))
        }

        LoadingButton(onClick = onChange, isLoading = isLoading) {
            Text(stringResource(R.string.security_change_do))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailChangeSheet(open: Boolean, close: () -> Unit, onChange: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var newEmail by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val submit = suspend {
        try {
            isLoading = true
            val resp = apollo.mutation(ChangeEmailMutation(newEmail)).execute()
            if (resp.hasErrors()) {
                val error = AuthFlow.AuthException.fromError(resp.errors!!.first())
                ToolsToast.show(error.toUiString(context))
            } else {
                close()
                ToolsToast.show(R.string.security_email_change_done)

                val currentAuthState = AuthController.authState.first() ?: AuthController.NoneAuthState
                AuthController.saveAuthState(
                    when (currentAuthState) {
                        is AuthController.AuthenticatedAuthState -> currentAuthState.copy(email = newEmail)
                        else -> currentAuthState
                    }
                )

                onChange()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToolsToast.show(R.string.error_network_error)
        } finally {
            isLoading = false
        }
    }

    BetterModalBottomSheet(
        open = open,
        onDismissRequest = close,
    ) {
        Text(
            stringResource(R.string.security_email_change_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        TextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text(stringResource(R.string.security_email_change_new)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        ChangeBar(
            onClose = { close() },
            onChange = { scope.launch { submit() } },
            isLoading = isLoading,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeSheet(open: Boolean, close: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val submit = suspend {
        try {
            isLoading = true
            val resp = apollo.mutation(ChangePasswordMutation(oldPassword, newPassword)).execute()
            if (resp.hasErrors()) {
                val error = AuthFlow.AuthException.fromError(resp.errors!!.first())
                ToolsToast.show(error.toUiString(context))
            } else {
                close()
                ToolsToast.show(R.string.security_password_change_done)
            }
        } catch (e: Exception) {
            ToolsToast.show(R.string.error_network_error)
        } finally {
            isLoading = false
        }
    }

    BetterModalBottomSheet(
        onDismissRequest = close,
        open = open,
    ) {
        Text(
            stringResource(R.string.security_password_change_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        TextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            label = { Text(stringResource(R.string.security_password_change_old)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next,
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        TextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text(stringResource(R.string.security_password_change_new)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done,
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        ChangeBar(
            onClose = close,
            onChange = { scope.launch { submit() } },
            isLoading = isLoading,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSecurityScreen(
    onChangeEmail: () -> Unit,
    onChangeBirthday: () -> Unit,
) {
    DecorFitsSystemWindowEffect()

    val dataFlow = remember { apollo.query(AccountSecurityQuery()).watch() }
    val dataState = dataFlow.collectAsState(initial = null).value

    if (dataState == null) {
        LoadingSplash()
        return
    }

    val data = dataState.data
    LaunchedEffect(data) {
        if (data == null) Navigator.back()
    }
    if (data == null) {
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var emailChangeOpen by remember { mutableStateOf(false) }
    EmailChangeSheet(
        open = emailChangeOpen,
        close = { emailChangeOpen = false },
        onChange = onChangeEmail
    )

    var passwordChangeOpen by remember { mutableStateOf(false) }
    PasswordChangeSheet(
        open = passwordChangeOpen,
        close = { passwordChangeOpen = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(stringResource(R.string.security_title)) }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                SettingsListItem(
                    icon = Icons.Default.Email,
                    headlineContent = stringResource(R.string.security_email),
                    supportingContent = data.me.email ?: stringResource(R.string.security_email_not_set),
                    onClick = { emailChangeOpen = true }
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Default.Password,
                    headlineContent = stringResource(R.string.security_password),
                    supportingContent = stringResource(R.string.security_password_button),
                    onClick = { passwordChangeOpen = true }
                )
            }
            item {
                BindGoogleListItem(data, snackbarHostState)
            }
            item {
                MigrationListItem(data)
            }
            item {
                SettingsListItem(
                    icon = Icons.Default.Cake,
                    headlineContent = stringResource(R.string.birthday_label),
                    supportingContent = if (data.me.birthday != null) {
                        data.me.birthday.date.toString()
                    } else {
                        stringResource(R.string.birthday_not_specified)
                    },
                    onClick = {
                        if (data.me.birthday != null) {
                            ToolsToast.show(R.string.birthday_not_changeable)
                        } else {
                            onChangeBirthday()
                        }
                    }
                )
            }

            item {
                Text(
                    stringResource(R.string.security_sessions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                )
            }
            items(data.activeSessions, key = { it.id }) { session ->
                SessionListItem(session, snackbarHostState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.SessionListItem(
    session: AccountSecurityQuery.ActiveSession,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }

    val terminate = suspend {
        try {
            isLoading = true
            val resp = apollo.mutation(TerminateSessionMutation(session.id))
                .execute()

            if (resp.hasErrors()) {
                snackbarHostState.showSnackbar(
                    AuthFlow.AuthException.fromError(resp.errors!!.first()).toUiString(context)
                )
            } else {
                scope.launch { apollo.query(AccountSecurityQuery()).execute() }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.error_network_error))
            }
        } finally {
            isLoading = false
        }
    }

    ListItem(
        headlineContent = {
            Text(
                session.userAgent?.let { OkHttpController.parseUserAgent(it) }
                    ?: stringResource(R.string.security_sessions_unknown)
            )
        },
        supportingContent = {
            Text(
                stringResource(R.string.security_sessions_ip).format(session.ip) + "\n" +
                stringResource(R.string.security_sessions_active).format(
                    ToolsDate.dateToString(session.lastActive.millis)
                )
            )
        },
        trailingContent = {
            if (!session.active) return@ListItem
            AnimatedContent(isLoading, label = "SessionTerminating") { state ->
                if (state) {
                    CircularProgressIndicator()
                } else {
                    IconButton(onClick = { scope.launch { terminate() } }, enabled = !session.current) {
                        Icon(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.security_sessions_logout))
                    }
                }
            }
        },
        modifier = Modifier.animateItemPlacement()
    )
}

@Composable
private fun SettingsListItem(
    icon: ImageVector,
    headlineContent: String,
    supportingContent: String,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = { Icon(icon, null) },
        headlineContent = { Text(headlineContent) },
        supportingContent = {
            Text(supportingContent)
        },
        modifier = Modifier
            .clickable {
                onClick()
            }
    )
}

@Composable
private fun MigrationListItem(data: AccountSecurityQuery.Data) {
    ListItem(
        leadingContent = { Icon(Icons.Default.Hiking, null) },
        headlineContent = { Text(stringResource(R.string.security_firebase)) },
        supportingContent = {
            if (data.me.securitySettings.firebaseLinked) {
                Text(stringResource(R.string.security_firebase_linked))
            } else {
                Text(stringResource(R.string.security_firebase_not_linked))
            }
        },
        modifier = Modifier
            .clickable {
                ToolsToast.show(
                    if (data.me.securitySettings.firebaseLinked) {
                        R.string.security_firebase_toast_linked
                    } else {
                        R.string.security_firebase_toast_not_linked
                    }
                )
            }
    )
}

@Composable
private fun BindGoogleListItem(
    data: AccountSecurityQuery.Data,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isBindingGoogle by remember { mutableStateOf(false) }
    val bindGoogle = suspend {
        try {
            isBindingGoogle = true
            BindGoogleFlow(context).start()
        } catch (e: AuthFlow.AuthException) {
            scope.launch { snackbarHostState.showSnackbar(e.toUiString(context)) }
        } finally {
            isBindingGoogle = false
        }
    }

    ListItem(
        leadingContent = { Icon(Icons.Default.GMobiledata, null) },
        headlineContent = { Text(stringResource(R.string.security_google)) },
        supportingContent = {
            if (data.me.securitySettings.googleLinked) {
                Text(stringResource(R.string.security_google_linked))
            } else {
                Text(stringResource(R.string.security_google_not_linked))
            }
        },
        trailingContent = {
            if (isBindingGoogle) {
                CircularProgressIndicator()
            }
        },
        modifier = Modifier
            .clickable {
                if (data.me.securitySettings.googleLinked) return@clickable
                scope.launch {
                    bindGoogle()
                }
            },
    )
}
