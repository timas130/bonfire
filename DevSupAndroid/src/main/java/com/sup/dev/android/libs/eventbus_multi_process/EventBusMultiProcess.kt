package com.sup.dev.android.libs.eventbus_multi_process

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import java.io.Serializable

class EventBusMultiProcess : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val event = intent.getSerializableExtra(MULTI_PROCESS_INTENT_EXTRA)
            EventBus.post(event as Any)
        } catch (ex: Exception) {
            err(ex)
        }

    }

    companion object {

        private val MULTI_PROCESS_INTENT_ACTION = "multi_process_event"
        private val MULTI_PROCESS_INTENT_EXTRA = "event"

        fun init() {

            EventBus.setPostMultiProcessCallback { post(it) }

            if (!isRegisteredInManifest()) {
                val filter = IntentFilter(MULTI_PROCESS_INTENT_ACTION)
                filter.addCategory(Intent.CATEGORY_DEFAULT)
                SupAndroid.appContext!!.registerReceiver(EventBusMultiProcess(), filter)
            }
        }

        private val baseIntent: Intent
            get() {
                val intent = Intent()
                intent.action = MULTI_PROCESS_INTENT_ACTION
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                return intent
            }

        fun post(event: Serializable) {
            val broadcastIntent = baseIntent
            broadcastIntent.putExtra(MULTI_PROCESS_INTENT_EXTRA, event)
            SupAndroid.appContext!!.sendBroadcast(broadcastIntent)
        }

        private fun isRegisteredInManifest(): Boolean {
            val intent = baseIntent
            val process = ToolsAndroid.getProcessName()
            return process != null && ToolsAndroid.hasBroadcastReceiver(process, intent, SupAndroid.appContext!!)
        }
    }


}