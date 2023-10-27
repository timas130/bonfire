package sh.sit.bonfire.auth.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sup.dev.android.libs.screens.navigator.Navigator
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.components.FormScreen

@Composable
fun PasswordRecoverySentScreen() {
    FormScreen(
        title = stringResource(R.string.forgot_password_sent),
        snackbarHostState = remember { SnackbarHostState() },
    ) {
        Text(
            stringResource(R.string.forgot_password_sent_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Button(onClick = { Navigator.back() }) {
            Text(stringResource(R.string.back))
        }
    }
}
