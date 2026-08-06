package com.sayzen.campfiresdk.video.player

import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * The display cutout as padding for content that has to stay clear of it.
 *
 * `WindowInsets.displayCutout` is useless here: the screen container returns
 * `WindowInsetsCompat.CONSUMED` (see `SActivity.listenForWindowInsetsOnView`),
 * so everything below it — this composable included — is told the insets are
 * zero. Normally that is fine, because that same listener already inset the
 * container by the system bars. The player is the exception: it hides the
 * system bars and draws edge to edge, so nothing else is keeping its controls
 * out from under the notch, and it has to read the cutout off the window root
 * itself.
 *
 * Layout changes are the refresh signal, since a rotation — the one thing that
 * moves a cutout from one edge to another — always relays the view.
 */
@Composable
internal fun rememberDisplayCutoutPadding(): PaddingValues {
    val view = LocalView.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var insets by remember { mutableStateOf(Insets.NONE) }

    DisposableEffect(view) {
        fun update() {
            insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.displayCutout())
                ?: Insets.NONE
        }
        update()

        val listener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> update() }
        view.addOnLayoutChangeListener(listener)
        onDispose { view.removeOnLayoutChangeListener(listener) }
    }

    return remember(insets, density, layoutDirection) {
        val rtl = layoutDirection == LayoutDirection.Rtl
        with(density) {
            PaddingValues(
                start = (if (rtl) insets.right else insets.left).toDp(),
                top = insets.top.toDp(),
                end = (if (rtl) insets.left else insets.right).toDp(),
                bottom = insets.bottom.toDp(),
            )
        }
    }
}
