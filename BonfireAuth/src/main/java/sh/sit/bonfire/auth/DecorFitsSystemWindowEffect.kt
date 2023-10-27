package sh.sit.bonfire.auth

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

private val effectStack = MutableStateFlow(0)

@Composable
fun DecorFitsSystemWindowEffect() {
    DisposableEffect(Unit) {
        effectStack.update { it + 1 }

        val window = SupAndroid.activity!!.window
        val isDark = ToolsResources.getColorAttr(R.attr.colorOnPrimary) == Color.WHITE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).run {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        onDispose {
            // ugly hack to transition between screens correctly.
            // this is required because when switching screens, compose first
            // composes the destination, and then disposes the source.
            if (effectStack.updateAndGet { it - 1 } > 0) {
                return@onDispose
            }
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }
}
