package com.sayzen.campfiresdk.screens.chat

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.*
import com.dzen.campfire.api.tools.client.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.AttacheAgent
import com.sayzen.campfiresdk.models.cards.CardChatMessage
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChangedModeration
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.objects.MChatMessagesPool
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

class SChat constructor(
        val chat: Chat,
        var scrollToMessageId: Long
) : SLoadingRecycler<CardChatMessage, PublicationChatMessage>(R.layout.screen_chat), AttacheAgent {

    companion object {

        fun instance(messageId: Long, setStack: Boolean, action: NavigationAction) {
            if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            ApiRequestsSupporter.executeInterstitial(action, RChatGet(ChatTag(), messageId)) { r ->
                for (i in ToolsCollections.copy(Navigator.currentStack.stack)) if (i is SChat && i.chat.tag == r.chat.tag) Navigator.remove(i)
                onChatLoaded(r, messageId, {})
            }.onApiError(API.ERROR_ACCESS) {
                ToolsToast.show(t(API_TRANSLATE.error_chat_access))
            }
        }

        fun instance(tag: ChatTag, scrollToMessageId: Long, setStack: Boolean, action: NavigationAction, onShow: (SChat) -> Unit = {}) {
            tag.targetSubId = if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT || tag.targetSubId != 0L) tag.targetSubId else ControllerApi.getLanguageId()

            if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            if (tryOpenFromBackStack(tag, scrollToMessageId)) return

            ApiRequestsSupporter.executeInterstitial(action, RChatGet(tag, 0)) { r ->
                onChatLoaded(r, scrollToMessageId, onShow)
            }
        }

        private fun tryOpenFromBackStack(tag: ChatTag, messageId: Long): Boolean {
            for (i in Navigator.currentStack.stack) {
                if (i is SChat && i.chat.tag == tag) {
                    Navigator.reorder(i)
                    if (messageId > 0) i.scrollTo(messageId)
                    return true
                }
            }
            return false
        }

        private fun onChatLoaded(r: RChatGet.Response, messageId: Long, onShow: (SChat) -> Unit): SChat {
            r.chat.tag.setMyAccountId(ControllerApi.account.getId())
            ControllerChats.putRead(r.chat.tag, r.chat.anotherAccountReadDate)
            val screen = SChat(r.chat, messageId)
            onShow.invoke(screen)
            return screen
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.eventOnChatTypingChanged(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventFandomBackgroundImageChangedModeration::class) { this.onEventFandomBackgroundImageChangedModeration(it) }
            .subscribe(EventFandomBackgroundImageChanged::class) { this.onEventFandomBackgroundImageChanged(it) }
            .subscribe(EventChatChanged::class) { this.onEventChatChanged(it) }
            .subscribe(EventChatMemberStatusChanged::class) { this.onEventChatMemberStatusChanged(it) }
            .subscribe(EventAccountRemoveFromBlackList::class) { if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE && it.accountId == chat.tag.getAnotherId()) updateChatInfo() }

    private val vLine: View = findViewById(R.id.vLine)
    private val vMenu: ViewIcon
    private val vNotifications: ViewIcon
    private val vTypingText: TextView = findViewById(R.id.vTypingText)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vChatBackground: ImageView = findViewById(R.id.vChatBackground)
    private val vBlackListAlert: TextView = findViewById(R.id.vBlackListAlert)
    private val vBottomArrow: FloatingActionButton = findViewById(R.id.vBottomArrow)
    private val vBottomArrowText: ViewChip = findViewById(R.id.vBottomArrowText)
    private var xAccount: XAccount? = null

    private val fieldLogic = FieldLogic(this)

    private val carSpace = CardSpace(24)
    private var needSendReadRequest = false
    private var loaded = false
    private var scrollToMessageWasLoaded = false
    private var newMessagesScrollCount = 0
    private val addAfterLoadList = ArrayList<PublicationChatMessage>()

    init {
        isNavigationShadowAvailable = false
        forceBackIcon = true
        SActivityTypeBottomNavigation.setShadow(vLine)

        vBottomArrowText.autoControlVisibility = false
        vAvatarTitle.setTitleColor(ToolsResources.getColorAttr(R.attr.colorOnPrimary))
        vAvatarTitle.setSubtitleColor(ToolsResources.getColorAttr(R.attr.colorOnPrimary))
        vAvatarTitle.vSubtitle.maxLines = 2

        vNotifications = addToolbarIcon(R.drawable.ic_notifications_white_24dp) { sendSubscribe(!chat.subscribed) }

        vNotifications.visibility = View.GONE
        ToolsView.setFabColor(vBottomArrow, ToolsResources.getColor(R.color.focus_dark))
        (vBottomArrow as View).visibility = View.INVISIBLE
        vBottomArrow.setOnClickListener {
            if (loaded) vRecycler.scrollToPosition(adapter.itemCount - 1) else reloadChat()
            updateNewMessagesScrollVisible(true)
            ToolsThreads.main(500) { setNewMessagesScrollCount(0) }
        }
        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) vNotifications.visibility = View.VISIBLE
        if (chat.tag.chatType == API.CHAT_TYPE_CONFERENCE && chat.memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE) vNotifications.visibility = View.VISIBLE

        if (chat.tag.chatType == API.CHAT_TYPE_CONFERENCE) vAvatarTitle.setOnClickListener { SChatCreate.instance(chat.tag.targetId, Navigator.TO) }

        vMenu = addToolbarIcon(R.drawable.ic_more_vert_white_24dp) {
            ControllerChats.instanceChatPopup(chat.tag, chat.params, chat.customImageId, chat.memberStatus) { Navigator.remove(this) }.asPopupShow(it)
        }

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_5)
        setTextEmpty(if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) t(API_TRANSLATE.chat_empty_fandom) else t(API_TRANSLATE.chat_empty_private))
        setTextProgress(t(API_TRANSLATE.chat_loading))

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        vRecycler.layoutManager = layoutManager
        vRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                updateNewMessagesScrollVisible()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        updateSubscribed()
        update()
        updateTyping()
        updateBackground()

        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            if (!ControllerSettings.viewedChats.contains(chat.tag.targetId)) {
                ToolsThreads.main(100) { ControllerChats.showFandomChatInfo(chat.tag, chat.params, chat.customImageId) }
            }
        }
        adapter.setBottomLoader { onLoad, cards ->
            if (loaded) {
                onLoad.invoke(emptyArray())
            } else {
                subscription = RChatMessageGetAll(chat.tag,
                        if (cards.isEmpty()) 0 else cards[cards.size - 1].xPublication.publication.dateCreate,
                        false,
                        scrollToMessageId)
                        .onComplete { r ->

                            if (scrollToMessageId > 0) {
                                onLoad.invoke(r.publications)
                            } else {
                                if (loaded) {
                                    onLoad.invoke(emptyArray())
                                    return@onComplete
                                }
                                adapter.remove(carSpace)
                                onLoad.invoke(r.publications)
                                adapter.add(carSpace)
                                if (r.publications.isNotEmpty()) EventBus.post(EventChatNewBottomMessage(r.publications[r.publications.size - 1]))
                                if (r.publications.isEmpty() || r.publications.size < RChatMessageGetAll.COUNT) {
                                    loaded = true
                                    adapter.setShowLoadingCardBottom(false)
                                    EventBus.post(EventChatRead(chat.tag))
                                    adapter.lockBottom()
                                }
                                if (cards.isNotEmpty()) cards.get(cards.size - 1).onSameChanged()
                            }
                        }
                        .onNetworkError { onLoad.invoke(null) }
                        .send(api)
            }
        }
        adapter.setTopLoader { onLoad, cards ->
            subscription = RChatMessageGetAll(chat.tag, if (cards.isEmpty()) 0 else cards[0].xPublication.publication.dateCreate, true, 0)
                    .onComplete { r ->
                        if (cards.isEmpty()) loaded = true
                        onLoad.invoke(r.publications)
                        if (cards.isNotEmpty()) cards.get(cards.size - 1).onSameChanged()
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
        adapter.setRemoveSame(true)
        adapter.setShowLoadingCardBottom(true)
        adapter.setShowLoadingCardTop(true)
        adapter.addOnLoadedPack_NotEmpty {
            ToolsThreads.main(true) {
                while (addAfterLoadList.isNotEmpty() && loaded) addMessage(addAfterLoadList.removeAt(0), true)
                val loadingCards = adapter.get(CardLoading::class)
                for (c in loadingCards) adapter.remove(c)
                for (c in loadingCards) adapter.add(c)
                updateNewMessagesScrollVisible()
            }
            if (scrollToMessageId != 0L) {
                for (c in adapter.get(CardChatMessage::class)) {
                    if (c.xPublication.publication.id == scrollToMessageId) {
                        scrollToMessageId = 0
                        ToolsView.scrollRecycler(vRecycler, adapter.indexOf(c) + 1)
                        ToolsThreads.main(500) { c.flash() }
                    }
                }
                if (scrollToMessageId != 0L) {
                    if (scrollToMessageWasLoaded) {
                        adapter.loadBottom()
                        scrollToMessageWasLoaded = false
                        scrollToMessageId = 0
                    } else {
                        RChatMessageGet(chat.tag, scrollToMessageId)
                                .onComplete {
                                    adapter.loadBottom()
                                    scrollToMessageWasLoaded = true
                                }
                                .onApiError(ApiClient.ERROR_GONE) {
                                    if (it.messageError == RChatMessageGet.GONE_BLOCKED) ControllerApi.showBlockedDialog(it, t(API_TRANSLATE.chat_error_gone_block))
                                    else if (it.messageError == RChatMessageGet.GONE_REMOVE) SplashAlert().setText(t(API_TRANSLATE.chat_error_gone_remove)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
                                    else SplashAlert().setText(t(API_TRANSLATE.chat_error_gone)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
                                    scrollToMessageWasLoaded = false
                                    scrollToMessageId = 0
                                }
                                .send(api)
                    }
                }
            }
        }
        adapter.addOnFinish {
            if (scrollToMessageId > 0 && scrollToMessageWasLoaded) {
                scrollToMessageId = 0
                scrollToMessageWasLoaded = false
                SplashAlert().setText(t(API_TRANSLATE.chat_error_gone_remove)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
            }
        }

        updateChatInfo()
    }

    private fun updateChatInfo() {

        vBlackListAlert.visibility = View.GONE

        ApiRequestsSupporter.execute(RChatGetInfo(chat.tag)) { r ->
            vBlackListAlert.visibility = if (r.anotherAccountIsBlackList || r.myAccountIsBlackList) View.VISIBLE else View.INVISIBLE
            if (r.myAccountIsBlackList) {
                vBlackListAlert.text = t(API_TRANSLATE.chat_black_list_alert_my_account)
                vBlackListAlert.setOnClickListener {}
            }
            if (r.anotherAccountIsBlackList) {
                vBlackListAlert.text = t(API_TRANSLATE.chat_black_list_alert_another_account)
                vBlackListAlert.setOnClickListener {
                    ControllerCampfireSDK.removeFromBlackListUser(chat.tag.getAnotherId())
                }
            }
        }


    }

    private fun setNewMessagesScrollCount(count: Int) {
        if (newMessagesScrollCount == count) return
        newMessagesScrollCount = count
        vBottomArrowText.text = "$newMessagesScrollCount"
        updateNewMessagesScrollVisible()
    }

    private fun updateNewMessagesScrollVisible(forceHide: Boolean = false) {
        ToolsView.alpha(vBottomArrowText, forceHide || isNeedScrollAfterAdd() || newMessagesScrollCount < 1)
        ToolsView.alpha(vBottomArrow, forceHide || isNeedScrollAfterAdd())
    }

    override fun classOfCard() = CardChatMessage::class

    override fun map(item: PublicationChatMessage) = instanceCard(item)

    private fun scrollTo(messageId: Long) {
        var found = false
        val cards = adapter.get(CardChatMessage::class)
        for (i in cards) {
            if (i.xPublication.publication.id == messageId) {
                ToolsView.scrollRecycler(vRecycler, adapter.indexOf(i) + 1)
                ToolsThreads.main(500) {
                    i.flash()
                    updateNewMessagesScrollVisible()
                }
                found = true
                break
            }
        }
        if (!found) {
            scrollToMessageId = messageId
            if (messageId > cards.get(cards.size - 1).xPublication.publication.id) {
                adapter.loadBottom()
            } else {
                adapter.loadTop()
            }
        }
    }

    private fun update() {
        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            val xFandom = XFandom().setId(chat.tag.targetId).setLanguageId(chat.tag.targetSubId).setName(chat.customName).setImageId(chat.customImageId).setOnChanged { update() }
            xFandom.setView(vAvatarTitle)
            vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            vAvatarTitle.setSubtitle(t(API_TRANSLATE.app_subscribers) + ": ${chat.membersCount}")
            vAvatarTitle.setOnClickListener { Navigator.to(SChatSubscribers(chat.tag.targetId, chat.tag.targetSubId, chat.customName)) }
        } else if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) {
            if (xAccount == null) xAccount = XAccount().setAccount(chat.anotherAccount).setOnChanged { update() }
            xAccount!!.setView(vAvatarTitle)

            if (!xAccount!!.isOnline()) {
                vAvatarTitle.setSubtitle(tCap(API_TRANSLATE.app_was_online, ToolsResources.sex(chat.anotherAccount.sex, t(API_TRANSLATE.he_was), t(API_TRANSLATE.she_was)), ToolsDate.dateToString(xAccount!!.getLastOnlineTime())))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            } else {
                vAvatarTitle.setSubtitle(t(API_TRANSLATE.app_online))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.green_700))
            }
        } else {
            ImageLoader.load(chat.customImageId).into(vAvatarTitle.vAvatar.vImageView)
            vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            vAvatarTitle.setSubtitle(t(API_TRANSLATE.app_subscribers) + ": ${chat.membersCount}")
            vAvatarTitle.setTitle(chat.customName)
        }
    }

    private fun updateSubscribed() {
        vNotifications.setFilter(if (chat.subscribed) ToolsResources.getSecondaryColor(context) else ToolsResources.getColorAttr(R.attr.colorOnPrimary))
    }

    private fun updateBackground() {
        if (chat.backgroundImageId > 0 && ControllerSettings.fandomBackground) {
            vChatBackground.visibility = View.VISIBLE
            ImageLoader.load(chat.backgroundImageId).holder(0x00000000).into(vChatBackground)
            vChatBackground.setColorFilter(ToolsColor.setAlpha(210, ToolsResources.getColorAttr(android.R.attr.windowBackground)))
        } else {
            vChatBackground.setImageBitmap(null)
            vChatBackground.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        ControllerNotifications.hide(ControllerNotifications.TYPE_CHAT, chat.tag.asTag())
        if (needSendReadRequest) {
            needSendReadRequest = false
            ControllerChats.readRequest(chat.tag)
            ControllerNotifications.hide(ControllerNotifications.TYPE_CHAT, chat.tag.asTag())
        }
    }

    private fun instanceCard(u: PublicationChatMessage): CardChatMessage {
        return CardChatMessage.instance(u,
                onClick = { publication ->
                    if (ControllerApi.isCurrentAccount(publication.creator.id)) {
                        false
                    } else {
                        fieldLogic.setAnswer(publication, true)
                        true
                    }
                },
                onChange = { publication -> fieldLogic.setChange(publication) },
                onQuote = { publication ->
                    if (!ControllerApi.isCurrentAccount(publication.creator.id)) fieldLogic.setAnswer(publication, false)
                    fieldLogic.setQuote(publication)
                    ToolsView.showKeyboard(fieldLogic.vText)
                },
                onGoTo = { id ->
                    for (i in adapter.get(CardChatMessage::class)) {
                        if (i.xPublication.publication.id == id) {
                            i.flash()
                            val indexReal = adapter.indexOf(i)
                            val indexOffset = if (u.dateCreate < i.xPublication.publication.dateCreate) 1 else -1
                            val indexResult = indexReal + indexOffset
                            ToolsView.scrollRecycler(vRecycler, indexResult)
                            ToolsThreads.main(500) { updateNewMessagesScrollVisible() }
                            return@instance
                        }
                    }
                    ApiRequestsSupporter.executeInterstitial(Navigator.REPLACE, RChatGet(chat.tag, 0)) { r ->
                        onChatLoaded(r, id) {}
                    }

                },
                onBlocked = {
                    addMessage(it, false)
                }
        )
    }

    //
    //  Methods
    //

    private fun updateTyping() {
        val text = ControllerChats.getTypingText(chat.tag)

        if (text == null) {
            vTypingText.text = ""
            vTypingText.visibility = View.GONE
            return
        }

        vTypingText.visibility = View.VISIBLE
        vTypingText.text = text
    }

    fun sendSubscribe(subscribed: Boolean) {
        if (chat.tag.chatType != API.CHAT_TYPE_FANDOM_ROOT && chat.tag.chatType != API.CHAT_TYPE_CONFERENCE) return
        ApiRequestsSupporter.executeProgressDialog(RChatSubscribe(chat.tag, subscribed)) { _ ->
            EventBus.post(EventChatSubscriptionChanged(chat.tag, subscribed))
            if (!subscribed) ControllerChats.setMessages(MChatMessagesPool(chat.tag, false))
        }
    }

    fun isNeedScrollAfterAdd(): Boolean {
        val lm = vRecycler.layoutManager
        if (lm !is LinearLayoutManager) return false
        if (lm.findLastCompletelyVisibleItemPosition() != -1) return lm.findLastCompletelyVisibleItemPosition() > vRecycler.adapter!!.itemCount - 6
        return lm.findLastVisibleItemPosition() > vRecycler.adapter!!.itemCount - 6
    }

    override fun onBackPressed(): Boolean {
        if (fieldLogic.publicationChange != null) {
            fieldLogic.setChange(null)
            return true
        }
        if (fieldLogic.vText.text?.isNotEmpty() == true) {
            fieldLogic.vText.setText("")
            return true
        }
        if (!Navigator.hasBackStack()) {
            Navigator.set(SChats())
            return true
        }
        return super.onBackPressed()
    }

    fun addCard(card: CardSending) {
        if (loaded) {
            adapter.remove(carSpace)
            adapter.add(card)
            adapter.add(carSpace)
            ToolsView.scrollRecycler(vRecycler, vRecycler.adapter!!.itemCount - 1)
        } else {
            adapter.clear()
            adapter.add(card)
            adapter.add(carSpace)
            ToolsView.scrollRecycler(vRecycler, vRecycler.adapter!!.itemCount - 1)
            adapter.reloadTop()
        }
    }

    fun reloadChat() {
        adapter.clear()
        adapter.reloadTop()
    }

    fun addMessage(message: PublicationChatMessage, forceScroll: Boolean, replaceCard: Card? = null) {
        if (!loaded) {
            if (replaceCard != null) adapter.remove(replaceCard)
            addAfterLoadList.add(message)
            return
        }
        val b = isNeedScrollAfterAdd()
        val card = instanceCard(message)
        if (replaceCard == null || !adapter.contains(replaceCard)) {
            if (!adapter.containsSame(card)) {
                adapter.remove(carSpace)
                adapter.add(card)
                adapter.add(carSpace)
            }
        } else {
            adapter.replace(adapter.indexOf(replaceCard), card)
        }

        val index = adapter.indexOf(card)
        if (index > 0) {
            val cardX = adapter.get(index - 1)
            if (cardX is CardChatMessage) {
                cardX.onSameChanged()
                ToolsThreads.main(1000) {
                    cardX.onSameChanged()
                }
            }
        }

        if (forceScroll) {
            ToolsView.scrollRecycler(vRecycler, vRecycler.adapter!!.itemCount)
            setNewMessagesScrollCount(0)
        } else if (b) {
            ToolsView.scrollRecyclerSmooth(vRecycler, vRecycler.adapter!!.itemCount)
            setNewMessagesScrollCount(0)
        } else {
            setNewMessagesScrollCount(newMessagesScrollCount + 1)
        }
        setState(State.NONE)

        ToolsThreads.main(500) { updateNewMessagesScrollVisible() }
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (chat.tag == n.tag && !ControllerApi.isCurrentAccount(n.publicationChatMessage.creator.id)) {
                addMessage(n.publicationChatMessage, false)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) {
                    ControllerNotifications.hide(ControllerNotifications.TYPE_CHAT, chat.tag.asTag())
                    ControllerChats.readRequest(chat.tag)
                } else needSendReadRequest = true


            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (chat.tag == n.tag) {
                addMessage(n.publicationChatMessage, false)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) {
                    ControllerNotifications.hide(ControllerNotifications.TYPE_CHAT, chat.tag.asTag())
                    ControllerChats.readRequest(chat.tag)
                } else needSendReadRequest = true


            }
        }
    }

    private fun onEventChatSubscriptionChanged(e: EventChatSubscriptionChanged) {
        if (e.tag == chat.tag) {
            chat.subscribed = e.subscribed
            chat.membersCount += if (e.subscribed) 1 else -1
            update()
            updateSubscribed()
        }
    }

    private fun onEventFandomBackgroundImageChangedModeration(e: EventFandomBackgroundImageChangedModeration) {
        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            if (chat.tag.targetId == e.fandomId && chat.tag.targetSubId == e.languageId) {
                chat.backgroundImageId = e.imageId
                updateBackground()
            }
            if (chat.tag.targetId == 0L && chat.tag.targetSubId == 0L) {
                updateBackground()
            }
        }
    }

    private fun onEventFandomBackgroundImageChanged(e: EventFandomBackgroundImageChanged) {
        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB || chat.tag.chatType == API.CHAT_TYPE_CONFERENCE) {
            if (chat.tag == e.chatTag) {
                chat.backgroundImageId = e.imageId
                updateBackground()
            }
            if (chat.tag.targetId == 0L && chat.tag.targetSubId == 0L) {
                updateBackground()
            }
        }
    }

    private fun onEventChatChanged(e: EventChatChanged) {
        if (chat.tag.chatType == API.CHAT_TYPE_CONFERENCE && e.chatId == chat.tag.targetId) {
            chat.customName = e.name
            chat.customImageId = e.imageId
            chat.membersCount = e.accountCount.toLong()
            update()
        }
    }

    private fun onEventChatMemberStatusChanged(e: EventChatMemberStatusChanged) {
        if (chat.tag.chatType == e.tag.chatType && e.tag.targetId == chat.tag.targetId && e.tag.targetSubId == chat.tag.targetSubId) {
            if (e.accountId == ControllerApi.account.getId()) chat.memberStatus = e.status
            update()
        }
    }

    private fun eventOnChatTypingChanged(e: EventChatTypingChanged) {
        if (e.tag == chat.tag) updateTyping()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBus.unsubscribe()
    }

    //
    //  Share
    //

    override fun attacheText(text: String, postAfterAdd: Boolean) {
        fieldLogic.setText(text)
    }

    override fun attacheImage(image: Uri, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()

        ToolsBitmap.getFromUri(image, {
            if (it == null) {
                dialog.hide()
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                return@getFromUri
            }

            fieldLogic.attach.setImageBitmapNow(it, dialog)
        }, {
            dialog.hide()
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
        })
    }

    override fun attacheImage(image: Bitmap, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        fieldLogic.attach.setImageBitmapNow(image, dialog)
    }

    override fun attacheAgentIsActive() = Navigator.getCurrent() == this

}
