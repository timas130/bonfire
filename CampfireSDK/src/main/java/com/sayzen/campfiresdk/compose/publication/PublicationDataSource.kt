package com.sayzen.campfiresdk.compose.publication

import android.util.Log
import androidx.compose.runtime.State
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.compose.data.AccountDataSource
import com.sayzen.campfiresdk.compose.fandom.FandomDataSource
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.*

abstract class PublicationDataSource<T : Publication>(pub: T, private val onRemoved: State<() -> Unit>) : BonfireDataSource<T>(pub) {
    init {
        subscriber
            .subscribe(EventAccountRemoveFromBlackList::class) {
                edit(pub.creator.id == it.accountId) {
                    blacklisted = false
                }
            }
            .subscribe(EventAccountAddToBlackList::class) {
                edit(pub.creator.id == it.accountId) {
                    blacklisted = true
                }
            }
            .subscribe(EventPublicationKarmaAdd::class) {
                edit(it.publicationId) {
                    myKarma = it.myKarma
                    karmaCount += it.myKarma
                }
            }
            .subscribe(EventPublicationFandomChanged::class) {
                edit(it.publicationId) {
                    fandom.id = it.fandomId
                    fandom.image = it.fandomImage
                    fandom.languageId = it.languageId
                    fandom.name = it.fandomName
                }
            }
            .subscribe(EventPublicationReactionAdd::class) {
                edit(it.publicationId) {
                    reactions += Publication.Reaction(ControllerApi.account.getId(), it.reactionIndex)
                }
            }
            .subscribe(EventPublicationReactionRemove::class) { ev ->
                edit(ev.publicationId) {
                    reactions = reactions
                        .filterNot {
                            ControllerApi.isCurrentAccount(it.accountId) && it.reactionIndex == ev.reactionIndex
                        }
                        .toTypedArray()
                }
            }
            .subscribe(EventPublicationRemove::class) {
                remove(it.publicationId)
            }
            .subscribe(EventPublicationBlocked::class) {
                remove(it.publicationId)
            }
            .subscribe(EventCommentsCountChanged::class) {
                edit(it.publicationId) {
                    subPublicationsCount = it.commentsCount
                }
            }
            .subscribe(EventReportsCountChanged::class) {
                edit(it.publicationId) {
                    reportsCount = it.reportsCount
                }
            }
    }

    override fun handleNotification(ev: EventNotification) {
        super.handleNotification(ev)

        if (ev.notification is NotificationComment) {
            edit(ev.notification.publicationId) {
                subPublicationsCount++
            }
        } else if (ev.notification is NotificationCommentAnswer) {
            edit(ev.notification.publicationId) {
                subPublicationsCount++
            }
        }
    }

    private val fandomDataSource = object : FandomDataSource(pub.fandom) {
        override fun edit(cond: Boolean, editor: Fandom.() -> Unit) {
            this@PublicationDataSource.edit(cond) {
                this.fandom.editor()
            }
        }
    }

    private val accountDataSource = object : AccountDataSource(pub.creator) {
        override fun edit(cond: Boolean, editor: Account.() -> Unit) {
            this@PublicationDataSource.edit(cond) {
                this.creator.editor()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        fandomDataSource.destroy()
        accountDataSource.destroy()
    }

    fun edit(id: Long, editor: T.() -> Unit) {
        edit(id == data.id, editor)
    }

    override fun edit(cond: Boolean, editor: T.() -> Unit) {
        if (cond) {
            Log.d("PublicationDataSource", "modifying id=${data.id}")
        }
        super.edit(cond, editor)
    }

    fun remove(id: Long) {
        remove(id == data.id)
    }
    fun remove(cond: Boolean) {
        if (cond) {
            onRemoved.value()
        }
    }
}
