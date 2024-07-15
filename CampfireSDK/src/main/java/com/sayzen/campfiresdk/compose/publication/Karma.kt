package com.sayzen.campfiresdk.compose.publication

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsKarmaAdd
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.compose.publication.post.counterTransitionSpec
import com.sayzen.campfiresdk.compose.util.KonfettiViewExt
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaStateChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt
import com.sup.dev.java.libs.eventBus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.listeners.OnParticleSystemUpdateListener
import java.util.concurrent.TimeUnit

class KarmaCounterModel(
    val publication: Publication,
    val onFinish: (up: Boolean) -> Unit,
) {
    data class KarmaProgress(val up: Boolean)

    private val _progress = MutableStateFlow<KarmaProgress?>(null)
    val progress = _progress.asStateFlow()

    private var progressJob: Job? = null

    fun rate(
        scope: CoroutineScope,
        up: Boolean,
        anon: Boolean = ControllerSettings.anonRates
    ) {
        progressJob?.cancel()
        if (_progress.value?.up == up) {
            progressJob?.cancel()
            progressJob = null
            _progress.tryEmit(null)
            return
        }
        _progress.tryEmit(KarmaProgress(up))

        progressJob = scope.launch {
            delay(CampfireConstants.RATE_TIME)

            try {
                val resp = RPublicationsKarmaAdd(publication.id, up, ControllerApi.getLanguageId(), anon)
                    .sendSuspendExt()

                PostHog.capture(
                    event = "karma_rate",
                    properties = mapOf(
                        "up" to up,
                        "anon" to anon,
                        "amount" to resp.myKarmaCount,
                    )
                )

                // launch it in outer scope, so job cancellation cannot affect us
                scope.launch {
                    progressJob = null
                    _progress.emit(null)
                    onFinish(up)

                    EventBus.post(EventPublicationKarmaAdd(publication.id, resp.myKarmaCount))
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_KARMA)
                    EventBus.post(EventPublicationKarmaStateChanged(publication.id))
                }
            } catch (_: Exception) {
                progressJob = null
                _progress.emit(null)
            }
        }
    }
}

@Composable
internal fun KarmaCounter(publication: Publication) {
    val colors = ButtonDefaults.filledTonalButtonColors()

    @Composable
    fun Divider() {
        VerticalDivider(
            modifier = Modifier.height(16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }

    var showConfetti by remember { mutableStateOf(false) }
    var counterCenter by remember { mutableStateOf<Offset?>(null) }

    if (showConfetti && counterCenter != null) {
        val party = remember {
            Party(
                angle = 270,
                spread = 90,
                position = Position.Absolute(counterCenter!!.x, counterCenter!!.y),
                emitter = Emitter(500, TimeUnit.MILLISECONDS).max(50)
            )
        }
        KonfettiViewExt(
            party = party,
            updateListener = object : OnParticleSystemUpdateListener {
                override fun onParticleSystemEnded(view: KonfettiView, party: Party, activeSystems: Int) {
                    showConfetti = false
                }
                override fun onParticleSystemStarted(view: KonfettiView, party: Party, activeSystems: Int) {
                }
            }
        )
    }

    val model = remember(publication) {
        KarmaCounterModel(
            publication = publication,
            onFinish = {
                if (Math.random() > 0.95) {
                    PostHog.capture("lucky_karma_confetti")
                    showConfetti = true
                }
            }
        )
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.containerColor,
            contentColor = colors.contentColor,
            modifier = Modifier
                .height(36.dp)
                .onGloballyPositioned {
                    counterCenter = it.localToWindow(Offset(it.size.width / 2f, it.size.height / 2f))
                },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KarmaCounterSide(up = false, model = model)
                Divider()
                KarmaCounterAmount(publication = publication)
                Divider()
                KarmaCounterSide(up = true, model = model)
            }
        }
    }
}

@Composable
internal fun KarmaCounterSide(up: Boolean, model: KarmaCounterModel) {
    val colors = ButtonDefaults.filledTonalButtonColors()
    val scope = rememberCoroutineScope()

    val accentColor = if (up) {
        Color(0xFF388E3C)
    } else {
        Color(0xFFE53935)
    }
    val dividerThickness = with(LocalDensity.current) { DividerDefaults.Thickness.toPx() }

    val karmaProgress by model.progress.collectAsState()
    val targetValue = if (karmaProgress?.up == up) {
        1f
    } else {
        0f
    }
    val fillProgress = animateFloatAsState(
        targetValue = targetValue,
        animationSpec = if (targetValue == 1f) {
            tween(durationMillis = CampfireConstants.RATE_TIME.toInt())
        } else {
            tween(durationMillis = 300)
        },
        label = "${if (up) "Up" else "Down"}Fill"
    ).value

    Surface(
        shape = if (up) {
            RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
        },
        color = Color.Transparent,
        contentColor = colors.contentColor,
        modifier = Modifier
            .zIndex(1f)
            .drawWithContent {
                drawRect(colors.containerColor)
                // the shape is clipped anyway, so there's almost no point
                // in separating rendering for up/down
                drawRect(
                    topLeft = Offset(-dividerThickness, 0f),
                    color = accentColor,
                    size = Size(fillProgress * (size.width + dividerThickness * 2), size.height)
                )
                drawContent()
            },
        enabled = model.publication.myKarma == 0L,
        onClick = {
            model.rate(scope, up)
        },
    ) {
        val isCurrentActive = if (up) model.publication.myKarma > 0 else model.publication.myKarma < 0
        val isOtherActive = !isCurrentActive && model.publication.myKarma != 0L

        AnimatedContent(
            targetState = isCurrentActive,
            label = "${if (up) "Up" else "Down"}Done",
            transitionSpec = {
                val specOffset = tween<IntOffset>(durationMillis = 1000)
                val specFloat = tween<Float>(durationMillis = 1000)
                if (up) {
                    slideInVertically(specOffset) { it } + fadeIn(specFloat) togetherWith
                            slideOutVertically(specOffset) { -it } + fadeOut(specFloat)
                } else {
                    slideInVertically(specOffset) { -it } + fadeIn(specFloat) togetherWith
                            slideOutVertically(specOffset) { it } + fadeOut(specFloat)
                }
            }
        ) { disabled ->
            Icon(
                if (up) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                if (up) {
                    stringResource(R.string.karma_up)
                } else {
                    stringResource(R.string.karma_down)
                },
                Modifier
                    .padding(
                        start = if (up) 6.dp else 10.dp,
                        end = if (up) 10.dp else 6.dp,
                        top = 6.dp,
                        bottom = 6.dp
                    )
                    .size(24.dp)
                    .alpha(if (disabled || isOtherActive) 0.6f else 1f)
            )
        }
    }
}

@Composable
internal fun KarmaCounterAmount(publication: Publication) {
    Column(
        Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .widthIn(min = 28.dp),
        verticalArrangement = Arrangement.spacedBy((-6).dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = publication.karmaCount,
            transitionSpec = counterTransitionSpec,
            label = "KarmaAmount",
        ) {
            val color = if (it < 0) {
                MaterialTheme.colorScheme.error
            } else {
                Color.Unspecified
            }
            Text(
                text = (it / 100).toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                textAlign = TextAlign.Center,
            )
        }

        if (publication.fandom.karmaCof != 100L) {
            Text(
                text = "x${"%.2f".format(publication.fandom.karmaCof / 100f)}",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}
