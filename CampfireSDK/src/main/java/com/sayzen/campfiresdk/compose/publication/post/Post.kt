package com.sayzen.campfiresdk.compose.publication.post

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.compose.ComposeCard
import com.sayzen.campfiresdk.compose.util.mapState
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.cards.CardComment
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.screens.navigator.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.formatting.compose.LinksClickableText
import sh.sit.bonfire.formatting.compose.buildInlineAnnotatedString
import sh.sit.bonfire.formatting.core.BonfireFormatter

@Composable
internal fun PostTitle(title: String) {
    if (title.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp, top = 4.dp),
        )
    }
}

@Composable
internal fun PostChips(post: PublicationPost) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (post.important == API.PUBLICATION_IMPORTANT_IMPORTANT) {
            item(key = "important") {
                SuggestionChip(
                    onClick = {},
                    icon = {
                        Icon(painterResource(R.drawable.priority_high_24px), contentDescription = null)
                    },
                    label = {
                        Text(stringResource(R.string.post_important))
                    }
                )
            }
        }

        val activity = post.userActivity
        if (activity != null) {
            item(key = "activity") {
                SuggestionChip(
                    onClick = {
                        SRelayRaceInfo.instance(activity.id, Navigator.TO)
                    },
                    icon = {
                        Icon(painterResource(R.drawable.rowing_24), stringResource(R.string.post_activity))
                    },
                    label = {
                        Text(activity.name)
                    }
                )
            }
        }
    }
}

class PostModel(post: PublicationPost, onRemoved: State<() -> Unit>) : ViewModel() {
    val dataSource = PostDataSource(post, onRemoved)

    private val _expanded = MutableStateFlow(false)
    val expanded = _expanded.asStateFlow()

    private val _nsfwOverlayActive = MutableStateFlow(post.nsfw)
    val nsfwOverlayActive = _nsfwOverlayActive.asStateFlow()

    enum class NsfwOverlayButtonVariant {
        None,
        SpecifyAge,
        Open
    }
    val nsfwOverlayButtons = AuthController.currentUserState.mapState { user ->
        if (user?.birthday == null) {
            NsfwOverlayButtonVariant.SpecifyAge
        } else if (user.nsfwAllowed == true) {
            NsfwOverlayButtonVariant.Open
        } else {
            NsfwOverlayButtonVariant.None
        }
    }

    init {
        viewModelScope.launch {
            dataSource.flow
                .map { it.nsfw }
                .collect { _nsfwOverlayActive.value = it }
        }
    }

    fun expand() {
        _expanded.value = true
    }
    fun shrink() {
        _expanded.value = false
    }

    override fun onCleared() {
        super.onCleared()
        dataSource.destroy()
    }

    fun dismissNsfw() {
        _nsfwOverlayActive.value = false
    }
}

