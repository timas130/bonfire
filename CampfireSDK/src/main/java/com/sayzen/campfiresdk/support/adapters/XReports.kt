package com.sayzen.campfiresdk.support.adapters

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.EventPublicationInstance
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.publications.*
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.eventBus.EventBus

class XReports(
        private val publication: Publication,
        var onChanged: () -> Unit
) {

    companion object {
        private val eventBus = EventBus
                .subscribe(EventPublicationInstance::class) { set(it.publication.id, it.publication.reportsCount, System.currentTimeMillis()) }
                .subscribe(EventPublicationReportsAdd::class) { set(it.publicationId, get(it.publicationId) + 1, System.currentTimeMillis()) }
                .subscribe(EventPublicationReportsClear::class) {  set(it.publicationId, 0, System.currentTimeMillis()) }
        private val reports = HashMap<Long, Item2<Long, Long>>()

        fun set(publicationId: Long, reportsCount: Long, publicationInstanceDate: Long) {
            if (!reports.containsKey(publicationId)) {
                reports[publicationId] = Item2(reportsCount, publicationInstanceDate)
                EventBus.post(EventReportsCountChanged(publicationId, reportsCount, reportsCount))
            } else {
                if (reports[publicationId]!!.a2 < publicationInstanceDate && reports[publicationId]!!.a1 != reportsCount) {
                    val old = reports[publicationId]!!.a1
                    reports[publicationId] = Item2(reportsCount, publicationInstanceDate)
                    EventBus.post(EventReportsCountChanged(publicationId, reportsCount, reportsCount - old))
                }
            }
        }

        fun get(publicationId: Long) = if (reports.containsKey(publicationId)) reports[publicationId]!!.a1 else 0L

    }

    private val eventBus = EventBus.subscribe(EventReportsCountChanged::class) {
        if (it.publicationId == publication.id) {
            publication.reportsCount = get(publication.id)
            onChanged.invoke()
        }
    }

    fun setView(view: TextView) {
        view.text = "${publication.reportsCount}"
        view.visibility = if (publication.reportsCount > 0 && ControllerApi.can(publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE
    }


}
