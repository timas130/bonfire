package com.sayzen.campfiresdk.compose.publication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsReactionAdd
import com.dzen.campfire.api.requests.publications.RPublicationsReactionRemove
import com.sayzen.campfiresdk.compose.publication.comment.nestedClickableRoot
import com.sayzen.campfiresdk.compose.publication.post.counterTransitionSpec
import com.sayzen.campfiresdk.compose.util.relativeToView
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerReactions
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.java.libs.eventBus.EventBus
import sh.sit.bonfire.images.RemoteImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicationReactions(
    publication: Publication,
    modifier: Modifier = Modifier,
    // does not change behavior
    onLongClick: () -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val data = remember(publication.reactions) { ReactionData.fromArray(publication.reactions) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        for (reactionIndex in ApiResources.REACTIONS.indices.map { it.toLong() }) {
            val reactionCount = data.counts[reactionIndex] ?: 0
            val image = ApiResources.REACTIONS.getOrNull(reactionIndex.toInt()) ?: ApiResources.EMOJI_5

            val windowOffset = remember { mutableStateOf<Offset?>(null) }

            AnimatedVisibility(reactionCount > 0) {
                Reaction(
                    selected = data.myReacts.contains(reactionIndex),
                    onValueChange = { add ->
                        val request = if (add) {
                            RPublicationsReactionAdd(publication.id, reactionIndex)
                        } else {
                            RPublicationsReactionRemove(publication.id, reactionIndex)
                        }

                        ApiRequestsSupporter.execute(request) {
                            if (add) {
                                EventBus.post(EventPublicationReactionAdd(publication.id, reactionIndex))
                            } else {
                                EventBus.post(EventPublicationReactionRemove(publication.id, reactionIndex))
                            }
                        }
                    },
                    onLongClick = { offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()

                        val relativeOffset = (windowOffset.value!! + offset).relativeToView(view)
                        ControllerReactions.showAccounts(
                            publication.id,
                            reactionIndex,
                            view,
                            relativeOffset.x.toInt(),
                            relativeOffset.y.toInt(),
                        )
                    },
                    leadingIcon = {
                        RemoteImage(
                            link = image,
                            contentDescription = reactionEmoji[reactionIndex.toInt()],
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = {
                        AnimatedContent(
                            // conversion to long is required by counterTransitionSpec
                            targetState = reactionCount.toLong(),
                            transitionSpec = counterTransitionSpec,
                            label = "ReactionCounter",
                        ) { count ->
                            Text(count.toString())
                        }
                    },
                    modifier = Modifier
                        .onGloballyPositioned {
                            windowOffset.value = it.localToWindow(Offset.Zero)
                        }
                )
            }
        }
    }
}

@Composable
fun Reaction(
    selected: Boolean,
    onValueChange: (Boolean) -> Unit,
    onLongClick: (Offset) -> Unit,
    leadingIcon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            color = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
            shape = FilterChipDefaults.shape,
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected),
            modifier = modifier
                .padding(vertical = 8.dp)
                .semantics { role = Role.Checkbox }
                .clip(FilterChipDefaults.shape)
                .toggleable(
                    value = selected,
                    onValueChange = onValueChange
                )
                .nestedClickableRoot(
                    onClick = { onValueChange(!selected) },
                    onLongClick = onLongClick,
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
                CompositionLocalProvider(
                    value = LocalTextStyle provides MaterialTheme.typography.labelLarge,
                    content = label
                )
            }
        }
    }
}

private val reactionEmoji = listOf("🙁", "🙂", "😂", "😍", "🤔", "🤮", "🙄")

private data class ReactionData(
    val counts: Map<Long, Int>,
    val myReacts: List<Long>
) {
    companion object {
        fun fromArray(reactions: Array<Publication.Reaction>): ReactionData {
            val counts = mutableMapOf<Long, Int>()
            val myReacts = mutableListOf<Long>()

            for (reaction in reactions) {
                if (ControllerApi.isCurrentAccount(reaction.accountId)) {
                    myReacts.add(reaction.reactionIndex)
                }

                if (counts.containsKey(reaction.reactionIndex)) {
                    counts[reaction.reactionIndex] = counts[reaction.reactionIndex]!! + 1
                } else {
                    counts[reaction.reactionIndex] = 1
                }
            }

            return ReactionData(
                counts,
                myReacts
            )
        }
    }
}
