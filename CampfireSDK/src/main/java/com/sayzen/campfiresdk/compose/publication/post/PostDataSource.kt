package com.sayzen.campfiresdk.compose.publication.post

import androidx.compose.runtime.State
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.compose.fandom.FandomDataSource
import com.sayzen.campfiresdk.compose.publication.PublicationDataSource
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.*
import com.sup.dev.android.models.EventStyleChanged

class PostDataSource(post: PublicationPost, onRemoved: State<() -> Unit>) : PublicationDataSource<PublicationPost>(post, onRemoved) {
    init {
        subscriber
            .subscribe(EventPostChanged::class) {
                edit(it.publicationId) {
                    pages = it.pages
                }
            }
            .subscribe(EventCommentRemove::class) {
                edit(post.bestComment?.id == it.commentId) {
                    bestComment = null
                }
            }
            .subscribe(EventPostRubricChange::class) {
                edit(it.postId) {
                    rubricId = it.rubric.id
                    rubricName = it.rubric.name
                }
            }
            .subscribe(EventPostCloseChange::class) {
                edit(it.publicationId) {
                    closed = it.closed
                }
            }
            .subscribe(EventPostNotifyFollowers::class) {
                edit(it.publicationId) {
                    tag_3 = 1
                }
            }
            .subscribe(EventPostMultilingualChange::class) {
                edit(it.publicationId) {
                    fandom.languageId = it.languageId
                    tag_5 = it.tag5
                }
            }
            .subscribe(EventPublicationImportantChange::class) {
                edit(it.publicationId) {
                    important = it.important
                }
            }
            .subscribe(EventPostSetNsfw::class) {
                edit(it.publicationId) {
                    nsfw = it.nsfw
                }
            }
            .subscribe(EventStyleChanged::class) {
                remove(!ControllerSettings.showNsfwPosts && data.nsfw)
            }
    }

    private val fandomDataSource = object : FandomDataSource(post.fandom) {
        override fun edit(cond: Boolean, editor: Fandom.() -> Unit) {
            this@PostDataSource.edit(cond) {
                this.fandom.editor()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        fandomDataSource.destroy()
    }

    override fun handleNotification(ev: EventNotification) {
        when (ev.notification) {
            is NotificationComment -> {
                edit(ev.notification.publicationId) {
                    subPublicationsCount++
                }
            }
            is NotificationCommentAnswer -> {
                edit(ev.notification.publicationId) {
                    subPublicationsCount++
                }
            }
        }
    }
}
