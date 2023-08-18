package com.sayzen.campfiresdk.support.adapters

import android.widget.TextView
import com.dzen.campfire.api.models.publications.EventPublicationInstance
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentAdd
import com.sayzen.campfiresdk.models.events.publications.EventCommentRemove
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.eventBus.EventBus

class XComments(
        private val publication: Publication,
        var onChanged: () -> Unit
) {

    companion object {
        private val eventBus = EventBus
                .subscribe(EventPublicationInstance::class) { set(it.publication.id, it.publication.subPublicationsCount, System.currentTimeMillis()) }
                .subscribe(EventCommentAdd::class) { set(it.parentPublicationId, get(it.parentPublicationId) + 1, System.currentTimeMillis()) }
                .subscribe(EventCommentRemove::class) { set(it.parentPublicationId, get(it.parentPublicationId) - 1, System.currentTimeMillis()) }
                .subscribe(EventNotification::class) {
                    if (it.notification is NotificationComment) set(it.notification.publicationId, get(it.notification.publicationId) + 1, System.currentTimeMillis())
                    if (it.notification is NotificationCommentAnswer) set(it.notification.publicationId, get(it.notification.publicationId) + 1, System.currentTimeMillis())
                }

        private val comments = HashMap<Long, Item2<Long, Long>>()

        fun set(publicationId: Long, commentsCount: Long, publicationInstanceDate: Long) {
            if (!comments.containsKey(publicationId)) {
                comments[publicationId] = Item2(commentsCount, publicationInstanceDate)
                EventBus.post(EventCommentsCountChanged(publicationId, commentsCount, commentsCount))
            } else {
                if (comments[publicationId]!!.a2 < publicationInstanceDate && comments[publicationId]!!.a1 != commentsCount) {
                    val old = comments[publicationId]!!.a1
                    comments[publicationId] = Item2(commentsCount, publicationInstanceDate)
                    EventBus.post(EventCommentsCountChanged(publicationId, commentsCount, commentsCount - old))
                }
            }
        }

        fun get(publicationId: Long) = if (comments.containsKey(publicationId)) comments[publicationId]!!.a1 else 0L

    }

    private val eventBus = EventBus
            .subscribe(EventCommentsCountChanged::class) {
                if (it.publicationId == publication.id) {
                    publication.subPublicationsCount = get(publication.id)
                    onChanged.invoke()
                }
            }

    fun setView(view: TextView) {
        view.text = "${publication.subPublicationsCount}"
    }


}
