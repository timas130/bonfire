package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posthog.PostHog
import com.sayzen.campfiresdk.compose.util.AnimatedNullableVisibility
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AttachFlyout(
    open: Boolean,
    onDismissRequest: () -> Unit,
    delegate: AttachFlyoutDelegate = AttachFlyoutDelegate.Stub,
) {
    val model = viewModel {
        AttachFlyoutModel(get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY)!!)
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (sheetState.targetValue == SheetValue.Hidden &&
        sheetState.currentValue == SheetValue.Hidden &&
        !open) return

    val activeGifPopup by model.activeGifPopup.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { activeGifPopup }
            .filterNotNull()
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    sheetState.animateTo(SheetValue.Expanded)
                }
            }
    }
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.targetValue }
            .filter { it != SheetValue.Expanded }
            .distinctUntilChanged()
            .collect {
                model.closeGifPopup()
            }
    }

    LaunchedEffect(open) {
        if (open) {
            PostHog.capture(
                event = "attach flyout open",
                properties = mapOf("tab" to model.activeTab.value.name),
                userPropertiesSetOnce = mapOf("opened attach flyout" to true)
            )
            sheetState.show()
        } else {
            sheetState.hide()
            model.onClosed()
        }
    }

    val isImeVisible = WindowInsets.isImeVisible
    val openedImage by model.openedImage.collectAsState()
    val shouldDismissOnBackPress by derivedStateOf {
        !isImeVisible && activeGifPopup == null && openedImage == null
    }

    ModalBottomSheetExt(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = shouldDismissOnBackPress
        ),
        dragHandle = {
            AttachFlyoutHeader(model, sheetState, onDismissRequest)
        },
        overlay = {
            AttachFlyoutOverlay(model)
        }
    ) {
        val pagerState = rememberPagerState(pageCount = { AttachFlyoutModel.Tab.entries.size })
        val activeTab by model.activeTab.collectAsState()

        LaunchedEffect(activeTab) {
            if (activeTab.ordinal != pagerState.currentPage) {
                scope.launch {
                    pagerState.animateScrollToPage(activeTab.ordinal)
                }
            }
        }
        LaunchedEffect(pagerState.currentPage) {
            model.switchTab(AttachFlyoutModel.Tab.entries[pagerState.currentPage], userAction = true)
        }

        Box {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                userScrollEnabled = model.pagerScrollAllowed.collectAsState().value,
                modifier = Modifier.fillMaxHeight()
            ) { page ->
                val tab = AttachFlyoutModel.Tab.entries[page]

                when (tab) {
                    AttachFlyoutModel.Tab.Gallery -> {
                        GalleryTab(model, this@ModalBottomSheetExt)
                    }

                    AttachFlyoutModel.Tab.Gif -> {
                        GifTab(model, this@ModalBottomSheetExt)
                    }

                    AttachFlyoutModel.Tab.Stickers -> {
                        LazyColumn { }
                    }
                }
            }

            AttachFlyoutTabsWrapper(sheetState, activeTab, model, pagerState)

            AnimatedNullableVisibility(
                value = activeGifPopup,
                modifier = Modifier
                    .matchParentSize()
                    .sheetPadding(sheetState)
            ) {
                AttachGifPopup(
                    activePopup = it,
                    model = model,
                    sharedTransitionScope = this@ModalBottomSheetExt
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.AttachFlyoutTabsWrapper(
    sheetState: SheetState,
    activeTab: AttachFlyoutModel.Tab,
    model: AttachFlyoutModel,
    pagerState: PagerState
) {
    val safeDrawingInsets = WindowInsets.safeDrawing
    Surface(
        modifier = Modifier
            .sheetPadding(sheetState)
            .offset {
                IntOffset(x = 0, y = safeDrawingInsets.getBottom(this))
            }
            .align(Alignment.BottomStart)
    ) {
        Column {
            AnimatedVisibility(activeTab == AttachFlyoutModel.Tab.Gif) {
                AttachGifFooter(model)
            }

            Row(
                modifier = Modifier
                    .padding(
                        safeDrawingInsets
                            .only(WindowInsetsSides.Bottom)
                            .asPaddingValues()
                    )
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AttachFlyoutTabs(model, pagerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.sheetPadding(sheetState: SheetState) = composed {
    val density = LocalDensity.current

    padding(object : PaddingValues {
        override fun calculateBottomPadding(): Dp {
            return with(density) {
                try {
                    sheetState.requireOffset().toDp()
                } catch (e: IllegalStateException) {
                    0.dp
                }
            }
        }

        override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
            return 0.dp
        }

        override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
            return 0.dp
        }

        override fun calculateTopPadding(): Dp {
            return 0.dp
        }
    })
}
