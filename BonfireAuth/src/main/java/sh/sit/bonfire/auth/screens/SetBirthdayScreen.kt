package sh.sit.bonfire.auth.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.dzen.campfire.api.ApiResources
import com.posthog.PostHog
import com.sup.dev.android.libs.screens.navigator.Navigator
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import sh.sit.bonfire.auth.ApolloController
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.SetBirthdayMutation
import sh.sit.bonfire.auth.components.FormScreen
import sh.sit.bonfire.auth.components.LoadingButton
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.toUiString
import sh.sit.bonfire.images.RemoteImage
import sh.sit.bonfire.schema.NaiveDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBirthdayScreen(onBirthdaySet: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateState = rememberDatePickerState(
        yearRange = IntRange(1900, Calendar.getInstance().get(Calendar.YEAR)),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis < System.currentTimeMillis()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= Calendar.getInstance().get(Calendar.YEAR)
            }
        }
    )

    var inputDate by remember { mutableStateOf<NaiveDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    suspend fun submit() {
        val date = inputDate ?: return

        PostHog.capture("birthday_set")

        val resp = try {
            ApolloController.apolloClient
                .mutation(SetBirthdayMutation(date))
                .execute()
        } catch (e: Exception) {
            e.printStackTrace()
            snackbarHostState.showSnackbar(context.getString(R.string.error_network_error))
            return
        }

        if (resp.hasErrors()) {
            val exception = AuthFlow.AuthException.fromError(resp.errors!!.first())
            snackbarHostState.showSnackbar(exception.toUiString(context))
            return
        }

        Navigator.back()
        onBirthdaySet()
    }

    LaunchedEffect(dateState.selectedDateMillis) {
        inputDate = dateState.selectedDateMillis?.let {
            NaiveDate(LocalDate.fromDateFields(Date(it)))
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = dateState, showModeToggle = true)
        }
    }

    FormScreen(
        title = stringResource(R.string.set_birthday_title),
        snackbarHostState = snackbarHostState
    ) {
        RemoteImage(
            link = ApiResources.IMAGE_BACKGROUND_14,
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.set_birthday_text),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // fixme: a11y
        Box {
            TextField(
                label = {
                    Text(stringResource(R.string.birthday_label))
                },
                value = inputDate?.toString() ?: "",
                onValueChange = {},
                enabled = true,
                singleLine = true,
            )

            Box(Modifier
                .matchParentSize()
                .clickable {
                    showDialog = true
                })
        }

        var isLoading by remember { mutableStateOf(false) }
        LoadingButton(
            onClick = {
                isLoading = true
                scope.launch {
                    submit()
                }.invokeOnCompletion {
                    isLoading = false
                }
            },
            enabled = inputDate != null,
            isLoading = isLoading,
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
