package com.sup.dev.android.tools

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.tools.ToolsMath

object ToolsNotifications {

    private val SPLITER = "-FS2ААА67миО-"
    var defChanelId = "chanel_0"

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "NOTIF_CANCEL") parseNotification(intent)
        }
    }

    enum class Importance {
        DEFAULT, HIGH, MIN
    }

    enum class GroupingType {
        SINGLE, GROUP
    }

    enum class IntentType(val index: Int) {
        CLICK(1), ACTION(2), CANCEL(3)
    }

    var notificationsListener: (Intent, IntentType, tag: String) -> Unit = { _, _, _ -> }
    private var chanels = ArrayList<Chanel>()
    private var notificationIdCounter = ToolsMath.randomInt(0, 5000000) //  Чтоб id были псевдоуникальными между запусками
    private var notificationManager: NotificationManager = SupAndroid.appContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        SupAndroid.appContext?.registerReceiver(broadcastReceiver, IntentFilter("NOTIF_CANCEL"))
    }

    fun instanceGroup(groupId: Int, name: Int) = instanceGroup(groupId, ToolsResources.s(name))

    fun instanceGroup(groupId: Int, name: String): String {
        val id = "group_$groupId"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannelGroup(NotificationChannelGroup(id, name))

        return id
    }

    fun instanceChanel(id: Int): Chanel {
        val chanel = Chanel(id)
        chanels.add(chanel)
        return chanel
    }

    fun cancelAll(){
        notificationManager.cancelAll()
    }

    fun parseNotification(intent: Intent): Boolean {
        val notificationId = intent.getIntExtra("ToolsNotification.notificationId", -1)
        val intentTypeIndex = intent.getIntExtra("ToolsNotification.intentType", -1)
        val notificationTag = intent.getStringExtra("ToolsNotification.notificationTag") ?: "null${SPLITER}null"
        val actionTag = intent.getStringExtra("ToolsNotification.actionTag")

        if (notificationId != -1 && ToolsStorage.getInt("ToolsNotification.notificationId", -1) != notificationId) {
            ToolsStorage.put("ToolsNotification.notificationId", notificationId)

            val intentType = when (intentTypeIndex) {
                IntentType.ACTION.index -> IntentType.ACTION
                IntentType.CANCEL.index -> IntentType.CANCEL
                else -> IntentType.CLICK
            }

            notificationManager.cancel(notificationId)
            notificationManager.cancel(notificationTag, notificationId)
            for (chanel in chanels) chanel.cancel(notificationTag)

            val tag = if (actionTag == null) notificationTag.split(SPLITER)[1] else actionTag
            notificationsListener.invoke(intent, intentType, tag)

            return true
        }

        return false
    }

    //
    //  Classes
    //

    class Chanel {

        private val idS: String
        private var name = ""
        private var description = ""
        private var groupId = ""
        private var sound = true
        private var vibration = true
        private var importance = Importance.DEFAULT
        private var groupingType = GroupingType.GROUP

        private var showedNotifications = HashList<String, Int>()


        constructor(id: Int) {
            this.idS = "chanel_$id"
        }

        fun getId() = idS

        fun post(icon: Int, title: String, text: String, intent: Intent, tag: String, intentCancel: Intent? = null) {
            val notification = NotificationX()
                    .setIcon(icon)
                    .setTitle(title)
                    .setText(text)
                    .setIntent(intent)
                    .setTag(tag)

            if (intentCancel != null) {
                notification.setIntentCancel(intentCancel)
            } else {
                if (intent.extras != null) notification.intentCancel.putExtras(intent.extras!!)
            }

            post(notification)
        }

        fun post(notification: NotificationX, onBuild: (NotificationCompat.Builder) -> Unit = {}) {

            if (groupingType == GroupingType.SINGLE) cancel(notification.tag)

            val builder = NotificationCompat.Builder(SupAndroid.appContext!!, idS)
                    .setSmallIcon(notification.icon)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentText(notification.text)

            if(notification.bigText != null) builder.setStyle(NotificationCompat.BigTextStyle().bigText(notification.bigText))

            if (notification.title != null) builder.setContentTitle(notification.title)
            if (sound && vibration) {
                builder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
            } else if (vibration) {
                builder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                builder.setSound(null)
            } else if (sound) {
                builder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                builder.setVibrate(LongArray(0))
            } else {
                builder.setDefaults(Notification.DEFAULT_LIGHTS)
                builder.setSound(null)
                builder.setVibrate(LongArray(0))
            }

            val notificationId = notificationIdCounter++
            val notificationTag = idS + SPLITER + notification.tag
            showedNotifications.add(notification.tag, notificationId)

            notification.intent.putExtra("ToolsNotification.notificationId", notificationId)
            notification.intent.putExtra("ToolsNotification.notificationTag", notificationTag)
            notification.intent.putExtra("ToolsNotification.intentType", IntentType.CLICK.index)
            notification.intentCancel.putExtra("ToolsNotification.notificationId", notificationId)
            notification.intentCancel.putExtra("ToolsNotification.notificationTag", notificationTag)
            notification.intentCancel.putExtra("ToolsNotification.intentType", IntentType.CANCEL.index)

            builder.setContentIntent(PendingIntent.getActivity(SupAndroid.appContext!!, ++notificationIdCounter, notification.intent, PendingIntent.FLAG_CANCEL_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else 0)))
            builder.setDeleteIntent(PendingIntent.getBroadcast(SupAndroid.appContext!!, ++notificationIdCounter, notification.intentCancel, PendingIntent.FLAG_CANCEL_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else 0)))

            for (a in notification.actions) {
                a.intent.putExtra("ToolsNotification.notificationId", notificationId)
                a.intent.putExtra("ToolsNotification.notificationTag", notificationTag)
                a.intent.putExtra("ToolsNotification.actionTag", a.tag)
                a.intent.putExtra("ToolsNotification.intentType", IntentType.ACTION.index)

                val action = NotificationCompat.Action(a.icon, a.text, PendingIntent.getActivity(SupAndroid.appContext, ++notificationIdCounter, a.intent, PendingIntent.FLAG_CANCEL_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE else 0)))
                builder.addAction(action)
            }

            onBuild.invoke(builder)

            notificationManager.notify(notificationTag, notificationId, builder.build())

        }

        fun init(): Chanel {
            init(sound)
            return this
        }

        fun cancel() {
            val keys = showedNotifications.getKeys()
            for (tag in keys) cancel(tag)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                for (n in notificationManager.activeNotifications) if (n.tag != null && n.tag.split(SPLITER)[0] == idS) notificationManager.cancel(n.tag, n.id)

        }

        fun cancel(tag: String) {
            val ids = showedNotifications.getAll(tag)
            for (id in ids) notificationManager.cancel(idS + SPLITER + tag, id)
            showedNotifications.remove(tag)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                for (n in notificationManager.activeNotifications) {
                    if (n.tag != null) {
                        val split = n.tag.split(SPLITER)
                        if (split.size > 1)
                            if (split[1] == tag) notificationManager.cancel(n.tag, n.id)
                    }
                }
        }

        fun cancelAllOrByTagIfNotEmpty(tag: String) {
            if (tag.isEmpty()) cancel()
            else cancel(tag)
        }

        private fun init(sound: Boolean) {

            if (name.isEmpty()) name = SupAndroid.TEXT_APP_NAME ?: ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val imp = when (importance) {
                    Importance.HIGH -> NotificationManager.IMPORTANCE_HIGH
                    Importance.MIN -> NotificationManager.IMPORTANCE_MIN
                    else -> NotificationManager.IMPORTANCE_DEFAULT
                }

                val channel = NotificationChannel(idS, name, imp)

                channel.enableVibration(true)
                if (groupId.isNotEmpty()) channel.group = groupId
                if (description.isNotEmpty()) channel.description = description
                if (!sound) channel.setSound(null, null)
                if (!vibration) channel.vibrationPattern = longArrayOf(0)

                notificationManager.createNotificationChannel(channel)
            }
        }

        //
        //  Setters
        //

        fun setName(name: Int): Chanel {
            return setName(ToolsResources.s(name))
        }

        fun setDescription(description: Int): Chanel {
            return setName(ToolsResources.s(description))
        }

        fun setDescription(description: String): Chanel {
            this.description = description
            return this
        }

        fun setName(name: String): Chanel {
            this.name = name
            return this
        }

        fun setSound(sound: Boolean): Chanel {
            this.sound = sound
            return this
        }

        fun setGroupId(groupId: String): Chanel {
            this.groupId = groupId
            return this
        }

        fun setVibration(vibration: Boolean): Chanel {
            this.vibration = vibration
            return this
        }

        fun setGroupingType(groupingType: GroupingType): Chanel {
            this.groupingType = groupingType
            return this
        }

        fun setImportance(importance: Importance): Chanel {
            this.importance = importance
            return this
        }


    }

    class NotificationX {

        @DrawableRes
        var icon = 0
        var title: String? = null
        var text: String? = null
        var bigText: String? = null
        var intent = Intent(SupAndroid.appContext, SupAndroid.activityClass)
        var intentCancel = Intent("NOTIF_CANCEL")
        var tag = "tag"
        var actions = ArrayList<ActionX>()

        fun setIcon(icon: Int): NotificationX {
            this.icon = icon
            return this
        }

        fun setTitle(title: String): NotificationX {
            this.title = title
            return this
        }

        fun setText(text: String): NotificationX {
            this.text = text
            return this
        }

        fun setBigText(bigText: String): NotificationX {
            this.bigText = bigText
            return this
        }

        fun setIntent(intent: Intent): NotificationX {
            this.intent = intent
            return this
        }

        fun setIntentCancel(intentCancel: Intent): NotificationX {
            this.intentCancel = intentCancel
            intentCancel.action = "NOTIF_CANCEL"
            return this
        }

        fun setTag(tag: String): NotificationX {
            this.tag = tag
            return this
        }

        fun addAction(action: ActionX): NotificationX {
            this.actions.add(action)
            return this
        }

        fun addExtra(key: String, value: String): NotificationX {
            intent.putExtra(key, value)
            return this
        }

    }

    class ActionX {

        var icon = 0
        var text = ""
        var tag = ""
        val intent: Intent = Intent(SupAndroid.appContext, SupAndroid.activityClass)

        fun setIcon(icon: Int): ActionX {
            this.icon = icon
            return this
        }

        fun setText(text: String): ActionX {
            this.text = text
            return this
        }

        fun setTag(tag: String): ActionX {
            this.tag = tag
            return this
        }

        fun addExtra(key: String, value: String): ActionX {
            intent.putExtra(key, value)
            return this
        }


    }
}

