package com.sayzen.campfiresdk.compose.attach

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.sayzen.campfiresdk.R

@Composable
internal fun AttachFlyoutTabs(model: AttachFlyoutModel, pagerState: PagerState, modifier: Modifier = Modifier) {
    val activeTab by model.activeTab.collectAsState()

    ScrollableTabRowExt(
        selectedTabIndex = activeTab.ordinal,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorLayout { measurable, constraints, tabPositions ->
                    val layoutInfo = pagerState.layoutInfo
                    val visiblePages = layoutInfo.visiblePagesInfo

                    fun TabPosition.contentOffset(): Dp = left + (width - contentWidth) / 2

                    if (visiblePages.size == 1) {
                        val page = visiblePages.single()
                        val tabPos = tabPositions[page.index]

                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = tabPos.contentWidth.roundToPx(),
                                maxWidth = tabPos.contentWidth.roundToPx(),
                            )
                        )
                        val tabContentOffset = tabPos.contentOffset()

                        return@tabIndicatorLayout layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(tabContentOffset.roundToPx(), 0)
                        }
                    } else {
                        val pages = visiblePages.sortedBy { it.index }
                        val page1 = pages[0]
                        val page2 = pages[1]
                        val tab1 = tabPositions[page1.index]
                        val tab2 = tabPositions[page2.index]
                        val size = layoutInfo.viewportSize

                        // determine how much of the next page is visible
                        val progress = (size.width - page2.offset) / size.width.toFloat()

                        // interpolate indicator width
                        val width = (tab1.contentWidth + (tab2.contentWidth - tab1.contentWidth) * progress).roundToPx()

                        // interpolate indicator offset
                        val tab1Offset = tab1.contentOffset()
                        val tab2Offset = tab2.contentOffset()
                        val offset = (tab1Offset + (tab2Offset - tab1Offset) * progress).roundToPx()

                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = width,
                                maxWidth = width,
                            )
                        )

                        return@tabIndicatorLayout layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(offset, 0)
                        }
                    }
                },
                width = Dp.Unspecified,
            )
        },
        modifier = modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
    ) {
        AttachFlyoutModel.Tab.entries.forEach { tab ->
            Tab(
                selected = tab == activeTab,
                onClick = { model.switchTab(tab) },
                text = {
                    Text(
                        when (tab) {
                            AttachFlyoutModel.Tab.Gallery -> stringResource(R.string.attach_tab_gallery)
                            AttachFlyoutModel.Tab.Gif -> stringResource(R.string.attach_tab_gif)
                            AttachFlyoutModel.Tab.Stickers -> stringResource(R.string.attach_tab_stickers)
                        }
                    )
                },
            )
        }
    }
}
