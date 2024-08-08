package com.sayzen.campfiresdk.compose.attach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posthog.PostHog
import kotlinx.coroutines.launch
import java.io.File

interface AttachFlyoutDelegate {
    fun onSelectedImage(file: File)

    object Stub : AttachFlyoutDelegate {
        override fun onSelectedImage(file: File) {
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    if (sheetState.targetValue == SheetValue.Hidden &&
        sheetState.currentValue == SheetValue.Hidden &&
        !open) return

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
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { AttachFlyoutHeader(model, sheetState, onDismissRequest) },
    ) {
        val scope = rememberCoroutineScope()
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
            model.switchTab(AttachFlyoutModel.Tab.entries[pagerState.currentPage])
        }

        Box {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxHeight()
            ) { page ->
                val tab = AttachFlyoutModel.Tab.entries[page]

                when (tab) {
                    AttachFlyoutModel.Tab.Gallery -> {
                        GalleryTab(model)
                    }

                    AttachFlyoutModel.Tab.Gif -> {
                        LazyColumn {  }
                    }
                    AttachFlyoutModel.Tab.Stickers -> {
                        LazyColumn {  }
                    }
                }
            }

            val safeDrawingInsets = WindowInsets.safeDrawing
            Column(
                modifier = Modifier
                    .offset {
                        val offset = sheetState.requireOffset()
                        val inset = safeDrawingInsets.getBottom(this)
                        IntOffset(x = 0, y = -offset.fastRoundToInt() + inset)
                    }
                    .align(Alignment.BottomStart)
            ) {
                Surface {
                    Row(
                        modifier = Modifier
                            .padding(
                                WindowInsets.safeDrawing
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
    }
}
