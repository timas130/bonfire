package com.sayzen.campfiresdk.compose.fandom

import com.dzen.campfire.api.models.fandoms.Fandom
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomClose

open class FandomDataSource(data: Fandom) : BonfireDataSource<Fandom>(data) {
    init {
        subscriber
            .subscribe(EventFandomChanged::class) {
                edit(it.fandomId) {
                    if (it.name.isNotEmpty()) name = it.name
                    if (it.image.isNotEmpty()) image = it.image
                    if (it.imageTitle.isNotEmpty()) imageTitle = it.imageTitle
                    if (it.imageTitleGif.isNotEmpty()) imageTitleGif = it.imageTitleGif
                }
            }
            .subscribe(EventFandomClose::class) {
                edit(it.fandomId) {
                    closed = it.closed
                }
            }
    }

    fun edit(id: Long, editor: Fandom.() -> Unit) {
        edit(data.id == id, editor)
    }
}
