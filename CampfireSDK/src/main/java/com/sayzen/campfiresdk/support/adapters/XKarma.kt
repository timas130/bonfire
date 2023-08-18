package com.sayzen.campfiresdk.support.adapters

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerKarma
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaStateChanged
import com.sayzen.campfiresdk.screens.rates.SPublicationRates
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.libs.eventBus.EventBus

class XKarma (
        val publicationId: Long,
        var myKarma: Long,
        val creatorId: Long,
        val publicationStatus: Long,
        var onChanged: (XKarma) -> Unit
) {

    private var noEvent = false
    var anon =  ControllerSettings.anonRates

    private val eventBus = EventBus
            .subscribe(EventPublicationKarmaStateChanged::class) {
                if(noEvent) return@subscribe
                if (it.publicationId == publicationId) onChanged.invoke(this) }
            .subscribe(EventPublicationKarmaAdd::class) {
                if(noEvent) return@subscribe
                if (it.publicationId == publicationId) {
                    myKarma = it.myKarma
                    onChanged.invoke(this)
                }
            }

    constructor(publication: Publication, onChanged: (XKarma) -> Unit) : this(publication.id, publication.myKarma, publication.creator.id, publication.status, onChanged) {
        noEvent = true
        ControllerKarma.set(publication.id, publication.karmaCount)
        noEvent = false
    }

    fun setView(view: ViewKarma) {
        view.update(publicationId, ControllerKarma.get(publicationId), myKarma, creatorId, publicationStatus,
                { b -> rate(b) },
                { Navigator.to(SPublicationRates(publicationId, myKarma, creatorId, publicationStatus)) }
        )
    }

    fun rate(up: Boolean) {
        if (ControllerKarma.getStartTime(publicationId) > 0) {
            if(!ControllerKarma.isSend(publicationId)) {
                ControllerKarma.stop(publicationId)
            }else{
                ToolsToast.show(t(API_TRANSLATE.error_too_late))
            }
        }
        else ControllerKarma.rate(publicationId, up, anon)
    }


}
