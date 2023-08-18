package com.sayzen.campfiresdk.models.splashs

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.comments.RCommentsChange
import com.dzen.campfire.api.requests.comments.RCommentsCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.AttacheAgent
import com.sayzen.campfiresdk.models.events.publications.EventCommentAdd
import com.sayzen.campfiresdk.models.events.publications.EventCommentChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationCommentWatchChange
import com.sayzen.campfiresdk.models.support.Attach
import com.sayzen.campfiresdk.screens.other.rules.SGoogleRules
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewEditText
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

class SplashComment constructor(
        private val publicationId: Long,
        private val answer: PublicationComment?,
        private val changeComment: PublicationComment?,
        private var quoteId: Long,
        private var quoteText: String,
        private var showToast: Boolean,
        private val onCreated: ((PublicationComment) -> Unit)?
) : Splash(R.layout.splash_comment_input), AttacheAgent {

    private val vSend: ViewIcon = findViewById(R.id.vSend)
    private val vAttach: ViewIcon = findViewById(R.id.vAttach)
    private val vAttachRecycler: RecyclerView = findViewById(R.id.vAttachRecycler)
    private val vText: ViewEditText = findViewById(R.id.vText)
    private val vQuoteText: ViewText = findViewById(R.id.vQuoteText)
    private val vFieldContainer: ViewGroup = findViewById(R.id.vFieldContainer)
    private val vSendContainer: ViewGroup = findViewById(R.id.vSendContainer)

    private var attach = Attach(vAttach, vAttachRecycler,
            { updateSendEnabled() },
            {
                ToolsThreads.main(100) {
                    if (isHided()) asSheetShow()
                }
            }, // Нужна задержка, иначе откроется и сразу закроется из-за смены экранов
            { sendSticker(it) })

    constructor(changeComment: PublicationComment, showToast: Boolean) : this(0, null, changeComment, 0, "", showToast, null)

    constructor(publicationId: Long, showToast: Boolean, onCreated: (PublicationComment) -> Unit) : this(publicationId, null, null, 0, "", showToast, onCreated)

    constructor(publicationId: Long, answer: PublicationComment?, showToast: Boolean, onCreated: (PublicationComment) -> Unit) : this(publicationId, answer, null, 0, "", showToast, onCreated)

    init {

        ControllerMention.startFor(vText)

        vText.hint = t(API_TRANSLATE.app_message)

        if (changeComment != null) {
            vText.setText(changeComment.text)
            vText.setSelection(vText.text!!.length)
            quoteText = changeComment.quoteText
            quoteId = changeComment.quoteId
        } else if (answer != null && quoteId == 0L) {
            vText.setText(answer.creator.name + ", ")
            vText.setSelection(vText.text!!.length)
        }

        vQuoteText.visibility = if (quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = quoteText
        ControllerApi.makeTextHtml(vQuoteText)

        if (changeComment == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        vText.addTextChangedListener(TextWatcherChanged { updateSendEnabled() })

        vSend.setOnClickListener { onSendClicked() }
        vSend.setImageResource(if (changeComment == null) R.drawable.ic_send_white_24dp else R.drawable.ic_done_white_24dp)
        vAttach.visibility = if (changeComment == null) View.VISIBLE else View.GONE


        vFieldContainer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (vFieldContainer.height >= ToolsView.dpToPx(130)) {
                if (vFieldContainer.indexOfChild(vAttach) != -1) {
                    vSendContainer.addView(ToolsView.removeFromParent(vAttach), 0)
                    vText.requestLayout()
                }
            } else if (vFieldContainer.height <= ToolsView.dpToPx(100)) {
                if (vSendContainer.indexOfChild(vAttach) != -1) {
                    vFieldContainer.addView(ToolsView.removeFromParent(vAttach), 0)
                    vText.requestLayout()
                }
            }
        }

        updateSendEnabled()
    }

    private fun updateSendEnabled() {
        vSend.isEnabled = (vText.isEnabled && vText.text!!.isNotEmpty() && vText.text!!.length < API.COMMENT_MAX_L) || attach.isHasContent()
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vText)
    }

    override fun onHide() {
        super.onHide()
        ControllerMention.hide()
    }

    override fun setEnabled(enabled: Boolean): Splash {
        attach.setEnabled(enabled)
        vText.isEnabled = enabled
        vQuoteText.isEnabled = enabled
        updateSendEnabled()
        return super.setEnabled(enabled)
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

    override fun onBackPressed(): Boolean {
        onCancel()
        return true
    }

    private fun onCancel() {
        if (attach.isHasContent()
                || (getText().isNotEmpty() && answer != null && getText() != answer.creator.name + ",")
                || (changeComment != null && getText() != changeComment.text.trim())
                || (answer == null && changeComment == null && getText().isNotEmpty())
        ) {
            SplashAlert()
                    .setText(t(API_TRANSLATE.comments_cancel_confirm))
                    .setOnCancel(t(API_TRANSLATE.app_do_cancel))
                    .setOnEnter(t(API_TRANSLATE.app_close)) {
                        hide()
                    }
                    .asSheetShow()
        } else {
            hide()
        }
    }


    private fun afterSend(comment: PublicationComment) {
        if (showToast) ToolsToast.show(t(API_TRANSLATE.app_published))
        onCreated?.invoke(comment)
        EventBus.post(EventCommentAdd(publicationId, comment))
        if (ControllerSettings.watchPost) EventBus.post(EventPublicationCommentWatchChange(publicationId, true))
        hide()
        ControllerStoryQuest.incrQuest(API.QUEST_STORY_COMMENTS)
    }

    private fun getText() = vText.text!!.toString().trim { it <= ' ' }

    private fun getParentId(): Long {
        if (quoteId != 0L) return quoteId
        if (answer != null && getText().startsWith(answer.creator.name + ", ")) return answer.id
        return 0L
    }

    private fun onSendClicked() {
        if (isHided() || !isEnabled) return
        val text = getText()
        val parentId = getParentId()

        if (text.isEmpty() && !attach.isHasContent()) return
        if (text.length > API.COMMENT_MAX_L) {
            ToolsToast.show(t(API_TRANSLATE.error_too_long_text))
            return
        }

        if (changeComment == null) {
            if (attach.isHasContent()) sendImage(text, parentId)
            else if (ToolsText.isWebLink(text)) sendLink(text, parentId, true)
            else sendText(text, parentId)
        } else sendChange(text)
    }


    //
    //  Text
    //


    private fun sendText(text: String, parentId: Long) {
        SGoogleRules.acceptRulesDialog() {
            ApiRequestsSupporter.executeEnabled(this, RCommentsCreate(publicationId, text, null, null, parentId, ControllerSettings.watchPost, quoteId, 0)) { r ->
                afterSend(r.comment)
            }
                    .onApiError(RCommentsCreate.E_BAD_PUBLICATION_STATUS) { ToolsToast.show(t(API_TRANSLATE.error_gone)) }
                    .onApiError(RCommentsCreate.E_PARENT_COMMENT_DONT_EXIST) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone_parent)) }
        }

    }

    private fun sendChange(text: String) {
        SGoogleRules.acceptRulesDialog() {
            ApiRequestsSupporter.executeEnabled(
                    this,
                    RCommentsChange(changeComment!!.id, text, quoteId)
            ) {
                ToolsToast.show(t(API_TRANSLATE.app_changed))
                EventBus.post(EventCommentChange(changeComment.id, text, quoteId, quoteText))
            }
        }
    }

    //
    //  Link
    //

    private fun sendLink(text: String, parentId: Long, send: Boolean) {
        SGoogleRules.acceptRulesDialog() {
            val dialog = ToolsView.showProgressDialog()
            ToolsNetwork.getBytesFromURL(text, 10) { bytes ->
                if (bytes == null || !ToolsBytes.isImage(bytes)) {
                    dialog.hide()
                    if (send) sendText(text, parentId)
                    else vText.setText(text)
                } else {
                    attach.attachUrl(text, dialog) {
                        if (send) sendText(text, parentId)
                        else vText.setText(text)
                    }
                }

            }
        }
    }

    private fun sendImage(text: String, parentId: Long) {
        SGoogleRules.acceptRulesDialog() {
            setEnabled(false)

            ToolsThreads.thread {
                val bytes = attach.getBytes()
                val gif = if (bytes.size == 1 && ToolsBytes.isGif(bytes[0])) bytes[0] else null
                if (gif != null) {
                    val bt = ToolsBitmap.decode(bytes[0])
                    if (bt == null) {
                        setEnabled(true)
                        ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                        return@thread
                    }
                    val byt = ToolsBitmap.toBytes(bt, API.CHAT_MESSAGE_IMAGE_WEIGHT)
                    if (byt == null) {
                        setEnabled(true)
                        ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                        return@thread
                    }
                    bytes[0] = byt
                }
                ToolsThreads.main {
                    ApiRequestsSupporter.executeProgressDialog(
                            RCommentsCreate(
                                    publicationId,
                                    text,
                                    bytes,
                                    gif,
                                    parentId,
                                    ControllerSettings.watchPost,
                                    quoteId,
                                    0
                            )) { r -> afterSend(r.comment) }
                            .onApiError(RCommentsCreate.E_BAD_PUBLICATION_STATUS) { ToolsToast.show(t(API_TRANSLATE.error_gone)) }
                            .onApiError(RCommentsCreate.E_PARENT_COMMENT_DONT_EXIST) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone_parent)) }
                            .onFinish { setEnabled(true) }
                }
            }

        }
    }

    private fun sendSticker(sticker: PublicationSticker) {

        val text = getText()
        if (text.length > API.COMMENT_MAX_L) {
            ToolsToast.show(t(API_TRANSLATE.error_too_long_text))
            return
        }

        SGoogleRules.acceptRulesDialog() {
            setEnabled(false)

            ApiRequestsSupporter.executeProgressDialog(
                    RCommentsCreate(
                            publicationId,
                            text,
                            null,
                            null,
                            answer?.id ?: 0,
                            ControllerSettings.watchPost,
                            quoteId,
                            sticker.id
                    )
            ) { r -> afterSend(r.comment) }
                    .onApiError(RCommentsCreate.E_BAD_PUBLICATION_STATUS) { ToolsToast.show(t(API_TRANSLATE.error_gone)) }
                    .onApiError(RCommentsCreate.E_PARENT_COMMENT_DONT_EXIST) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone_parent)) }
                    .onFinish { setEnabled(true) }
        }
    }

    //
    //  Attache
    //

    override fun attacheText(text: String, postAfterAdd: Boolean) {
        vText.setText(vText.text.toString() + text)
    }

    override fun attacheImage(image: Uri, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        ToolsBitmap.getFromUri(image, {
            if (it == null) {
                dialog.hide()
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                return@getFromUri
            }

            attach.setImageBitmapNow(it, dialog)
        }, {
            dialog.hide()
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
        })
    }

    override fun attacheImage(image: Bitmap, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            try {
                attach.attachBytes_inWorker(ToolsBitmap.toPNGBytes(image), dialog)
            }catch (e:Exception){
                ToolsToast.show(t(API_TRANSLATE.error_unknown))
                dialog.hide()
            }
        }
    }

    override fun attacheAgentIsActive() = isShoved()

}
