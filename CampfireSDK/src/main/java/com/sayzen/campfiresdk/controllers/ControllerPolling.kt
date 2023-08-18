package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGet
import com.dzen.campfire.api.requests.post.RPostPagePollingVote
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.publications.EventPollingChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

object ControllerPolling {

    //
    //  Request Polling
    //

    private val results = HashMap<Long, Result>()

    fun clear() {
        results.clear()
    }

    fun get(pollingId: Long, callback: (Result) -> Unit) {
        val result = results[pollingId]
        if (result != null) {
            callback.invoke(result)
            if (result.dataCreate < System.currentTimeMillis() - 1000L * 30) load(pollingId)
        } else {
            load(pollingId, callback)
        }
    }

    fun reload(pollingId: Long) {
        results.remove(pollingId)
    }

    private fun load(pollingId: Long, callback: (Result) -> Unit = {}, tryCount: Int = 3) {
        RPostPagePollingGet(pollingId)
                .onComplete { r ->
                    val result = Result(r.results)
                    results[pollingId] = result
                    callback.invoke(result)
                }
                .onError {
                    if (tryCount > -1) ToolsThreads.main(1000) { load(pollingId, callback, tryCount - 1) }
                }
                .send(api)
    }

    class Result(var results: Array<PagePolling.Result>) {

        var voted = false
        var myVoteItemId = -1L
        var totalVotes = 0L
        var dataCreate = System.currentTimeMillis()

        init {
            totalVotes = 0L
            voted = false

            for (i in 0 until results.size) {
                totalVotes += results[i].count
                voted = voted || results[i].myVote
                if (results[i].myVote) myVoteItemId = results[i].itemId
            }
        }

        fun count(itemId: Long): Long {
            for (i in 0 until results.size)
                if (results[i].itemId == itemId) return results[i].count
            return 0L
        }

    }

    //
    //  Vote
    //

    private val votes = HashMap<Long, Vote>()

    fun vote(sourceType: Long, sourceId: Long, sourceIdSub: Long, pollingId: Long, itemId: Long) {
        if (votes[pollingId] != null && votes[pollingId]!!.itemId == itemId) {
            stop(pollingId)
        } else {
            if (votes[pollingId] != null) stop(pollingId)
            votes[pollingId] = Vote(pollingId, sourceType, sourceId, sourceIdSub, itemId)
            EventBus.post(EventPollingChanged(pollingId))
        }
    }

    fun stop(pollingId: Long) {
        votes[pollingId]?.clearVote()
    }

    fun getStartTime(pollingId: Long): Long {
        return votes[pollingId]?.rateStartTime ?: 0L
    }

    fun getStartItem(pollingId: Long): Long {
        return votes[pollingId]?.itemId ?: 0L
    }


    private class Vote(
            val pollingId: Long,
            val sourceType: Long,
            val sourceId: Long,
            val sourceIdSub: Long,
            val itemId: Long,
            val rateStartTime: Long = System.currentTimeMillis()
    ) {

        private val subscription: Subscription

        init {
            subscription = ToolsThreads.main(CampfireConstants.VOTE_TIME) {

                get(pollingId) { result ->
                    ApiRequestsSupporter.execute(RPostPagePollingVote(sourceType, sourceId, sourceIdSub, pollingId, itemId)) { _ ->

                        result.totalVotes++
                        result.myVoteItemId = itemId
                        result.voted = true

                        var updated = false
                        for (item in result.results) {
                            if (item.itemId == itemId) {
                                item.count++
                                item.myVote = true
                                updated = true
                                break
                            }
                        }

                        if (!updated) {
                            result.results = Array(result.results.size + 1) {
                                if (it < result.results.size) result.results[it]
                                else {
                                    val item = PagePolling.Result()
                                    item.itemId = itemId
                                    item.myVote = true
                                    item.count = 1
                                    item
                                }
                            }
                        }

                        EventBus.post(EventPollingChanged(pollingId))
                    }
                            .onApiError(RPostPagePollingVote.E_ALREADY) {
                                reload(pollingId)
                                EventBus.post(EventPollingChanged(pollingId))
                            }
                            .onFinish() {
                                clearVote()
                            }
                }
            }
        }

        fun clearVote() {
            subscription.unsubscribe()
            if (votes[pollingId] == this) votes.remove(pollingId)
            EventBus.post(EventPollingChanged(pollingId))
        }


    }


}