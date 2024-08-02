package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.publications.RPublicationsKarmaAdd
import com.posthog.PostHog
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaStateChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

object ControllerKarma {

    //
    //  Karma Cash
    //

    private val karma = HashMap<Long, Long>()

    fun set(publicationId: Long, karmaCount: Long) {
        if (karma[publicationId] == karmaCount) return
        karma[publicationId] = karmaCount
        EventBus.post(EventPublicationKarmaStateChanged(publicationId))
    }

    fun get(publicationId: Long) = karma[publicationId] ?: 0L

    fun add(publicationId: Long, amount: Long) {
        set(publicationId, get(publicationId) + amount)
    }

    //
    //  Rate
    //

    private val rates = HashMap<Long, Rate>()

    fun stop(publicationId: Long) {
        val rate = rates[publicationId]
        if (rate != null) {
            rate.clearRate()
            rates.remove(publicationId)
            EventBus.post(EventPublicationKarmaStateChanged(publicationId))
        }
    }

    fun rate(publicationId: Long, up: Boolean, anon: Boolean) {
        stop(publicationId)
        val rate = Rate(publicationId, up, anon)
        rate.start()
        rates[publicationId] = rate
        EventBus.post(EventPublicationKarmaStateChanged(publicationId))
    }

    fun rateNow(publicationId: Long, up: Boolean, anon: Boolean) {
        stop(publicationId)
        Rate(publicationId, up, anon).rateNow()
    }

    fun getStartTime(publicationId: Long): Long {
        val rate = rates[publicationId]
        return rate?.rateStartTime ?: 0L
    }

    fun isSend(publicationId: Long): Boolean {
        val rate = rates[publicationId]
        return rate?.isSend() ?: false
    }

    fun getIsUp(publicationId: Long): Boolean {
        val rate = rates[publicationId]
        return rate?.up ?: false
    }

    fun getKarmaColor(amount: Long): Int = when {
        amount > 0 -> 0xFF388E3C.toInt()
        amount < 0 -> 0xFFD32F2F.toInt()
        else -> 0xFF757575.toInt()
    }
    fun getKarmaColorHex(amount: Long): String = when {
        amount > 0 -> "388E3C"
        amount < 0 -> "D32F2F"
        else -> "757575"
    }

    private class Rate(
            val publicationId: Long,
            val up: Boolean,
            val anon: Boolean,
            val rateStartTime: Long = System.currentTimeMillis()
    ) {
        private lateinit var subscription: Subscription
        private var isSend = false

        fun start() {
            subscription = ToolsThreads.main(CampfireConstants.RATE_TIME) {
                rateNow()
            }
        }

        fun rateNow() {
            isSend = true
            ApiRequestsSupporter.execute(
                RPublicationsKarmaAdd(
                    publicationId,
                    up,
                    ControllerApi.getLanguageId(),
                    anon
                )
            ) { r ->
                PostHog.capture("karma_rate", properties = mapOf("up" to up, "anon" to anon))

                set(publicationId, get(publicationId) + r.myKarmaCount)
                EventBus.post(EventPublicationKarmaAdd(publicationId, r.myKarmaCount))
                ControllerStoryQuest.incrQuest(API.QUEST_STORY_KARMA)
                ToolsThreads.main(true) { EventBus.post(EventPublicationKarmaStateChanged(publicationId)) }
            }
                .onApiError(RPublicationsKarmaAdd.E_SELF_PUBLICATION) { ToolsToast.show(t(API_TRANSLATE.error_rate_self_publication)) }
                .onApiError(RPublicationsKarmaAdd.E_CANT_DOWN) { ToolsToast.show(t(API_TRANSLATE.error_rate_cant_down)) }
                .onFinish { stop(publicationId) }
        }

        fun clearRate() {
            subscription.unsubscribe()
        }

        fun isSend() = isSend


    }


}
