package com.sup.dev.android.tools

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.media.session.MediaButtonReceiver
import com.sup.dev.android.app.SupAndroid.appContext
import com.sup.dev.android.models.EventBluetoothScoStateChanged
import com.sup.dev.android.models.EventMediaButtonPress
import com.sup.dev.java.libs.debug.Debug.err
import com.sup.dev.java.libs.debug.Debug.info
import com.sup.dev.java.libs.eventBus.EventBus.post

object ToolsMediaButton {
    private const val TAG = "ToolsMediaButton"
    private var mediaSession: MediaSessionCompat? = null
    private var bluetoothScoRegistered = false
    private var lastHeadsetMicState = 0
    private var lockHeadsetMicEvent = false
    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun start(context: Context) {
        stop()
        mediaSession = MediaSessionCompat(context, "MyPlayerService")
        mediaSession!!.setCallback(mediaSessionCallback)
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, context, MediaButtonReceiver::class.java)
        mediaSession!!.setMediaButtonReceiver(PendingIntent.getBroadcast(context, 0, mediaButtonIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_MUTABLE else 0))
        mediaSession!!.isActive = true
    }

    fun stop() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }

    fun isStarted() = mediaSession != null

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val extras = mediaButtonEvent.extras
            info(TAG, "onMediaButtonEvent(): $extras")
            if (extras != null) {
                for (k in extras.keySet()) {
                    info(TAG, "key[" + k + "] value[" + extras[k] + "]")
                }
                if (extras.containsKey("android.intent.extra.KEY_EVENT")
                        && extras["android.intent.extra.KEY_EVENT"].toString().contains("action=ACTION_DOWN")
                        && (extras["android.intent.extra.KEY_EVENT"].toString().contains("keyCode=KEYCODE_MEDIA_PAUSE")
                                || extras["android.intent.extra.KEY_EVENT"].toString().contains("keyCode=KEYCODE_MEDIA_PLAY")
                                || extras["android.intent.extra.KEY_EVENT"].toString().contains("keyCode=KEYCODE_HEADSETHOOK"))) {
                    post(EventMediaButtonPress())
                }
            }
            return true
        }
    }

    private fun subscribeHeadsetMicIfNeed() {
        if (!bluetoothScoRegistered) {
            bluetoothScoRegistered = true
            appContext!!.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                    info(TAG, "startHeadsetMic state[$state]")
                    if (state != lastHeadsetMicState) {
                        lastHeadsetMicState = state
                        if (lastHeadsetMicState == 0) {
                            if (!lockHeadsetMicEvent) {
                                post(EventMediaButtonPress())
                            }
                        }
                        lockHeadsetMicEvent = false
                        post(EventBluetoothScoStateChanged(lastHeadsetMicState == 1))
                    }
                }
            }, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED))
        }
    }

    fun startHeadsetMic() {
        try {
            subscribeHeadsetMicIfNeed()
            (appContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager).startBluetoothSco()
        } catch (e: Exception) {
            err(e)
        }
    }

    fun stopHeadsetMic() {
        try {
            subscribeHeadsetMicIfNeed()
            (appContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager).stopBluetoothSco()
            lockHeadsetMicEvent = true
        } catch (e: Exception) {
            err(e)
        }
    }
}