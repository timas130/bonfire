package com.sayzen.campfiresdk.screens.chat

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.chat.RChatMessageChange
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.dzen.campfire.api.requests.chat.RChatTyping
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.support.Attach
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.android.views.views.ViewVoiceRecord
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewEditText
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class FieldLogic(
        val screen: SChat
) {

    val eventBus = EventBus.subscribe(EventPublicationRemove::class){
        if(it.publicationId == publicationChange?.id) setChange(null)
        if(it.publicationId == publicationAnswer?.id) clearAnswer()
    }

    val vSend: ViewIcon = screen.findViewById(R.id.vSend)
    val vAttach: ViewIcon = screen.findViewById(R.id.vAttach)
    val vAttachRecycler: RecyclerView = screen.findViewById(R.id.vAttachRecycler)
    val vText: ViewEditText = screen.findViewById(R.id.vText)
    val vQuoteContainer: ViewGroup = screen.findViewById(R.id.vQuoteContainer)
    val vQuoteText: ViewText = screen.findViewById(R.id.vQuoteText)
    val vQuoteRemove: ViewIcon = screen.findViewById(R.id.vQuoteRemove)
    val vVoiceRecorder: ViewVoiceRecord = screen.findViewById(R.id.vVoiceRecorder)
    val vFieldContainer: ViewGroup = screen.findViewById(R.id.vFieldContainer)
    val vVoiceContainer: ViewGroup = screen.findViewById(R.id.vVoiceContainer)
    val vVoicePlay: ViewIcon = screen.findViewById(R.id.vVoicePlay)
    val vVoiceRemove: ViewIcon = screen.findViewById(R.id.vVoiceRemove)
    val vVoiceLabel: TextView = screen.findViewById(R.id.vVoiceLabel)
    val vSendContainer: ViewGroup = screen.findViewById(R.id.vSendContainer)

    val attach = Attach(vAttach, vAttachRecycler, { updateAction() }, {}, { sendSticker(it) })
    private val utilsAudioPlayer = UtilsAudioPlayer()

    private var lastTypingSent = 0L
    private var isRecording = false
    private var publicationAnswer: PublicationChatMessage? = null
    var publicationChange: PublicationChatMessage? = null
    private var quoteText = ""
    private var quoteId = 0L
    private var voiceBytes: ByteArray? = null

    init {
        vVoiceContainer.visibility = View.GONE
        vQuoteContainer.visibility = View.GONE
        vQuoteRemove.setOnClickListener { setQuote("") }
        vSend.setOnClickListener { onSendClicked() }
        vText.setHint(t(API_TRANSLATE.app_message))
        vText.addTextChangedListener(TextWatcherChanged { onTextChanged() })

        ControllerMention.startFor(vText)

        vVoiceRecorder.maxRecordingTimeMs = API.CHAT_MESSAGE_VOICE_MAX_MS
        vVoiceRecorder.onRecordingProgress = { vVoiceLabel.text = ToolsText.toTime(it) }
        vVoiceRecorder.onRecordingStart = {
            stopMyVoice()
            vVoiceLabel.text = ToolsText.toTime(0)
            isRecording = true
            updateAction()
            if (ControllerSettings.voiceMessagesAutoLock) vVoiceRecorder.lock()
        }
        vVoiceRecorder.onRecordingStop = {
            voiceBytes = it
            isRecording = false
            updateAction()
            if (voiceBytes != null && ControllerSettings.voiceMessagesAutoSend) sendVoice()
        }

        vVoiceRemove.setOnClickListener {
            stopMyVoice()
            voiceBytes = null
            updateAction()
        }
        vVoicePlay.setOnClickListener {
            if (utilsAudioPlayer.isPlaying()) stopMyVoice()
            else startMyVoice()
        }

        vFieldContainer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(vFieldContainer.height >= ToolsView.dpToPx(130)){
                if(vFieldContainer.indexOfChild(vAttach) != -1){
                    vSendContainer.addView(ToolsView.removeFromParent(vAttach), 0)
                    vText.requestLayout()
                }
            } else if(vFieldContainer.height <= ToolsView.dpToPx(100)){
                if(vSendContainer.indexOfChild(vAttach) != -1){
                    vFieldContainer.addView(ToolsView.removeFromParent(vAttach), 0)
                    vText.requestLayout()
                }
            }
        }

        updateMedieEditText()
        onTextChanged()

    }

    fun clearAnswer(){
        publicationAnswer = null
        setQuote("", 0)
    }

    private fun stopMyVoice() {
        utilsAudioPlayer.stop()
        vVoicePlay.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_play_arrow_white_24dp))
    }

    private fun startMyVoice() {
        utilsAudioPlayer.play(voiceBytes!!) {
            vVoicePlay.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_play_arrow_white_24dp))
        }
        vVoicePlay.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_pause_white_24dp))
    }

    fun setQuote(publication: PublicationChatMessage) {
        var text = publication.creator.name + ": "
        if (publication.text.isNotEmpty()) text += publication.text
        else if (publication.resourceId != 0L || publication.imageIdArray.isNotEmpty()) text += t(API_TRANSLATE.app_image)
        else if (publication.stickerId != 0L) text += t(API_TRANSLATE.app_sticker)
        setQuote(text, publication.id)
    }

    fun setQuote(quoteText: String, quoteId: Long = 0) {
        if(publicationChange != null && quoteId == publicationChange!!.id){
            ToolsToast.show(t(API_TRANSLATE.chat_error_quote_same_message))
            return
        }
        this.quoteText = quoteText
        if (this.quoteText.length > API.CHAT_MESSAGE_QUOTE_MAX_SIZE) this.quoteText = this.quoteText.substring(0, API.CHAT_MESSAGE_QUOTE_MAX_SIZE) + "..."
        this.quoteId = quoteId
        vQuoteContainer.visibility = if (this.quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = this.quoteText
        ControllerApi.makeTextHtml(vQuoteText)
        updateAction()
    }

    fun setAnswer(publicationAnswer: PublicationChatMessage, withName: Boolean): Boolean {
        setChange(null)
        if (ControllerApi.isCurrentAccount(publicationAnswer.creator.id)) return false
        var text = vText.text!!.toString()
        if (this.publicationAnswer != null && text.startsWith(this.publicationAnswer!!.creator.name + ", ")) {
            text = text.substring((this.publicationAnswer!!.creator.name + ", ").length)
        }
        this.publicationAnswer = publicationAnswer
        if (withName) vText.setText(publicationAnswer.creator.name + ", " + text)
        vText.setSelection(vText.text!!.length)
        ToolsView.showKeyboard(vText)
        return true
    }

    private fun onTextChanged() {
        sendTyping()
        updateAction()
    }

    private fun updateAction() {
        vSend.visibility = if (vText.text.toString().isNotEmpty() || attach.isHasContent() || quoteId != 0L || publicationChange != null || voiceBytes != null) View.VISIBLE else View.GONE
        vVoiceRecorder.visibility = if (vText.text.toString().isNotEmpty() || attach.isHasContent() || quoteId != 0L || publicationChange != null || voiceBytes != null) View.GONE else View.VISIBLE

        if (isRecording || voiceBytes != null) vFieldContainer.visibility = View.GONE
        else vFieldContainer.visibility = View.VISIBLE

        vVoiceContainer.visibility = if (vFieldContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        vVoicePlay.visibility = if (voiceBytes == null) View.INVISIBLE else View.VISIBLE
        vVoiceRemove.visibility = if (voiceBytes == null) View.INVISIBLE else View.VISIBLE
    }

    //
    //  Send
    //

    private fun getText() = vText.text!!.toString().trim { it <= ' ' }

    private fun getParentId(): Long {
        if (quoteId != 0L) return quoteId
        if (publicationAnswer != null && getText().startsWith(publicationAnswer!!.creator.name + ",")) return publicationAnswer!!.id
        return 0L

    }

    private fun onSendClicked() {

        if (voiceBytes != null) {
            sendVoice()
            return
        }

        val text = getText()
        val parentId = getParentId()

        if (text.isEmpty() && !attach.isHasContent()) return

        if(text.length > API.CHAT_MESSAGE_TEXT_MAX_L){
            ToolsToast.show(t(API_TRANSLATE.error_too_long_text))
            return
        }

        if (publicationChange == null) {
            if (attach.isHasContent()) sendImage(text, parentId)
            else if (ToolsText.isWebLink(text)) sendLink(text, parentId, true)
            else sendText(text, parentId)
        } else sendChange(text)
    }

    private fun beforeSend() {
        voiceBytes = null
        setQuote("")
        attach.clear()
        setChange(null)
        setText("")
        updateAction()
    }

    fun setText(text: String) {
        vText.setText(text)
    }

    fun setChange(publicationChange: PublicationChatMessage?) {
        if (this.publicationChange != null && publicationChange == null) vText.setText(null)
        this.publicationChange = publicationChange

        updateMedieEditText()

        vSend.setImageResource(if (publicationChange == null) R.drawable.ic_send_white_24dp else R.drawable.ic_done_white_24dp)
        vAttach.visibility = if (publicationChange == null) View.VISIBLE else View.GONE
        if (publicationChange != null) {
            vText.setText(publicationChange.text)
            vText.setSelection(vText.text!!.length)
            ToolsThreads.main(true) { ToolsView.showKeyboard(vText) }
            setQuote(publicationChange.quoteText, publicationChange.quoteId)
        }
    }

    fun updateMedieEditText() {
        if (publicationChange == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        else vText.setCallback(null)
    }

    fun sendVoice() {
        val quoteIdV = quoteId
        val voiceBytes = this.voiceBytes
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.chat.tag, "", null, null, voiceBytes, 0L, quoteIdV, 0)))
    }

    private fun sendText(text: String, parentId: Long) {
        val quoteIdV = quoteId
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.chat.tag, text, null, null, null, parentId, quoteIdV, 0)))
    }

    private fun sendChange(text: String) {
        val quoteIdV = quoteId
        val quoteTextV = quoteText
        val publicationChangeId = publicationChange!!.id
        beforeSend()
        ToolsToast.show(t(API_TRANSLATE.app_changed))
        EventBus.post(EventChatMessageChanged(publicationChangeId, text, quoteIdV, quoteTextV))
        ApiRequestsSupporter.execute(RChatMessageChange(publicationChangeId, quoteIdV, text)) {
            EventBus.post(EventChatMessageChanged(publicationChangeId, it.message.text, quoteIdV, quoteTextV))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(t(API_TRANSLATE.error_chat_access)) }

    }

    private fun sendLink(text: String, parentId: Long, send: Boolean) {
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

    private fun sendImage(text: String, parentId: Long) {
        ToolsThreads.thread {
            val bytes = attach.getBytes()
            val gif = if (bytes.size == 1 && ToolsBytes.isGif(bytes[0])) bytes[0] else null
            if (gif != null) {
                val bt = ToolsBitmap.decode(bytes[0])
                if (bt == null) {
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@thread
                }
                val byt = ToolsBitmap.toBytes(bt, API.CHAT_MESSAGE_IMAGE_WEIGHT)
                if (byt == null) {
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@thread
                }
                bytes[0] = byt
            }

            var textV = text
            if(parentId > 0 && publicationAnswer != null) if(getText().trim() == publicationAnswer!!.creator.name+",") textV = ""

            ToolsThreads.main {
                val quoteIdV = quoteId
                beforeSend()
                screen.addCard(CardSending(screen, RChatMessageCreate(screen.chat.tag, textV, bytes, gif, null, parentId, quoteIdV, 0)))
            }
        }
    }

    private fun sendSticker(sticker: PublicationSticker) {
        val quoteIdV = quoteId
        val parentId = getParentId()
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.chat.tag, "", null, null, null, parentId, quoteIdV, sticker.id)))
    }

    private fun sendTyping() {
        if (lastTypingSent > System.currentTimeMillis() - 5000) return
        val t = vText.text
        if (t == null || t.isEmpty()) return
        lastTypingSent = System.currentTimeMillis()
        RChatTyping(screen.chat.tag).send(api)
    }


}