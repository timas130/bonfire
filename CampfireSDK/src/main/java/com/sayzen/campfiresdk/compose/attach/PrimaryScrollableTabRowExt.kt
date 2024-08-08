package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

// here until https://issuetracker.google.com/221790285 is fixed
// Feb 28, 2022 - Jul 04, 2024 (yup)

private val ScrollableTabRowMinimumTabWidth = 90.dp
internal val HorizontalTextPadding = 16.dp
private val ScrollableTabRowScrollSpec: AnimationSpec<Float> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

data class TabPosition(val left: Dp, val width: Dp, val contentWidth: Dp) {
    val right: Dp
        get() = left + width
}

interface TabIndicatorScope {
    fun Modifier.tabIndicatorLayout(
        measure:
        MeasureScope.(
            Measurable,
            Constraints,
            List<TabPosition>,
        ) -> MeasureResult
    ): Modifier
}

private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
        selectedTab: Int
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)
                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec
                        )
                    }
                }
            }
        }
    }

    private fun TabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>
    ): Int =
        with(density) {
            val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
            val visibleWidth = totalTabRowWidth - scrollState.maxValue
            val tabOffset = left.roundToPx()
            val scrollerCenter = visibleWidth / 2
            val tabWidth = width.roundToPx()
            val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
            // How much space we have to scroll. If the visible width is <= to the total width, then
            // we have no space to scroll as everything is always visible.
            val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
            return centeredTabOffset.coerceIn(0, availableSpace)
        }
}

@Composable
fun ScrollableTabRowExt(
    selectedTabIndex: Int,
    indicator: @Composable TabIndicatorScope.() -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    edgePadding: Dp = 0.dp,
    scrollState: ScrollState = rememberScrollState(),
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit
) {
    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.Center)
            .horizontalScroll(scrollState)
            .selectableGroup()
            .clipToBounds(),
        color = containerColor,
        contentColor = contentColor
    ) {
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData =
            remember(scrollState, coroutineScope) {
                ScrollableTabData(scrollState = scrollState, coroutineScope = coroutineScope)
            }

        val scope = remember {
            object : TabIndicatorScope {
                val tabPositions = mutableStateOf<(List<TabPosition>)>(listOf())

                override fun Modifier.tabIndicatorLayout(
                    measure:
                    MeasureScope.(
                        Measurable,
                        Constraints,
                        List<TabPosition>,
                    ) -> MeasureResult
                ): Modifier =
                    this.layout { measurable: Measurable, constraints: Constraints ->
                        measure(
                            measurable,
                            constraints,
                            tabPositions.value,
                        )
                    }

                fun setTabPositions(positions: List<TabPosition>) {
                    tabPositions.value = positions
                }
            }
        }

        Layout(
            contents =
            listOf(
                tabs,
                divider,
                { scope.indicator() },
            )
        ) { (tabMeasurables, dividerMeasurables, indicatorMeasurables), constraints ->
            val padding = edgePadding.roundToPx()
            val tabCount = tabMeasurables.size
            val minTabWidth = ScrollableTabRowMinimumTabWidth.roundToPx()
            val layoutHeight =
                tabMeasurables.fastFold(initial = 0) { curr, measurable ->
                    maxOf(curr, measurable.maxIntrinsicHeight(Constraints.Infinity))
                }
            var layoutWidth = padding * 2
            val tabConstraints =
                constraints.copy(
                    minWidth = minTabWidth,
                    minHeight = layoutHeight,
                    maxHeight = layoutHeight,
                )

            var left = edgePadding
            val tabPlaceables = tabMeasurables.fastMap { it.measure(tabConstraints) }

            val positions =
                List(tabCount) { index ->
                    val tabWidth =
                        maxOf(ScrollableTabRowMinimumTabWidth, tabPlaceables[index].width.toDp())
                    layoutWidth += tabWidth.roundToPx()
                    // Enforce minimum touch target of 24.dp
                    val contentWidth = maxOf(tabWidth - (HorizontalTextPadding * 2), 24.dp)
                    val tabPosition =
                        TabPosition(left = left, width = tabWidth, contentWidth = contentWidth)
                    left += tabWidth
                    tabPosition
                }
            scope.setTabPositions(positions)

            val dividerPlaceables =
                dividerMeasurables.fastMap {
                    it.measure(
                        constraints.copy(
                            minHeight = 0,
                            minWidth = layoutWidth,
                            maxWidth = layoutWidth
                        )
                    )
                }

            val indicatorPlaceables =
                indicatorMeasurables.fastMap {
                    it.measure(
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = positions[selectedTabIndex].width.roundToPx(),
                            minHeight = 0,
                            maxHeight = layoutHeight
                        )
                    )
                }

            layout(layoutWidth, layoutHeight) {
                left = edgePadding
                tabPlaceables.fastForEachIndexed { index, placeable ->
                    placeable.placeRelative(left.roundToPx(), 0)
                    left += positions[index].width
                }

                dividerPlaceables.fastForEach { placeable ->
                    placeable.placeRelative(0, layoutHeight - placeable.height)
                }

                indicatorPlaceables.fastForEach {
                    val relativeOffset =
                        max(0, (positions[selectedTabIndex].width.roundToPx() - it.width) / 2)
                    it.placeRelative(relativeOffset, layoutHeight - it.height)
                }

                scrollableTabData.onLaidOut(
                    density = this@Layout,
                    edgeOffset = padding,
                    tabPositions = positions,
                    selectedTab = selectedTabIndex
                )
            }
        }
    }
}
