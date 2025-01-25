package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsAdminStatusRemove
import com.dzen.campfire.api.requests.accounts.RAccountsStatusSet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountNoteChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountStatusChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import sh.sit.bonfire.formatting.BonfireMarkdown

class CardStatus(
    private val xAccount: XAccount
) : Card(R.layout.screen_account_card_status) {
    private val eventBus = EventBus
        .subscribe(EventAccountBaned::class) { this.onEventAccountBaned(it) }
        .subscribe(EventAccountNoteChanged::class) { this.onEventAccountNoteChanged(it) }
        .subscribe(EventAccountStatusChanged::class) { this.onEventAccountStatusChanged(it) }

    private var loaded = false
    private var banDate = 0L
    private var note = ""
    private var status = t(API_TRANSLATE.app_loading)

    override fun bindView(view: View) {
        super.bindView(view)
        updateStatus()
        updateNote()
        updateDateBan()
    }

    private fun updateStatus() {
        val view = getView() ?: return
        val vStatus: ViewText = view.findViewById(R.id.vStatus)
        val vStatusChange: ViewIcon = view.findViewById(R.id.vStatusChange)
        val vContainer: LayoutCorned = view.findViewById(R.id.vContainer)

        vContainer.setBackgroundColor(if (ControllerSettings.isProfileListStyle) ToolsResources.getColorAttr(R.attr.colorSurface) else 0x00000000)

        val statusText = status.ifEmpty {
            if (xAccount.isCurrentAccount()) "{grey ${t(API_TRANSLATE.profile_tap_to_change_status)}}"
            else "{grey Hello, world}"
        }
        BonfireMarkdown.setMarkdownInline(vStatus, statusText)
        ControllerLinks.linkifyShort(vStatus)

        if (xAccount.isCurrentAccount()) {
            vStatusChange.visibility = View.VISIBLE
            vStatusChange.setOnClickListener {
                if (!loaded) {
                    ToolsToast.show(t(API_TRANSLATE.profile_loading_in_profess))
                } else {
                    changeStatus()
                }
            }
        } else {
            vStatusChange.visibility = View.GONE

            if (loaded && status.isNotEmpty() && ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_STATUS)) {
                vStatusChange.setImageResource(R.drawable.ic_clear_white_24dp)
                vStatusChange.visibility = View.VISIBLE
                vStatusChange.setOnClickListener { removeStatus() }
            }
        }
    }

    private fun updateNote() {
        val view = getView() ?: return
        val vNote: ViewText = view.findViewById(R.id.vNote)

        vNote.visibility = if (note.isEmpty()) GONE else VISIBLE
        BonfireMarkdown.setMarkdownInline(vNote, "${t(API_TRANSLATE.app_note)}: $note")
        ControllerLinks.linkifyShort(vNote)
    }

    private fun updateDateBan() {
        val view = getView() ?: return
        val vBanText: TextView = view.findViewById(R.id.vBanText)
        if (banDate > ControllerApi.currentTime()) {
            vBanText.text = t(API_TRANSLATE.error_account_baned, ToolsDate.dateToString(banDate))
            vBanText.visibility = VISIBLE
        } else {
            vBanText.visibility = GONE
        }
    }

    fun setInfo(status: String, note: String, banDate: Long) {
        this.loaded = true
        this.status = status
        this.note = note
        this.banDate = banDate
        update()
    }

    fun setNote(note: String) {
        this.note = note
        updateNote()
    }

    fun getNote() = if (!loaded) null else note

    private fun changeStatus() {
        if (!xAccount.can(API.LVL_CAN_CHANGE_STATUS)) {
            ToolsToast.show(t(API_TRANSLATE.error_low_lvl))
            return
        }
        SplashField()
            .setHint(t(API_TRANSLATE.app_status))
            .setAutoHideOnEnter(false)
            .setLinesCount(1)
            .setMax(API.ACCOUNT_STATUS_MAX_L)
            .setText(status)
            .setOnCancel(t(API_TRANSLATE.app_cancel))
            .setOnEnter(t(API_TRANSLATE.app_change)) { w, status ->
                ApiRequestsSupporter.executeEnabled(w, RAccountsStatusSet(status.trim())) {
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                    EventBus.post(EventAccountStatusChanged(xAccount.getId(), status.trim()))
                }
            }
            .apply {
                vFieldWidget.vField.addTextChangedListener(
                    BonfireMarkdown.getInlineEditorTextChangedListener(
                        vFieldWidget.vField
                    )
                )
            }
            .asSheetShow()
    }

    private fun removeStatus() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_remove_status))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsAdminStatusRemove(xAccount.getId(), comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventAccountStatusChanged(xAccount.getId(), ""))
                    }
                }
                .asSheetShow()
    }

    private fun onEventAccountBaned(e: EventAccountBaned) {
        if (e.accountId == xAccount.getId()) {
            banDate = e.date
            update()
        }
    }

    private fun onEventAccountNoteChanged(e: EventAccountNoteChanged) {
        if (e.accountId == xAccount.getId()) {
            note = e.note
            update()
        }
    }

    private fun onEventAccountStatusChanged(e: EventAccountStatusChanged) {
        if (e.accountId == xAccount.getId()) {
            status = e.status
            update()
        }
    }
}
