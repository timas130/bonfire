package com.sayzen.devsupandroidgoogle

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsThreads

class GoogleNotifications : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        ToolsThreads.main { onReceive(remoteMessage) }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        if (onToken != null) onToken!!.invoke(s)
    }

    companion object {

        private var onToken: ((String?) -> Unit)? = null
        private var onReceive: ((RemoteMessage) -> Unit)? = null

        fun init(onToken: ((String?) -> Unit)?, onReceive: ((RemoteMessage) -> Unit)?) {
            GoogleNotifications.onToken = onToken
            GoogleNotifications.onReceive = onReceive

            val task = FirebaseMessaging.getInstance().token
            if(task.isComplete) {
                val fastToken = task.result
                info("XPush", "Fcm Token [$fastToken]")
                onToken?.invoke(fastToken)
            }
            else {
                task.addOnSuccessListener { instanceIdResult ->
                    info("XPush", "Fcm Token [${instanceIdResult}]")
                    onToken?.invoke(instanceIdResult)
                }
            }
        }

        internal fun onReceive(remoteMessage: RemoteMessage) {
            info("XPush", "Fcm onReceive")
            onReceive!!.invoke(remoteMessage)
        }
    }

}