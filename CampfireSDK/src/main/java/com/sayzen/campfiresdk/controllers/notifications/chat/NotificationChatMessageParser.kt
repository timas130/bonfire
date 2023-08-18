package com.sayzen.campfiresdk.controllers.notifications.chat

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.dzen.campfire.api.requests.chat.RChatRead
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.chat.EventChatRead
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsNotifications
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.text_format.TextFormatter

class MessageReplyReceiver : BroadcastReceiver() {
    companion object {
        const val KEY_TEXT_REPLY = "KEY_TEXT_REPLY"
        const val CHAT_TAG = "CHAT_TAG"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY) ?: return
        val chatTagS = intent.getStringExtra(CHAT_TAG) ?: return
        val chatTag = ChatTag(chatTagS)

        ApiRequestsSupporter.apply {
            init(api)
            execute(RChatMessageCreate(
                tag = chatTag, text = reply.toString(), imageArray = null, gif = null,
                voice = null, parentMessageId = 0, quoteMessageId = 0, stickerId = 0
            )) { response ->
                ControllerChats.incrementMessages(chatTag, response.message, true)

                NotificationChatMessageParser.sendNotification(
                    chatTag, ControllerNotifications.canSoundBySettings(NotificationChatMessage(
                        // canSoundBySettings doesn't actually care, but still
                        response.message, chatTag, true
                    ))
                )
            }.onApiError {
                ToolsToast.show(t(API_TRANSLATE.error_unknown))
            }
        }
    }
}

class MessageReadReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        val chatTag = ChatTag(intent.getStringExtra(MessageReplyReceiver.CHAT_TAG) ?: return)

        ApiRequestsSupporter.apply {
            init(api)
            execute(RChatRead(chatTag)) {
                EventBus.post(EventChatRead(chatTag))
            }.onApiError {
                ToolsToast.show(t(API_TRANSLATE.error_unknown))
            }
        }
    }
}

// taken from s/o: https://stackoverflow.com/questions/11932805#12089127
fun Bitmap.getRounded(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

fun NotificationChatMessageParser.Companion.sendNotification(tag: ChatTag, sound: Boolean = true) {
    val chatMessages = ControllerChats.getMessages(tag)
    if (chatMessages.isEmpty()) {
        clearNotification(tag)
        return
    }

    ApiRequestsSupporter.init(api)
    ControllerChats.getChat(tag) { chat ->
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ControllerNotifications.logoWhite
        else ControllerNotifications.logoColored

        val channel = if (sound) ControllerNotifications.chanelChatMessages
        else ControllerNotifications.chanelChatMessages_salient

        val me = Person.Builder().setName(t(API_TRANSLATE.app_you)).build()

        val builder = NotificationCompat.Builder(SupAndroid.appContext!!, channel.getId())
        builder.setSmallIcon(icon)
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.setContentIntent(PendingIntent.getActivity(
            SupAndroid.appContext!!, 2,
            Intent(SupAndroid.appContext!!, SupAndroid.activityClass!!).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("ToolsNotification.notificationId", tag.hashCode())
                putExtra("ToolsNotification.intentType", ToolsNotifications.IntentType.CLICK)
                putExtra(ControllerNotifications.EXTRA_NOTIFICATION, Json().apply {
                    NotificationChatMessage(
                        chatMessages.messages.last(), tag, true
                    ).json(true, this)
                }.toString())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else 0),
        ))
        builder.setStyle(NotificationCompat.MessagingStyle(me).also {
            it.isGroupConversation = tag.chatType != API.CHAT_TYPE_PRIVATE
            it.conversationTitle = chat.customName
            for (message in chatMessages.messages) {
                it.addMessage(NotificationCompat.MessagingStyle.Message(
                    TextFormatter(message.text).parseNoTags(),
                    message.dateCreate,
                    Person.Builder()
                        .setKey(message.creator.id.toString())
                        .setName(message.creator.name)
                        .setIcon(kotlin.run {
                            // TODO: Download avatars in the background
                            val avatar = ImageLoader.load(message.creator.imageId).startLoad()
                            if (avatar != null) IconCompat.createWithBitmap(
                                ToolsBitmap.decode(avatar)!!.getRounded()
                            ) else null
                        })
                        .build()
                ))
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val replyLabel = t(API_TRANSLATE.app_do_reply)
            val remoteInput = RemoteInput.Builder(MessageReplyReceiver.KEY_TEXT_REPLY)
                .setLabel(replyLabel).build()
            val replyPendingIntent = PendingIntent.getBroadcast(
                SupAndroid.appContext!!, tag.hashCode(),
                Intent(SupAndroid.appContext!!, MessageReplyReceiver::class.java).also {
                    it.putExtra(MessageReplyReceiver.CHAT_TAG, tag.asTag())
                },
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE else 0),
            )

            val action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_dialog_alert,
                t(API_TRANSLATE.app_do_reply),
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()
            builder.addAction(action)
        }

        builder.addAction(
            R.drawable.ic_mode_comment_white_24dp,
            t(API_TRANSLATE.chat_read_passive),
            PendingIntent.getBroadcast(
                SupAndroid.appContext!!, 0,
                Intent(SupAndroid.appContext!!, MessageReadReceiver::class.java).also {
                    it.putExtra(MessageReplyReceiver.CHAT_TAG, tag.asTag())
                },
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE else 0),
            )
        )

        with(NotificationManagerCompat.from(SupAndroid.appContext!!)) {
            Debug.info("NotificationChatMessage", "sending $tag (${tag.hashCode()})")
            this.notify(tag.hashCode(), builder.build())
        }
    }
}

fun NotificationChatMessageParser.Companion.clearNotification(tag: ChatTag) {
    val manager = NotificationManagerCompat.from(SupAndroid.appContext!!)
    Debug.info("NotificationChatMessage", "clearing $tag (${tag.hashCode()})")
    manager.cancel(tag.hashCode())
}

class NotificationChatMessageParser(override val n: NotificationChatMessage) : ControllerNotifications.Parser(n) {
    companion object;

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        sendNotification(n.tag, sound)
    }

    override fun asString(html: Boolean): String {
        return if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_sticker)
            else n.publicationChatMessage.creator.name + ": " + n.publicationChatMessage.text
        } else {
            if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) t(API_TRANSLATE.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) t(API_TRANSLATE.app_sticker)
            else n.publicationChatMessage.text
        }
    }

    override fun canShow() = canShowNotificationChatMessage(n)

    override fun doAction() {
        doActionNotificationChatMessage(n)
    }

    private fun canShowNotificationChatMessage(n: NotificationChatMessage): Boolean {
        if (!n.subscribed) return false
        if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT && !ControllerSettings.notificationsChatMessages) return false
        if (n.tag.chatType == API.CHAT_TYPE_PRIVATE && !ControllerSettings.notificationsPM) return false
        return if (SupAndroid.activityIsVisible && Navigator.getCurrent() is SChat) {
            val screen = Navigator.getCurrent() as SChat
            screen.chat.tag != n.tag
        } else {
            true
        }
    }

    private fun doActionNotificationChatMessage(n: NotificationChatMessage) {
        SChat.instance(ChatTag(n.publicationChatMessage.chatType, n.publicationChatMessage.fandom.id, n.publicationChatMessage.fandom.languageId), n.publicationChatMessage.id, true, Navigator.TO)
    }
}
