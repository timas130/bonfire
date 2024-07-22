package com.sayzen.campfiresdk.compose.publication

import android.util.Log
import androidx.compose.runtime.State
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlocked
import com.sayzen.campfiresdk.models.events.publications.EventPublicationFandomChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove

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
            .subscribe(EventPublicationRemove::class) {
                remove(it.publicationId)
            }
            .subscribe(EventPublicationBlocked::class) {
                remove(it.publicationId)
            }
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
