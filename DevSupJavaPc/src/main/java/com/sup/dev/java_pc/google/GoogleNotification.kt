package com.sup.dev.java_pc.google

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.*
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads
import java.util.concurrent.ConcurrentLinkedQueue

object GoogleNotification {
    private var onTokenNotFound: ((String) -> Unit)? = null

    sealed interface MessageTarget
    class TopicMessageTarget(val topic: String) : MessageTarget
    class TempTopicMessageTarget(val tokens: List<String>) : MessageTarget
    class LegacyTokensMessageTarget(val tokens: List<String>) : MessageTarget

    private class Message(val data: String, val target: MessageTarget)

    private lateinit var app: FirebaseApp
    private lateinit var messaging: FirebaseMessaging
    private val queue = ConcurrentLinkedQueue<Message>()

    fun init(app: FirebaseApp) {
        this.app = app
        this.messaging = FirebaseMessaging.getInstance(app)
        ToolsThreads.thread {
            while (true) {
                val message = queue.poll()
                if (message == null) {
                    ToolsThreads.sleep(1000)
                    continue
                }

                sendNow(message)
            }
        }
    }

    fun onTokenNotFound(onTokenNotFound: ((String) -> Unit)) {
        GoogleNotification.onTokenNotFound = onTokenNotFound
    }

    fun send(message: String, tokens: Array<String>) {
        queue.offer(Message(message, TempTopicMessageTarget(tokens.toList())))
    }

    fun sendLegacy(message: String, tokens: Array<String>) {
        queue.offer(Message(message, LegacyTokensMessageTarget(tokens.toList())))
    }

    fun sendTopic(message: String, topic: String) {
        queue.offer(Message(message, TopicMessageTarget(topic)))
    }

    fun subscribe(topic: String, tokens: List<String>) {
        messaging.subscribeToTopic(tokens, topic)
    }
    fun unsubscribe(topic: String, tokens: List<String>) {
        messaging.subscribeToTopic(tokens, topic)
    }

    private fun sendNow(message: Message) {
        when (message.target) {
            is LegacyTokensMessageTarget -> {
                val tokens = message.target.tokens
                if (tokens.isEmpty()) return

                val fbMessage = MulticastMessage.builder()
                    .putData("my_data", message.data)
                    .addAllTokens(tokens)
                    .build()

                val resp = messaging.sendEachForMulticast(fbMessage)

                resp.responses.forEachIndexed { idx, response ->
                    if (response.isSuccessful) return@forEachIndexed
                    when (response.exception.messagingErrorCode) {
                        MessagingErrorCode.UNREGISTERED -> onTokenNotFound?.invoke(tokens[idx])
                        else -> err("Error sending notification to (#$idx)${tokens[idx]}: " +
                                "${response.exception.message} (${response.exception.messagingErrorCode})")
                    }
                }
            }
            is TempTopicMessageTarget -> {
                val tokens = message.target.tokens
                if (tokens.isEmpty()) return

                val topic = "temp-${ToolsMath.randomInt(0, Int.MAX_VALUE / 2 - 1)}"
                subscribe(topic, tokens)
                sendNow(Message(message.data, TopicMessageTarget(topic)))
                unsubscribe(topic, tokens)
            }
            is TopicMessageTarget -> {
                val fbMessage = com.google.firebase.messaging.Message.builder()
                    .putData("my_data", message.data)
                    .setTopic(message.target.topic)
                    .build()

                messaging.send(fbMessage)
            }
        }
    }
}
