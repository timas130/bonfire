package com.sayzen.campfiresdk.compose.publication.post.pages

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.compose.publication.post.pages.activity.PageUserActivityRenderer
import com.sayzen.campfiresdk.compose.publication.post.pages.polling.PagePollingRenderer
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage

private fun spoilersNest(pages: List<Page>, start: Int = 0, size: Int = pages.size): Pair<List<Page>, Int> {
    val result = mutableListOf<Page>()
    var sizeMut = size
    var i = start

    while (i < (start + sizeMut).coerceAtMost(pages.size)) {
        val page = pages[i]
        page.index = i
        if (page is PageSpoiler) {
            val spoilerContent = spoilersNest(pages, i + 1, page.count)
            page.children = spoilerContent.first
            result.add(page)
            i += spoilerContent.second + 1
            sizeMut += spoilerContent.second
        } else {
            result.add(page)
            i++
        }
    }

    return Pair(result, i - start)
}

data class PagesSource(
    val sourceType: Long,
    val sourceId: Long,
    val sourceSubId: Long = 0,

    val editMode: Boolean = false,
    val movingIndex: Int? = null,
    val onMoveStarted: (idx: Int) -> Unit = {},
    val onMoveFinished: (idx: Int, dest: Int) -> Unit = { _, _ -> },
    //val onChangeClicked: (idx: Int) -> Unit = {},
    //val onRemoveClicked: (idx: Int) -> Unit = {},
) {
    companion object {
        val Unknown = PagesSource(sourceType = API.PAGES_SOURCE_TYPE_POST, sourceId = 0)
    }
}

@Composable
fun PostPage(page: Page, source: PagesSource = PagesSource.Unknown, onExpand: () -> Unit = {}) {
    when (page) {
        is PageText -> PageTextRenderer(page = page)
        is PageImage -> PageImageRenderer(page = page)
        is PageImages -> PageImagesRenderer(page = page)
        is PageLink -> PageLinkRenderer(page = page)
        is PageQuote -> PageQuoteRenderer(page = page)
        is PageSpoiler -> PageSpoilerRenderer(page = page, source = source, onExpand = onExpand)
        is PagePolling -> PagePollingRenderer(page = page, source = source)
        is PageVideo -> PageVideoRenderer(page = page)
        is PageTable -> PageTableRenderer(page = page)
        is PageDownload -> PageUnsupportedRenderer(page = page)
        is PageCampfireObject -> PageCampfireObjectRenderer(page = page)
        is PageUserActivity -> PageUserActivityRenderer(page = page)
        is PageLinkImage -> PageLinkImageRenderer(page = page)
        is PageCode -> PageCodeRenderer(page = page)
    }
}

@Composable
internal fun PageMoveDestination(idx: Int, source: PagesSource) {
    AnimatedVisibility(visible = source.movingIndex != null, Modifier.fillMaxWidth()) {
        Surface(Modifier.clickable {
            if (source.movingIndex == null) return@clickable
            source.onMoveFinished(source.movingIndex, idx)
        }) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.post_move_here),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun PostPages(
    pages: List<Page>,
    source: PagesSource = PagesSource.Unknown,
    onExpand: () -> Unit = {}
) {
    val nested = remember(pages) {
        spoilersNest(pages).first
    }

    nested.forEachIndexed { index, page ->
        PageMoveDestination(idx = index, source = source)
        PostPage(page = page, source = source, onExpand = onExpand)
    }
    PageMoveDestination(idx = nested.size, source = source)
}

class ComposeCardPage(pagesContainer: PagesContainer?, page: Page) : CardPage(R.layout.card_page_compose, pagesContainer, page) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vCompose: ComposeView = view.findViewById(R.id.vCompose)

        vCompose.setContent {
            BonfireTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PostPage(
                            page = page,
                            source = PagesSource.Unknown.copy(editMode = this@ComposeCardPage.editMode)
                        )
                    }
                }
            }
        }
    }

    override fun notifyItem() {
    }
}

class ComposePostPages(
    private val pages: List<Page>,
    private val pagesContainer: PagesContainer?,
) : ComposeCard() {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostPages(
                pages = pages,
                source = pagesContainer?.let {
                    PagesSource(
                        sourceType = it.getSourceType(),
                        sourceId = it.getSourceId(),
                        sourceSubId = it.getSourceIdSub()
                    )
                } ?: PagesSource.Unknown
            )
        }
    }

    @Composable
    override fun getBackground(): Color {
        return MaterialTheme.colorScheme.surfaceContainerLow
    }
}
