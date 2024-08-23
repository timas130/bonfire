package com.sayzen.campfiresdk.compose.attach

import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.view.*
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

private val PredictiveBackEasing: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

internal object PredictiveBack {
    internal fun transform(progress: Float) = PredictiveBackEasing.transform(progress)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalBottomSheetDialog(
    onDismissRequest: () -> Unit,
    properties: ModalBottomSheetProperties,
    predictiveBackProgress: Animatable<Float, AnimationVector1D>,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val composition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val dialogId = rememberSaveable { UUID.randomUUID() }
    val scope = rememberCoroutineScope()
    val darkThemeEnabled = isSystemInDarkTheme()
    val dialog =
        remember(view, density) {
            ModalBottomSheetDialogWrapper(
                onDismissRequest,
                properties,
                view,
                layoutDirection,
                density,
                dialogId,
                predictiveBackProgress,
                scope,
                darkThemeEnabled,
            )
                .apply {
                    setContent(composition) {
                        Box(
                            Modifier.semantics { dialog() },
                        ) {
                            currentContent()
                        }
                    }
                }
        }

    DisposableEffect(dialog) {
        dialog.show()

        onDispose {
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }

    SideEffect {
        dialog.updateParameters(
            onDismissRequest = onDismissRequest,
            properties = properties,
            layoutDirection = layoutDirection
        )
    }
}

// Fork of androidx.compose.ui.window.DialogLayout
// Additional parameters required for current predictive back implementation.
@Suppress("ViewConstructor")
private class ModalBottomSheetDialogLayout(
    context: Context,
    override val window: Window,
    shouldDismissOnBackPress: Boolean,
    private val onDismissRequest: () -> Unit,
    private val predictiveBackProgress: Animatable<Float, AnimationVector1D>,
    private val scope: CoroutineScope,
) : AbstractComposeView(context), DialogWindowProvider {

    var shouldDismissOnBackPress = shouldDismissOnBackPress
        private set

    private var content: @Composable () -> Unit by mutableStateOf({})

    private var backCallback: Any? = null

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
        createComposition()
    }

    // Display width and height logic removed, size will always span fillMaxSize().

    @Composable
    override fun Content() {
        content()
    }

    // Existing predictive back behavior below.
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        maybeRegisterBackCallback()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        maybeUnregisterBackCallback()
    }

    private fun maybeRegisterBackCallback() {
        if (!shouldDismissOnBackPress || Build.VERSION.SDK_INT < 33) {
            return
        }
        if (backCallback == null) {
            backCallback =
                if (Build.VERSION.SDK_INT >= 34) {
                    Api34Impl.createBackCallback(onDismissRequest, predictiveBackProgress, scope)
                } else {
                    Api33Impl.createBackCallback(onDismissRequest)
                }
        }
        Api33Impl.maybeRegisterBackCallback(this, backCallback)
    }

    private fun maybeUnregisterBackCallback() {
        if (Build.VERSION.SDK_INT >= 33) {
            Api33Impl.maybeUnregisterBackCallback(this, backCallback)
        }
        backCallback = null
    }

    fun setShouldDismissOnBackPress(shouldDismissOnBackPress: Boolean) {
        this.shouldDismissOnBackPress = shouldDismissOnBackPress
        maybeUnregisterBackCallback()
        if (shouldDismissOnBackPress) {
            maybeRegisterBackCallback()
        }
    }

    @RequiresApi(34)
    private object Api34Impl {
        @JvmStatic
        @DoNotInline
        fun createBackCallback(
            onDismissRequest: () -> Unit,
            predictiveBackProgress: Animatable<Float, AnimationVector1D>,
            scope: CoroutineScope
        ) =
            object : OnBackAnimationCallback {
                override fun onBackStarted(backEvent: BackEvent) {
                    scope.launch {
                        predictiveBackProgress.snapTo(PredictiveBack.transform(backEvent.progress))
                    }
                }

                override fun onBackProgressed(backEvent: BackEvent) {
                    scope.launch {
                        predictiveBackProgress.snapTo(PredictiveBack.transform(backEvent.progress))
                    }
                }

                override fun onBackInvoked() {
                    onDismissRequest()
                }

                override fun onBackCancelled() {
                    scope.launch { predictiveBackProgress.animateTo(0f) }
                }
            }
    }

    @RequiresApi(33)
    private object Api33Impl {
        @JvmStatic
        @DoNotInline
        fun createBackCallback(onDismissRequest: () -> Unit) =
            OnBackInvokedCallback(onDismissRequest)

        @JvmStatic
        @DoNotInline
        fun maybeRegisterBackCallback(view: View, backCallback: Any?) {
            if (backCallback is OnBackInvokedCallback) {
                view
                    .findOnBackInvokedDispatcher()
                    ?.registerOnBackInvokedCallback(
                        OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                        backCallback
                    )
            }
        }

        @JvmStatic
        @DoNotInline
        fun maybeUnregisterBackCallback(view: View, backCallback: Any?) {
            if (backCallback is OnBackInvokedCallback) {
                view.findOnBackInvokedDispatcher()?.unregisterOnBackInvokedCallback(backCallback)
            }
        }
    }
}

