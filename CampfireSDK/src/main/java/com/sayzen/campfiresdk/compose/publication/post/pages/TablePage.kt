package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageTable
import com.sayzen.campfiresdk.compose.util.TableWithBorders
import sh.sit.bonfire.formatting.compose.buildInlineAnnotatedString
import sh.sit.bonfire.formatting.core.BonfireFormatter
import sh.sit.bonfire.images.RemoteImage

@Composable
internal fun PageTableRenderer(page: PageTable) {
    val colors = MaterialTheme.colorScheme

    TableWithBorders(
        modifier = Modifier.padding(horizontal = 12.dp),
        columnCount = page.columnsCount,
        rowCount = page.rowsCount,
    ) { col, row ->
        val cell = page.cells.find { it.columnIndex == col && it.rowIndex == row }
            ?: PageTable.Cell.Empty

        if (cell.type == PageTable.CELL_TYPE_TEXT) {
            val formattedText = remember(cell.text) {
                BonfireFormatter
                    .parse(cell.text, inlineOnly = true)
                    .buildInlineAnnotatedString(colors)
            }

            Text(
                text = formattedText,
                modifier = Modifier.padding(4.dp),
            )
        } else if (cell.type == PageTable.CELL_TYPE_IMAGE) {
            RemoteImage(
                link = cell.image,
                contentDescription = "",
                modifier = Modifier
                    .padding(4.dp)
                    .widthIn(max = 256.dp)
                    .heightIn(max = 256.dp)
            )
        }
    }
}