@Composable
fun Post(
    initialPost: PublicationPost,
    onRemoved: () -> Unit = {},
    scrollToTop: (() -> Unit)? = null,
    onClick: ((PublicationPost) -> Unit)? = null,
    showBestComment: Boolean = true,
) {
    val onRemovedRef = rememberUpdatedState(onRemoved)
    val model = viewModel(key = "Post:${initialPost.id}") {
        PostModel(initialPost, onRemovedRef)
    }

    val post by model.dataSource.flow.collectAsState()
    val expanded by model.expanded.collectAsState()

    var expandable by remember {
        mutableStateOf(false)
    }

    if (post.blacklisted) {
        if (ControllerSettings.hideBlacklistedPubs) return

        val hiddenString = stringResource(R.string.pub_hidden)
        val colors = MaterialTheme.colorScheme

        Surface(
            Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            LinksClickableText(
                text = remember(post.creator.name) {
                    BonfireFormatter.parse(
                        hiddenString.format(post.creator.name),
                        inlineOnly = true
                    ).buildInlineAnnotatedString(colors)
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            )
        }
        return
    }

    DisposableEffect(expanded) {
        if (!expanded) return@DisposableEffect onDispose {}

        val listener = {
            model.shrink()
            true
        }
        Navigator.addOnBack(listener)

        onDispose {
            Navigator.removeOnBack(listener)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                awaitEachGesture {
                    // wait for and consume press start
                    val down = awaitFirstDown()
                    down.consume()

                    // if canceled within 300ms, return
                    // if not canceled within 300ms, show indication
                    val maybeUp = try {
                        withTimeout(300) {
                            waitForUpOrCancellation()
                        } ?: return@awaitEachGesture
                    } catch (_: PointerEventTimeoutCancellationException) {
                        null
                    }

                    val press = PressInteraction.Press(down.position)
                    interactionSource.tryEmit(press)

                    // wait for up, if not already fired
                    val up = maybeUp ?: waitForUpOrCancellation()
                    if (up == null) {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                        return@awaitEachGesture
                    }
                    up.consume()

                    interactionSource.tryEmit(PressInteraction.Release(press))

                    // do action
                    if (onClick != null) {
                        onClick(post)
                    } else if (post.status != API.STATUS_DRAFT) {
                        PostHog.capture("post_open", properties = mapOf("type" to "full"))
                        SPost.instance(post.id, Navigator.TO, skipNsfw = !model.nsfwOverlayActive.value)
                    } else {
                        SPostCreate.instance(post.id, Navigator.TO)
                    }
                }
            }
    ) {
        PostHeader(post = post)

        PostTitle(title = post.title)

        PostChips(post = post)

        PostContent(
            post = post,
            model = model,
            onExpandableChanged = { expandable = it },
            onExpand = model::expand,
        )

        PostFooter(
            post = post,
            model = model,
            expandable = expandable,
            onExpand = { expand ->
                if (!expand) {
                    scrollToTop?.invoke()
                    model.shrink()
                } else {
                    model.expand()
                }
            }
        )

        AnimatedVisibility(visible = post.bestComment != null && showBestComment) {
            val card = remember(post.bestComment) {
                CardComment.instance(
                    publication = post.bestComment!!,
                    dividers = false,
                    miniSize = true,
                    onClick = {
                        SPost.instance(post.id, it.id, Navigator.TO)
                        true
                    },
                    onGoTo = {
                        SPost.instance(post.id, it, Navigator.TO)
                    }
                ).apply {
                    maxTextSize = 500
                }
            }

            HorizontalDivider()
            AndroidView(
                factory = {
                    val view = card.instanceView(it)
                    card.bindCardView(view)
                    view
                },
                update = {
                    card.bindCardView(it)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

internal val testPost = PublicationPost().apply {
    id = -1
    title = "Это новый пост."
    pages = arrayOf()
    creator = Account().apply {
        id = 1
        name = "sit"
        lvl = 100
    }
    karmaCount = -4200
    myKarma = 0
    subPublicationsCount = 15
    fandom = Fandom().apply {
        name = "Minecraft"
        image = ApiResources.AVATAR_2
        karmaCof = 150
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestPost() {
    BonfireTheme(useDarkTheme = isSystemInDarkTheme()) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Post(initialPost = testPost, onClick = {})
        }
    }
}

class ComposeCardPost(
    private val proxy: CardPostProxy,
    private val vRecycler: RecyclerView?,
    private val post: PublicationPost,
    private val onClick: ((PublicationPost) -> Unit)? = null,
) : ComposeCard() {
    var showFandom: Boolean
        set(_) {}
        get() = false

    @Composable
    override fun Content() {
        Post(
            initialPost = post,
            onRemoved = { adapter.remove(proxy) },
            scrollToTop = {
                val index = adapter.indexOf(proxy)
                if (index == -1) return@Post

                vRecycler?.scrollToPosition(index)
            },
            onClick = onClick,
        )
    }

    @Composable
    override fun getBackground(): Color {
        return Color.Transparent
    }
}
