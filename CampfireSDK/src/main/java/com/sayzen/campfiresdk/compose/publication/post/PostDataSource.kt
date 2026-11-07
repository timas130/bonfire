package com.sayzen.campfiresdk.compose.publication.post

import androidx.compose.runtime.State
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.post.PageUserActivity
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.compose.publication.PublicationDataSource
import com.sayzen.campfiresdk.compose.publication.post.pages.activity.UserActivityDataSource
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.publications.*
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.java.libs.eventBus.EventBus

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
            .subscribe(EventPostStatusChange::class) {
                remove(
                    it.publicationId == data.id && it.status != data.status &&
                        (data.status == API.STATUS_DRAFT || data.status == API.STATUS_PUBLIC || data.status == API.STATUS_PENDING)
                )
            }
            .subscribe(EventPublicationDeepBlockRestore::class) {
                remove(it.publicationId == data.id && data.status == API.STATUS_DEEP_BLOCKED)
            }
    }

    private val userActivityDataSource = post.userActivity?.let { userActivity ->
        object : UserActivityDataSource(userActivity) {
            override fun edit(cond: Boolean, editor: UserActivity.() -> Unit) {
                this@PostDataSource.edit(cond) {
                    this.userActivity?.editor()
                    (this.pages.find { it is PageUserActivity } as PageUserActivity?)
                        ?.userActivity?.editor()
                }
            }
        }
    }

    private val bestCommentSubscriber = EventBus
        .subscribe(EventPublicationRemove::class) {
            edit(data.bestComment?.id == it.publicationId) {
                bestComment = null
            }
        }

    override fun destroy() {
        super.destroy()
        userActivityDataSource?.destroy()
        bestCommentSubscriber.unsubscribe()
    }
}
