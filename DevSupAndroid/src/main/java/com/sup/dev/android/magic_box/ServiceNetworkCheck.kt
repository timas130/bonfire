package com.sup.dev.android.magic_box

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.eventbus_multi_process.EventBusMultiProcess
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsNotifications
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads
import java.io.IOException
import java.io.Serializable
import java.lang.RuntimeException
import java.net.InetSocketAddress
import java.net.Socket


/*
    Грубое решение проблемы проверки доступа в интернет через DOZE мод.
    * DOZE не ограничевает фореграунд сервисы, но они должны ыть запушпущены в отдельно процесе.

    1. Проверяет доступ в интернет в рабочем потоке
    2. Если его нет, проверяет еще раз подождав 5 секунд
    3. Если его нет, но он был во время прошлой проверки, то проверяет фореграунд сервисе запущеном в отдельном процессе (обход DOZE)

        <service
            android:name="com.sup.dev.android.magic_box.ServiceNetworkCheck"
            android:process=":networkCheckProcess"/>

 */

class ServiceNetworkCheck : Service() {


    //
    //  Service
    //

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val key = intent.getLongExtra(P_KEY, -1)
        startForeground(SupAndroid.SERVICE_NETWORK_CHECK, instanceNotification())
        isHasInternetConnection { has ->
            EventBusMultiProcess.post(EventServiceNetworkCheck(key, has))
            stopSelf()
        }
        return Service.START_STICKY
    }

    private fun instanceNotification(): Notification {
        return NotificationCompat.Builder(SupAndroid.appContext!!, NOTIFICATION_SALIENT_CHANEL_ID)
                .setSmallIcon(NOTIFICATION_ICON)
                .setAutoCancel(false)
                .setTicker(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_TEXT)
                .setContentTitle(NOTIFICATION_TITLE)
                .setSound(null)
                .setVibrate(longArrayOf(0L))
                .setOngoing(true)
                .build()
    }

    //
    //  Event
    //

    class EventServiceNetworkCheck(internal var key: Long, internal var result: Boolean) : Serializable

    companion object {

        private val P_KEY = "P_KEY"
        private var globalKey: Long = 0
        val SERVICE_NETWORK_CHECK_LAST_RESULT = "SERVICE_NETWORK_CHECK_LAST_RESULT"
        var NOTIFICATION_ICON: Int = 0
        var NOTIFICATION_TITLE: String? = null
        var NOTIFICATION_TEXT: String? = null
        var NOTIFICATION_SALIENT_CHANEL_ID = ""

        //
        //  Static
        //

        private fun onCheckFinish(onResult: (Boolean) -> Unit, has: Boolean) {
            ToolsStorage.put(SERVICE_NETWORK_CHECK_LAST_RESULT, has)
            onResult.invoke(has)
        }

        @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
        fun check(onResult: (Boolean) -> Unit) {
            isHasInternetConnection { has ->
                if ((!has) && ToolsStorage.getBoolean(SERVICE_NETWORK_CHECK_LAST_RESULT, true))
                    isHasInternetConnectionInService { has2 -> onCheckFinish(onResult, has2) }
                else
                    onCheckFinish(onResult, has)
            }
        }

        fun isHasInternetConnectionInService(onResult: (Boolean) -> Unit) {
            val key = ++globalKey
            val time = System.currentTimeMillis()
            EventBus.subscribeHard(EventServiceNetworkCheck::class
            ) { e ->
                if (e.key == key && time + 15000 > System.currentTimeMillis()) onResult.invoke(e.result)
                Unit
            }
            ToolsIntent.startServiceForeground(ServiceNetworkCheck::class.java,
                    P_KEY, key)
        }

        @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
        fun isHasInternetConnection(onResult: (Boolean) -> Unit) {
            ToolsThreads.thread {
                val has = Item(isHasInternetConnectionNow)
                if (!has.a) {
                    ToolsThreads.sleep(5000)
                    has.a = isHasInternetConnectionNow
                }
                ToolsThreads.main {
                    onResult.invoke(has.a)
                    Unit
                }
                Unit
            }
        }

        val isHasInternetConnectionNow: Boolean
            @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
            @WorkerThread
            get() {
                var sock: Socket? = null
                try {
                    sock = Socket()
                    sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                    sock.close()
                    return true
                } catch (e: IOException) {
                    return false
                } finally {
                    if (sock != null) {
                        try {
                            sock.close()
                        } catch (e: IOException) {
                            err(e)
                        }

                    }
                }
            }
    }

}
