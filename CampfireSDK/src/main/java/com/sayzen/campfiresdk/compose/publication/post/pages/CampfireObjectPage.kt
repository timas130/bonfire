package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.LinkParsed
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PageCampfireObject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireObjects
import com.sayzen.campfiresdk.controllers.ControllerSettings
import sh.sit.bonfire.images.RemoteImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private data class CampfireObjectData(
    val title: String,
    val subtitle: String,
    val image: ImageRef,
)

private suspend fun ControllerCampfireObjects.load(linkParsed: LinkParsed): CampfireObjectData {
    return suspendCoroutine {
        // ControllerCampfireObjects.load is guaranteed to call the callback.
        // in case of an error, placeholder data is returned
        load(linkParsed) { title, subtitle, image ->
            it.resume(CampfireObjectData(title, subtitle, image))
        }
    }
}

@Composable
internal fun PageCampfireObjectRenderer(page: PageCampfireObject) {
    var objectData by remember { mutableStateOf<CampfireObjectData?>(null) }

    LaunchedEffect(page.link) {
        objectData = ControllerCampfireObjects.load(LinkParsed(page.link))
    }

    val data = objectData
    PageLinkLayout(
        icon = {
            if (data != null) {
                RemoteImage(
                    link = data.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(ControllerSettings.styleAvatarsRounding.dp)),
                )
            } else {
                CircularProgressIndicator(Modifier.size(32.dp))
            }
        },
        title = data?.title ?: stringResource(R.string.page_campfire_object),
        subtitle = data?.subtitle ?: LinkParsed(page.link).link,
        link = page.link,
    )
}