// Fork of androidx.compose.ui.window.DialogWrapper.
// predictiveBackProgress and scope params added for predictive back implementation.
// EdgeToEdgeFloatingDialogWindowTheme provided to allow theme to extend into status bar.
@ExperimentalMaterial3Api
private class ModalBottomSheetDialogWrapper(
    private var onDismissRequest: () -> Unit,
    private var properties: ModalBottomSheetProperties,
    private val composeView: View,
    layoutDirection: LayoutDirection,
    density: Density,
    dialogId: UUID,
    predictiveBackProgress: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope,
    darkThemeEnabled: Boolean,
) :
    ComponentDialog(
        ContextThemeWrapper(
            composeView.context,
            androidx.compose.material3.R.style.EdgeToEdgeFloatingDialogWindowTheme
        )
    ),
    ViewRootForInspector {

    private val dialogLayout: ModalBottomSheetDialogLayout

    // On systems older than Android S, there is a bug in the surface insets matrix math used by
    // elevation, so high values of maxSupportedElevation break accessibility services: b/232788477.
    private val maxSupportedElevation = 8.dp

    override val subCompositionView: AbstractComposeView
        get() = dialogLayout

    init {
        val window = window ?: error("Dialog has no window")
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        dialogLayout =
            ModalBottomSheetDialogLayout(
                context,
                window,
                properties.shouldDismissOnBackPress,
                onDismissRequest,
                predictiveBackProgress,
                scope,
            )
                .apply {
                    // Set unique id for AbstractComposeView. This allows state restoration for the
                    // state
                    // defined inside the Dialog via rememberSaveable()
                    setTag(androidx.compose.ui.R.id.compose_view_saveable_id_tag, "Dialog:$dialogId")
                    // Enable children to draw their shadow by not clipping them
                    clipChildren = false
                    // Allocate space for elevation
                    with(density) { elevation = maxSupportedElevation.toPx() }
                    // Simple outline to force window manager to allocate space for shadow.
                    // Note that the outline affects clickable area for the dismiss listener. In
                    // case of
                    // shapes like circle the area for dismiss might be to small (rectangular
                    // outline
                    // consuming clicks outside of the circle).
                    outlineProvider =
                        object : ViewOutlineProvider() {
                            override fun getOutline(view: View, result: Outline) {
                                result.setRect(0, 0, view.width, view.height)
                                // We set alpha to 0 to hide the view's shadow and let the
                                // composable to draw
                                // its own shadow. This still enables us to get the extra space
                                // needed in the
                                // surface.
                                result.alpha = 0f
                            }
                        }
                }
        // Clipping logic removed because we are spanning edge to edge.

        setContentView(dialogLayout)
        dialogLayout.setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        dialogLayout.setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        dialogLayout.setViewTreeSavedStateRegistryOwner(
            composeView.findViewTreeSavedStateRegistryOwner()
        )

        // Initial setup
        updateParameters(onDismissRequest, properties, layoutDirection)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkThemeEnabled
            isAppearanceLightNavigationBars = !darkThemeEnabled
        }
        // Due to how the onDismissRequest callback works
        // (it enforces a just-in-time decision on whether to update the state to hide the dialog)
        // we need to unconditionally add a callback here that is always enabled,
        // meaning we'll never get a system UI controlled predictive back animation
        // for these dialogs
        onBackPressedDispatcher.addCallback(this) {
            if (properties.shouldDismissOnBackPress) {
                onDismissRequest()
            }
        }
    }

    private fun setLayoutDirection(layoutDirection: LayoutDirection) {
        dialogLayout.layoutDirection =
            when (layoutDirection) {
                LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
                LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
            }
    }

    fun setContent(parentComposition: CompositionContext, children: @Composable () -> Unit) {
        dialogLayout.setContent(parentComposition) {
            CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides this) {
                children()
            }
        }
    }

    private fun setSecurePolicy(securePolicy: SecureFlagPolicy) {
        val secureFlagEnabled =
            securePolicy.shouldApplySecureFlag(composeView.isFlagSecureEnabled())
        window!!.setFlags(
            if (secureFlagEnabled) {
                WindowManager.LayoutParams.FLAG_SECURE
            } else {
                WindowManager.LayoutParams.FLAG_SECURE.inv()
            },
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun updateParameters(
        onDismissRequest: () -> Unit,
        properties: ModalBottomSheetProperties,
        layoutDirection: LayoutDirection
    ) {
        this.onDismissRequest = onDismissRequest
        this.properties = properties
        setSecurePolicy(properties.securePolicy)
        setLayoutDirection(layoutDirection)

        // fix for https://issuetracker.google.com/330202421
        this.dialogLayout.setShouldDismissOnBackPress(properties.shouldDismissOnBackPress)

        // Window flags to span parent window.
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
        )
        window?.setSoftInputMode(
            if (Build.VERSION.SDK_INT >= 30) {
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            } else {
                @Suppress("DEPRECATION") WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            },
        )
    }

    fun disposeComposition() {
        dialogLayout.disposeComposition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        if (result) {
            onDismissRequest()
        }

        return result
    }

    override fun cancel() {
        // Prevents the dialog from dismissing itself
        return
    }
}

internal fun View.isFlagSecureEnabled(): Boolean {
    val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
    if (windowParams != null) {
        return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
    return false
}

// Taken from AndroidPopup.android.kt
private fun SecureFlagPolicy.shouldApplySecureFlag(isSecureFlagSetOnParent: Boolean): Boolean {
    return when (this) {
        SecureFlagPolicy.SecureOff -> false
        SecureFlagPolicy.SecureOn -> true
        SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
    }
}
