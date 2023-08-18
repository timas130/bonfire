package com.sayzen.campfiresdk.support.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGetVotes
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.publications.EventPollingChanged
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import kotlin.reflect.KClass

class XPolling(
        val page: PagePolling,
        val pagesContainer: PagesContainer?,
        val isEditModeProvider: ()->Boolean,
        val isPostIsDraftProvider: ()->Boolean,
        var onChanged: () -> Unit
) {

    // you're welcome - see git blame
    val eventBus = EventBus.subscribe(EventPollingChanged::class) {
        if (it.pollingId == page.pollingId) onChanged.invoke()
    }

    fun setView(view: View) {
        updateLimits(view)
        updateTitle(view)
        updateVotes(view)

        updateResultsButton(view)
        updateBlacklistButton(view)
    }

    private fun updateResultsButton(view: View) {
        val vPollResults: ViewButton = view.findViewById(R.id.vPollResults)

        val resultsRecycler = object : SLoadingRecycler<CardAccount, RPostPagePollingGetVotes.PollingResultsItem>() {
            init {
                disableShadows()
                disableNavigation()

                setTitle(t(API_TRANSLATE.post_page_polling_results_full))
                setTextEmpty(t(API_TRANSLATE.app_empty))
                setBackgroundImage(419896L)

                adapter.setBottomLoader { onLoad, cards ->
                    if (pagesContainer == null) {
                        onLoad(null)
                        return@setBottomLoader
                    }
                    subscription = RPostPagePollingGetVotes(
                        pagesContainer.getSourceType(),
                        pagesContainer.getSourceId(),
                        pagesContainer.getSourceIdSub(),
                        page.pollingId,
                        cards.size
                    )
                        .onComplete { onLoad(it.results) }
                        .onError { onLoad(null) }
                        .send(api)
                }
            }

            override fun classOfCard(): KClass<CardAccount> = CardAccount::class
            override fun map(item: RPostPagePollingGetVotes.PollingResultsItem): CardAccount {
                val card = CardAccount(item.account)
                card.setSubtitle(page.options[item.itemId.toInt()])
                return card
            }
        }

        vPollResults.text = t(API_TRANSLATE.post_page_polling_results)
        vPollResults.setOnClickListener {
            Navigator.to(resultsRecycler)
        }
    }

    private fun updateBlacklistButton(view: View) {
        val vBlacklistView: ViewButton = view.findViewById(R.id.vBlackListView)
        if (page.blacklist.isEmpty()) vBlacklistView.visibility = View.GONE
        else {
            val blacklistRecycler = object : SRecycler() {
                private val adapter: RecyclerCardAdapter = RecyclerCardAdapter()

                init {
                    disableShadows()
                    disableNavigation()

                    setTitle(t(API_TRANSLATE.settings_black_list))

                    for (account in page.blacklist) adapter.add(CardAccount(account))
                    vRecycler.adapter = adapter
                }
            }
            vBlacklistView.visibility = View.VISIBLE
            vBlacklistView.text = t(API_TRANSLATE.settings_black_list)
            vBlacklistView.setOnClickListener {
                Navigator.to(blacklistRecycler)
            }
        }
    }

    private fun updateVotes(view: View) {
        val vContainer: ViewGroup = view.findViewById(R.id.vContainer)
        val tag = System.currentTimeMillis()
        vContainer.tag = tag
        vContainer.removeAllViews()

        for (s in page.options) {
            val vItem: View = ToolsView.inflate(R.layout.card_page_polling_item)
            val vText: TextView = vItem.findViewById(R.id.vText)
            vText.text = s
            vContainer.addView(vItem)
        }

        if (!isEditModeProvider.invoke() && !isPostIsDraftProvider.invoke()) {
            ControllerPolling.get(page.pollingId) { result ->

                if (vContainer.tag != tag) return@get

                val showResults = result.voted || !canVote()

                if (showResults) {
                    val vPollResults: ViewButton = view.findViewById(R.id.vPollResults)
                    vPollResults.visibility = View.VISIBLE
                }

                var percentSum = 0
                for (i in 0 until vContainer.childCount) {
                    val vItem: View = vContainer.getChildAt(i)
                    val vCount: TextView = vItem.findViewById(R.id.vCount)
                    val vPercent: TextView = vItem.findViewById(R.id.vPercent)
                    val vTouch: View = vItem.findViewById(R.id.vTouch)
                    val vLine1: View = vItem.findViewById(R.id.vLine1)
                    val vLine2: View = vItem.findViewById(R.id.vLine2)
                    val vProgress: ViewProgressLine = vItem.findViewById(R.id.vProgress)

                    vCount.visibility = if (showResults) View.VISIBLE else View.INVISIBLE
                    vPercent.visibility = if (showResults) View.VISIBLE else View.INVISIBLE
                    vProgress.visibility = View.INVISIBLE

                    val percent = (result.count(i.toLong()).toFloat() / result.totalVotes * 100).toInt()
                    percentSum += percent
                    if (i == vContainer.childCount - 1) {
                        for (n in 0 until vContainer.childCount) {
                            val p = (result.count(n.toLong()).toFloat() / result.totalVotes * 100).toInt()
                            if (p > 0) {
                                (vContainer.getChildAt(n).findViewById(R.id.vPercent) as TextView).text = "${p + (100 - percentSum)}%"
                                break
                            }
                        }
                    }

                    if (showResults) {
                        vCount.text = "(${result.count(i.toLong())})"
                        if (result.totalVotes == 0L) vPercent.text = "0%"
                        else vPercent.text = "$percent%"
                        (vLine1.layoutParams as LinearLayout.LayoutParams).weight = 100 - percent.toFloat()
                        (vLine2.layoutParams as LinearLayout.LayoutParams).weight = percent.toFloat()
                        vLine1.requestLayout()
                    }

                    if (result.myVoteItemId == i.toLong()) {
                        vLine1.setBackgroundColor(ToolsResources.getSecondaryColor(vLine1.context))
                    } else {
                        vLine1.setBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
                    }

                    if (!showResults) {
                        vTouch.setOnClickListener {
                            if (pagesContainer != null)
                                ControllerPolling.vote(pagesContainer.getSourceType(), pagesContainer.getSourceId(), pagesContainer.getSourceIdSub(), page.pollingId, i.toLong())
                        }
                    }

                    val startTime = ControllerPolling.getStartTime(page.pollingId)
                    val startItem = ControllerPolling.getStartItem(page.pollingId)

                    if(startTime > 0 && startItem == i.toLong()){

                        val itemTag = System.currentTimeMillis()
                        vItem.tag = itemTag
                        ToolsThreads.timerMain(10){
                            if(vItem.tag != itemTag || ControllerPolling.getStartTime(page.pollingId) <= 0L || ControllerPolling.getStartItem(page.pollingId) != i.toLong()){
                                it.unsubscribe()
                                return@timerMain
                            }
                            vProgress.visibility = View.VISIBLE
                            vProgress.setProgress(System.currentTimeMillis() - startTime, CampfireConstants.VOTE_TIME)
                        }
                    }

                }
            }
        }

    }

    private fun updateTitle(view: View){
        val vTitle: ViewText = view.findViewById(R.id.vTitle)

        ControllerLinks.makeLinkable(vTitle)

        vTitle.visibility = if (page.title.isEmpty()) View.GONE else View.VISIBLE
        vTitle.text = page.title

    }

    private fun updateLimits(view: View){
        val vLimit: ViewText = view.findViewById(R.id.vLimit)

        if (page.minKarma <= 0 && page.minLevel <= 0 && page.minDays <= 0) {
            vLimit.visibility = View.GONE
        } else {
            vLimit.visibility = View.VISIBLE
            vLimit.text = "${t(API_TRANSLATE.app_limitations)}: "
            if (page.minLevel > 0) vLimit.text = "${vLimit.text} ${t(API_TRANSLATE.app_level)} ${ToolsText.numToStringRoundAndTrim(page.minLevel / 100f, 2)}  "
            if (page.minKarma > 0) vLimit.text = "${vLimit.text} ${t(API_TRANSLATE.app_karma)} ${((page.minKarma / 100).toInt())}"
            if (page.minDays > 0) vLimit.text = "${vLimit.text} ${t(API_TRANSLATE.post_page_polling_limit_days)} ${page.minDays}"
            if (page.blacklist.find { it.id == ControllerApi.account.getId() } != null)
                vLimit.text = "${vLimit.text}  ${t(API_TRANSLATE.settings_black_list)}"
            vLimit.setTextColor(ToolsResources.getColor(if (!canVote()) R.color.red_700 else R.color.green_700))
        }
    }

    private fun canVote() =
            ControllerApi.account.getLevel() >= page.minLevel &&
            ControllerApi.account.getKarma30() >= page.minKarma &&
            ((ControllerApi.currentTime() - ControllerApi.account.getDateAccountCreated()) / (3600000L * 24) + 1) >= page.minDays &&
            page.blacklist.find { it.id == ControllerApi.account.getId() } == null
}
