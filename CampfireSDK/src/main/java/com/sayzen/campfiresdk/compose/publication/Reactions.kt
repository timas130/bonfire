package com.sayzen.campfiresdk.compose.publication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsReactionAdd
import com.dzen.campfire.api.requests.publications.RPublicationsReactionRemove
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.java.libs.eventBus.EventBus
import sh.sit.bonfire.images.RemoteImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicationReactions(
    publication: Publication,
    modifier: Modifier = Modifier
) {
    val data = remember(publication.reactions) { ReactionData.fromArray(publication.reactions) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        for (reactionIndex in API.REACTIONS.indices.map { it.toLong() }) {
            val reactionCount = data.counts[reactionIndex] ?: 0
            val image = API.REACTIONS.getOrNull(reactionIndex.toInt()) ?: ApiResources.EMOJI_5

            AnimatedVisibility(reactionCount > 0) {
                FilterChip(
                    selected = data.myReacts.contains(reactionIndex),
                    onClick = {
                        val remove = data.myReacts.contains(reactionIndex)
                        val request = if (remove) {
                            RPublicationsReactionRemove(publication.id, reactionIndex)
                        } else {
                            RPublicationsReactionAdd(publication.id, reactionIndex)
                        }

                        ApiRequestsSupporter.execute(request) {
                            if (remove) {
                                EventBus.post(EventPublicationReactionRemove(publication.id, reactionIndex))
                            } else {
                                EventBus.post(EventPublicationReactionAdd(publication.id, reactionIndex))
                            }
                        }
                    },
                    leadingIcon = {
                        RemoteImage(
                            link = image,
                            contentDescription = reactionEmoji[reactionIndex.toInt()],
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = {
                        Text(reactionCount.toString())
                    }
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
