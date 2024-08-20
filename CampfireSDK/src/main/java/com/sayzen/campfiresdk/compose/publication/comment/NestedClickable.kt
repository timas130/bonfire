package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

internal fun Modifier.nestedClickableRoot(
    onClick: (Offset) -> Unit,
    onLongClick: (Offset) -> Unit,
    longClickEnabledFlag: MutableState<Boolean>? = null,
) = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val onClickRef = rememberUpdatedState(onClick)
    val onLongClickRef = rememberUpdatedState(onLongClick)

    Modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(longClickEnabledFlag) {
            awaitEachGesture {
                // when CommentQuote is pressed, it consumes the down event.
                // we don't care though because root Comment needs to handle long clicks.
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)

                // we can show the indication if it has not been shown in CommentQuote
                val emitInteractions = !down.isConsumed
                if (emitInteractions) {
                    interactionSource.tryEmit(press)
                    down.consume()
                }

                try {
                    val up = withTimeout(viewConfiguration.longPressTimeoutMillis) {
                        waitForUpOrCancellation()
                    }
                    if (up != null) {
                        // on click
                        up.consume()
                        if (emitInteractions) {
                            interactionSource.tryEmit(PressInteraction.Release(press))
                        }
                        onClickRef.value(down.position)
                    } else {
                        // canceled (or passed to CommentQuote)
                        if (emitInteractions) {
                            interactionSource.tryEmit(PressInteraction.Cancel(press))
                        }
                    }
                } catch (e: PointerEventTimeoutCancellationException) {
                    // on long click

                    if (longClickEnabledFlag?.value == false) {
                        longClickEnabledFlag.value = true
                        return@awaitEachGesture
                    }

                    if (!emitInteractions) {
                        interactionSource.tryEmit(press)
                    }

                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClickRef.value(down.position)

                    if (waitForUpOrCancellation() != null) {
                        interactionSource.tryEmit(PressInteraction.Release(press))
                    } else {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            }
        }
}

internal fun Modifier.nestedClickable(
    pass: PointerEventPass = PointerEventPass.Main,
    onClick: (Offset) -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val onClickRef = rememberUpdatedState(onClick)

    Modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(pass = pass)
                val press = PressInteraction.Press(down.position)
                interactionSource.tryEmit(press)

                down.consume()
                val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation(pass = pass)
                }

                if (up == null) {
                    interactionSource.tryEmit(PressInteraction.Cancel(press))
                    return@awaitEachGesture
                }

                interactionSource.tryEmit(PressInteraction.Release(press))
                up.consume()

                onClickRef.value(down.position)
            }
        }
}
