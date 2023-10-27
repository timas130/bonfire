package sh.sit.bonfire.auth.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sup.dev.android.libs.screens.navigator.Navigator
import sh.sit.bonfire.auth.R

@Composable
fun BackButton() {
    IconButton(onClick = { Navigator.back() }) {
        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
    }
}
