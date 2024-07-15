package com.sayzen.campfiresdk.compose.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity

// https://stackoverflow.com/a/71665355

class TableState internal constructor() {
    @Suppress("PropertyName")
    internal val _columnWidths = mutableStateMapOf<Int, Int>()
    val columnWidths = _columnWidths as Map<Int, Int>
}

@Composable
fun rememberTableState(): TableState {
    return remember { TableState() }
}

@Composable
fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    tableState: TableState = rememberTableState(),
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable BoxScope.(columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = tableState._columnWidths

    Box(modifier = modifier) {
        Column {
            HorizontalDivider()
            repeat(rowCount) { rowIndex ->
                Row(modifier = rowModifier) {
                    repeat(columnCount) { columnIndex ->
                        Box(modifier = Modifier.layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)

                            val existingWidth = columnWidths[columnIndex] ?: 0
                            val maxWidth = maxOf(existingWidth, placeable.width)

                            if (maxWidth > existingWidth) {
                                columnWidths[columnIndex] = maxWidth
                            }

                            layout(width = maxWidth, height = placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }) {
                            cellContent(columnIndex, rowIndex)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TableWithBorders(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable BoxScope.(columnIndex: Int, rowIndex: Int) -> Unit
) {
    val borderThickness = with(LocalDensity.current) { DividerDefaults.Thickness.toPx() }
    val borderColor = DividerDefaults.color

    val tableState = rememberTableState()

    // IntrinsicSize.Min results in fillMaxHeight() of the vertical dividers
    // matching the Box's height instead of its parent's
    Box(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(end = DividerDefaults.Thickness)
            .height(IntrinsicSize.Min)
    ) {
        Table(
            modifier = Modifier
                .drawWithCache {
                    onDrawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = borderThickness,
                        )
                        drawLine(
                            color = borderColor,
                            start = Offset.Zero,
                            end = Offset(0f, size.height),
                            strokeWidth = borderThickness,
                        )
                    }
                },
            rowModifier = rowModifier
                .drawWithCache {
                    onDrawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderThickness,
                        )
                    }
                },
            columnCount = columnCount,
            rowCount = rowCount,
            tableState = tableState,
            cellContent = { col, row ->
                Box(
                    Modifier
                        .padding(DividerDefaults.Thickness)
                ) {
                    cellContent(col, row)
                }
            },
        )

        repeat(columnCount) { col ->
            val x = with(LocalDensity.current) {
                var sum = 0
                tableState.columnWidths.iterator().forEach {
                    if (it.key <= col) sum += it.value
                }

                sum.toDp()
            }
            VerticalDivider(Modifier.offset(x = x))
        }
    }
}
