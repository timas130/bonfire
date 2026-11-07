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
    val loadedAt = System.currentTimeMillis()

    override fun json(inp: Boolean, json: Json): Json {
        results = json.m(inp, "results", results)
        return json
    }
}

private val pollCache = Cache<Long, MutableStateFlow<PollingData?>>(50)
private const val POLL_CACHE_TTL = 1000L * 30

fun clearPollCache() {
    pollCache.clear()
}

val Account.ageDays: Long
    get() = (ControllerApi.currentTime() - dateCreate) / 3600000 / 24 + 1

class PollingModel(val page: PagePolling, val source: PagesSource) {
    private val _data = pollCache[page.pollingId]
        ?: MutableStateFlow<PollingData?>(null).also { pollCache[page.pollingId] = it }
    val data = _data.asStateFlow()

    private val _votingFor = MutableStateFlow<Int?>(null)
    val votingFor = _votingFor.asStateFlow()

    private val _isTryingToLoad = MutableStateFlow(false)
    val isTryingToLoad = _isTryingToLoad.asStateFlow()

    private var votingJob: Job? = null
    private var isSendingVote = false

    init {
        val cached = _data.value
        if (cached != null) {
            if (System.currentTimeMillis() - cached.loadedAt > POLL_CACHE_TTL) reload()
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

            _isTryingToLoad.tryEmit(false)
        }.onApiError {
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
        get() = data.mapState { pollData ->
            pollData?.results?.filter { it.itemId >= 0 && it.itemId < page.options.size }?.sumOf { it.count } ?: 0L
        }

    fun getCanVote(account: Account = ControllerApi.account.getAccount()): Boolean {
        return account.lvl >= page.minLevel &&
                account.karma30 >= page.minKarma &&
                account.ageDays >= page.minDays &&
                // not in blacklist
                page.blacklist.find { it.id == account.id } == null &&
                // have not voted
                data.value?.results?.find { it.myVote } == null &&
                // not editing
                !source.editMode &&
                // not expired
                (page.duration <= 0 || page.duration >= (
                    System.currentTimeMillis() - source.sourceDateCreate.coerceAtLeast(page.dateCreate)
                ))
    }

    fun getShowResults(account: Account = ControllerApi.account.getAccount()): StateFlow<Boolean> {
        return data.mapState { data ->
            if (source.editMode) return@mapState false
            data?.results?.find { it.myVote } != null || !getCanVote(account)
        }
    }

    private suspend fun edit(editor: PollingData.() -> Unit) {
        val old = withTimeoutOrNull(2000) {
            data.first { it != null }!!
        }
        if (old == null) {
            reload()
            return
        }

        old.editor()
        val copyJson = old.json(true, Json())
        val copy = PollingData()
        copy.json(false, copyJson)

        _data.tryEmit(copy)
    }

    fun vote(scope: CoroutineScope, index: Int) {
        if (!getCanVote()) return
        if (isSendingVote) return

        votingJob?.cancel()
        if (_votingFor.value == index) {
            _votingFor.tryEmit(null)
            return
        }

        _votingFor.tryEmit(index)
        votingJob = scope.launch {
            delay(CampfireConstants.VOTE_TIME)

            isSendingVote = true
            try {
                withContext(NonCancellable) {
                    RPostPagePollingVote(
                        sourceType = source.sourceType,
                        sourceId = source.sourceId,
                        sourceIdSub = source.sourceSubId,
                        pollingId = page.pollingId,
                        itemId = index.toLong(),
                    )
                    .onApiError(RPostPagePollingVote.E_ALREADY) {
                        ToolsToast.show(SupAndroid.appContext!!.getString(R.string.polling_already_voted))
                        reload()
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
                }
            } catch (_: Exception) {
            } finally {
                _votingFor.tryEmit(null)
                votingJob = null
                isSendingVote = false
            }
        }
    }
}
