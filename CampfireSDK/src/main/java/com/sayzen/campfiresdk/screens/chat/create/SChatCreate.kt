package com.sayzen.campfiresdk.screens.chat.create

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatMember
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.dzen.campfire.api.requests.chat.RChatChange
import com.dzen.campfire.api.requests.chat.RChatCreate
import com.dzen.campfire.api.requests.chat.RChatGetForChange
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.chat.EventChatChanged
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus

class SChatCreate(
        var chatId: Long,
        val changeName: String,
        val changeImageId: Long,
        val accounts: Array<ChatMember>,
        val myLvl: Long,
        val params: ChatParamsConf
) : Screen(R.layout.screen_chat_create) {

    companion object {

        fun instance(chatId: Long, action: NavigationAction) {

            ApiRequestsSupporter.executeProgressDialog(RChatGetForChange(chatId)) { r ->
                Navigator.action(action, SChatCreate(chatId, r.chatName, r.chatImageId, r.accounts, r.myLvl, r.params))
            }
                    .onApiError(API.ERROR_ACCESS) { ToolsToast.show(t(API_TRANSLATE.error_chat_access)) }

        }

    }


    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val adapter = RecyclerCardAdapter()

    private val cardTitle = CardCreateTitle(chatId, myLvl, changeName, changeImageId, params) { updateFinish() }

    constructor() : this(0, "", 0, emptyArray(), API.CHAT_MEMBER_LVL_ADMIN, ChatParamsConf())

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.chat_create_title))

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        adapter.add(cardTitle)
        for (a in accounts) adapter.add(CardChatMember(a, myLvl))
        if (params.allowUserInvite || myLvl != API.CHAT_MEMBER_LVL_USER) addUserButton()
        adapter.add(CardSpace(128))

        vFab.setOnClickListener { create() }
        ToolsView.disableScrollViewJump(vRecycler)
        updateFinish()
    }

    override fun onFirstShow() {
        super.onFirstShow()
        vFab.requestFocus() //  Шоб поле не выделялось
    }

    private fun addUserButton() {
        adapter.add(CardMenu().setText(t(API_TRANSLATE.app_add)).setIcon(ToolsResources.getDrawable(R.drawable.ic_add_white_18dp)).setOnClick {
            Navigator.to(SAccountSearch(false) {
                for (c in adapter.get(CardChatMember::class)) if (c.chatMember.account.id == it.id) {
                    ToolsToast.show(t(API_TRANSLATE.chat_create_error_account_exist))
                    return@SAccountSearch
                }

                val m = ChatMember()
                m.account = it
                m.memberLvl = API.CHAT_MEMBER_LVL_USER
                adapter.add(adapter.size() - 2, CardChatMember(m, myLvl))
            })
        })
    }

    private fun updateFinish() {
        val b = (chatId != 0L || cardTitle.image != null) && cardTitle.text.isNotEmpty() && cardTitle.text.length <= API.CHAT_NAME_MAX
        ToolsView.setFabEnabledR(vFab, b, R.color.green_700)
    }

    private fun create() {

        if (chatId == 0L) {

            ApiRequestsSupporter.executeProgressDialog(RChatCreate(cardTitle.text, cardTitle.image)) { r ->
                cardTitle.text = ""
                cardTitle.image = null
                tag = r.tag
                chatId = r.tag.targetId
                sendChange()
            }

        } else {
            sendChange()
        }


    }

    private fun sendChange() {


        val accountsList = ArrayList<Long>()
        val removeAccountList = ArrayList<Long>()
        val changeAccountList = ArrayList<Long>()
        val changeAccountListLevels = ArrayList<Long>()

        for (c in adapter.get(CardChatMember::class)) {
            if (c.chatMember.account.id != ControllerApi.account.getId()) {
                accountsList.add(c.chatMember.account.id)
                val foundLvl = c.newLevel
                var found: ChatMember? = null
                for (a in accounts) if (a.account.id == c.chatMember.account.id) found = a

                if (found == null) {
                    if (foundLvl > 0 && foundLvl != API.CHAT_MEMBER_LVL_USER) {
                        changeAccountList.add(c.chatMember.account.id)
                        changeAccountListLevels.add(foundLvl)
                    }
                } else {
                    if (foundLvl > 0 && foundLvl != found.memberLvl) {
                        changeAccountList.add(c.chatMember.account.id)
                        changeAccountListLevels.add(foundLvl)
                    }
                }
            }
        }

        for (a in accounts) {
            if (a.account.id == ControllerApi.account.getId()) continue
            var found: ChatMember? = null
            for (c in adapter.get(CardChatMember::class)) if (a.account.id == c.chatMember.account.id) found = c.chatMember
            if (found == null) removeAccountList.add(a.account.id)
        }


        ApiRequestsSupporter.executeProgressDialog(RChatChange(chatId, cardTitle.text, cardTitle.image, accountsList.toTypedArray(), removeAccountList.toTypedArray(), changeAccountList.toTypedArray(), changeAccountListLevels.toTypedArray(), params)) { r ->
            if (cardTitle.image != null) ImageLoaderId(changeImageId).clear()
            var count = 0
            for (c in adapter.get(CardChatMember::class)) if (c.chatMember.memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE || c.chatMember.memberStatus == 0L) count++
            EventBus.post(EventChatChanged(chatId, cardTitle.text, changeImageId, count))
            Navigator.remove(this)
            SChat.instance(r.tag, 0, false, Navigator.TO)
        }.onApiError { exception ->
            if (exception.code == RChatChange.E_CONFERENCE_BLOCK) {
                SplashAlert()
                    .setText(t(API_TRANSLATE.error_blocked_adding_to_conferences))
                    .addLine("")
                    .addLine(exception.params.joinToString(", ") { accountId ->
                        val id = accountId.toLong()
                        adapter.get(CardChatMember::class)
                            .find { it.chatMember.account.id == id }!!
                            .chatMember.account.name
                    })
                    .setOnEnter(t(API_TRANSLATE.app_ok))
                    .asSheetShow()
            }
        }

    }

    private fun notChanged() = chatId != 0L || (cardTitle.image == null && cardTitle.text.isEmpty() && adapter.get(CardChatMember::class).size < 2)

    override fun onBackPressed(): Boolean {
        if (notChanged()) return false
        SplashAlert()
                .setText(t(API_TRANSLATE.post_create_cancel_alert))
                .setOnEnter(t(API_TRANSLATE.app_yes)) { Navigator.remove(this) }
                .setOnCancel(t(API_TRANSLATE.app_no))
                .asSheetShow()
        return true
    }

}