package com.sayzen.campfiresdk.compose.publication.post.pages.polling

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGet
import com.dzen.campfire.api.requests.post.RPostPagePollingVote
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.compose.publication.post.pages.PagesSource
import com.sayzen.campfiresdk.compose.util.mapState
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.classes.collections.Cache
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class PollingData(var results: Array<PagePolling.Result> = arrayOf()) : JsonParsable {
    override fun json(inp: Boolean, json: Json): Json {
        results = json.m(inp, "results", results)
        return json
    }
}

private val pollCache = Cache<Long, PollingData>(50)

val Account.ageDays: Long
    get() = (ControllerApi.currentTime() - dateCreate) / 3600000 / 24 + 1

class PollingModel(val page: PagePolling, val source: PagesSource) {
    private val _data = MutableStateFlow<PollingData?>(null)
    val data = _data.asStateFlow()

    private val _votingFor = MutableStateFlow<Int?>(null)
    val votingFor = _votingFor.asStateFlow()

    private val _isTryingToLoad = MutableStateFlow(false)
    val isTryingToLoad = _isTryingToLoad.asStateFlow()

    private var votingJob: Job? = null

    init {
        val cached = pollCache[page.pollingId]
        if (cached != null) {
            _data.tryEmit(cached)
        } else {
            reload()
        }
    }

    fun reload() {
        if (source.editMode) return
        if (_isTryingToLoad.value) return

        _isTryingToLoad.tryEmit(true)
        ApiRequestsSupporter.executeWithRetry(
            tryCount = 3,
            request = RPostPagePollingGet(page.pollingId),
            onFailure = {
                _isTryingToLoad.tryEmit(false)
            }
        ) { resp ->
            val data = PollingData(resp.results)
            _data.tryEmit(data)
            pollCache[page.pollingId] = data

            _isTryingToLoad.tryEmit(false)
        }
    }

    fun getResult(index: Int): StateFlow<PagePolling.Result> {
        return data.mapState { data ->
            data?.results?.find { it.itemId == index.toLong() }
                ?: PagePolling.Result().apply { itemId = index.toLong() }
        }
    }

    val totalVotes: StateFlow<Long>
        get() = data.mapState { pollData -> pollData?.results?.sumOf { it.count } ?: 0L }

    fun getCanVote(account: Account = ControllerApi.account.getAccount()): Boolean {
        return account.lvl >= page.minLevel &&
                account.karma30 >= page.minKarma &&
                account.ageDays >= page.minDays &&
                // not in blacklist
                page.blacklist.find { it.id == account.id } == null &&
                // have not voted
                data.value?.results?.find { it.myVote } == null &&
                // not editing
                !source.editMode
    }

    fun getShowResults(account: Account = ControllerApi.account.getAccount()): StateFlow<Boolean> {
        return data.mapState { data ->
            if (source.editMode) return@mapState false
            data?.results?.find { it.myVote } != null || !getCanVote(account)
        }
    }

    private suspend fun edit(editor: PollingData.() -> Unit) {
        val old = withTimeout(2000) {
            data.first { it != null }!!
        }

        old.editor()
        val copyJson = old.json(true, Json())
        val copy = PollingData()
        copy.json(false, copyJson)

        _data.tryEmit(copy)
    }

    fun vote(scope: CoroutineScope, index: Int) {
        if (!getCanVote()) return

        votingJob?.cancel()
        if (_votingFor.value == index) {
            _votingFor.tryEmit(null)
            return
        }

        _votingFor.tryEmit(index)
        votingJob = scope.launch {
            delay(CampfireConstants.VOTE_TIME)

            try {
                RPostPagePollingVote(
                    sourceType = source.sourceType,
                    sourceId = source.sourceId,
                    sourceIdSub = source.sourceSubId,
                    pollingId = page.pollingId,
                    itemId = index.toLong(),
                )
                .onApiError(RPostPagePollingVote.E_ALREADY) {
                    ToolsToast.show(SupAndroid.appContext!!.getString(R.string.polling_already_voted))
                }
                .sendSuspendExt()

                edit {
                    val existingResult = results.find { it.itemId == index.toLong() }
                    if (existingResult != null) {
                        existingResult.myVote = true
                        existingResult.count++
                    } else {
                        results += PagePolling.Result().apply {
                            itemId = index.toLong()
                            count = 1
                            myVote = true
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                _votingFor.tryEmit(null)
                votingJob = null
            }
        }
    }
}
