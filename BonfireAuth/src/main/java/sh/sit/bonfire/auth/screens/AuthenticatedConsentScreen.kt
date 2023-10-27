package sh.sit.bonfire.auth.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sup.dev.android.app.SupAndroid
import sh.sit.bonfire.auth.components.NoConsentException
import sh.sit.bonfire.auth.components.useConsentController

@Composable
fun AuthenticatedConsentScreen(
    onConsent: () -> Unit,
) {
    val consentController = useConsentController()

    LaunchedEffect(Unit) {
        try {
            consentController.waitForConsent()
            onConsent()
        } catch (e: NoConsentException) {
            SupAndroid.activity!!.finish()
        }
    }
}
