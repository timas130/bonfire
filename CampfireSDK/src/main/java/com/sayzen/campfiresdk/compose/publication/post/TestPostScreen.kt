package com.sayzen.campfiresdk.compose.publication.post

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostFeedGetAllSubscribe
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.sendSuspend
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sup.dev.java.libs.eventBus.EventBus
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.components.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPostScreenC() {
    var posts by remember { mutableStateOf<List<PublicationPost>>(emptyList()) }

    LaunchedEffect(Unit) {
        val resp = RPostFeedGetAllSubscribe(0)
            .sendSuspend(api)
        posts = resp.publications.filterIsInstance<PublicationPost>()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(stringResource(R.string.post_comment)) }
            )
        },
    ) { paddingValues ->
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LazyColumn(contentPadding = paddingValues, state = listState) {
            item {
                Button(onClick = { EventBus.post(EventPublicationKarmaAdd(-1, 100)) }) {
                    Text("Karma")
                }
            }
            item {
                Post(initialPost = testPost)
            }
            itemsIndexed(posts) { index, post ->
                Post(
                    initialPost = post,
                    scrollToTop = {
                        scope.launch {
                            val layoutInfo = listState.layoutInfo
                            val topVisible = layoutInfo.visibleItemsInfo
                                .find { it.index == index + 2 }
                                ?.let { it.offset > 0 } == true

                            if (!topVisible) {
                                listState.animateScrollToItem(index + 2)
                            }
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("ViewConstructor")
class TestPostScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        TestPostScreenC()
    }
}
