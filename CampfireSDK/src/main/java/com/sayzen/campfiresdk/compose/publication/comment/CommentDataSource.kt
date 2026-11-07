package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.runtime.State
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.compose.publication.PublicationDataSource
import com.sayzen.campfiresdk.models.events.publications.EventCommentChange
import com.sayzen.campfiresdk.models.events.publications.EventCommentRemove
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore

class CommentDataSource(
    data: PublicationComment,
    onRemoved: State<() -> Unit>
) : PublicationDataSource<PublicationComment>(data, onRemoved) {
    init {
        subscriber
            .subscribe(EventCommentRemove::class) {
                remove(it.commentId)
            }
            .subscribe(EventCommentChange::class) {
                edit(it.publicationId) {
                    text = it.text
                    quoteId = it.quoteId
                    quoteText = it.quoteText
                    changed = true
                }
            }
            .subscribe(EventPublicationDeepBlockRestore::class) {
                remove(it.publicationId == data.id && data.status == API.STATUS_DEEP_BLOCKED)
            }
    }
}
