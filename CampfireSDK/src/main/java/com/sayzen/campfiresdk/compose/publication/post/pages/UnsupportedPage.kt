package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageDownload
import com.sayzen.campfiresdk.R

@Composable
internal fun PageUnsupportedRenderer(page: Page) {
    val message = when (page) {
        is PageDownload -> R.string.page_download_unsupported
        else -> R.string.page_unsupported
    }

    Text(
        text = stringResource(message),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
