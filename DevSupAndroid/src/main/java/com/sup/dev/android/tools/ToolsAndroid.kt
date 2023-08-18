package com.sup.dev.android.tools

import android.Manifest
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.media.audiofx.AcousticEchoCanceler
import android.net.ConnectivityManager
import android.os.*
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import com.sup.dev.android.BuildConfig
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.magic_box.Miui
import com.sup.dev.android.magic_box.ServiceNetworkCheck
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.*

object ToolsAndroid {

    fun lockScreenCurrentOrientation() {
        lockScreenOrientation (getScreenRotation())
    }
    fun lockScreenOrientation(rotation:Int) {
        when (rotation) {
            Surface.ROTATION_0 -> lockScreenPortrait()
            Surface.ROTATION_90 -> lockScreenLandscape()
            Surface.ROTATION_180 -> lockScreenPortraitReverse()
            Surface.ROTATION_270 -> lockScreenLandscapeReverse()
        }
    }

    fun lockScreenPortrait() {
        SupAndroid.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun lockScreenPortraitReverse() {
        SupAndroid.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
    }

    fun lockScreenLandscape() {
        SupAndroid.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    fun lockScreenLandscapeReverse() {
        SupAndroid.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    }

    fun unlockScreenOrientation() {
        SupAndroid.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun getDownloadsFolder(): File {
        val f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        f.mkdirs()
        return f
    }

    private var audioFocusListener = AudioManager.OnAudioFocusChangeListener() {}

    //
    //  Device
    //

    fun isScreenOn(): Boolean {
        val powerManager = SupAndroid.appContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT < 20) powerManager.isScreenOn else powerManager.isInteractive
    }

    @Suppress("DEPRECATION")
    fun requestAudioFocus() {
        val audioManager = SupAndroid.appContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
    }

    @Suppress("DEPRECATION")
    fun releaseAudioFocus() {
        val audioManager = SupAndroid.appContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(audioFocusListener)
    }

    @Suppress("DEPRECATION")
    fun setLanguage(context: Context, lang: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val res = context.resources
            val conf = res.configuration
            conf.setLocale(Locale(lang.toLowerCase()))
            res.updateConfiguration(conf, res.displayMetrics)
        }
    }

    @Suppress("DEPRECATION")
    fun getLanguage(context: Context): String {
        val res = context.resources
        val conf = res.configuration
        return conf.locale.language.toLowerCase()

    }

    fun getVersion() = SupAndroid.appContext!!.packageManager.getPackageInfo(SupAndroid.appContext!!.packageName, 0).versionName
    fun getVersionCode() = SupAndroid.appContext!!.packageManager.getPackageInfo(SupAndroid.appContext!!.packageName, 0).versionCode

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    fun getBluetoothMacAddress(): String {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothMacAddress = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val mServiceField = bluetoothAdapter.javaClass.getDeclaredField("mService")
                mServiceField.isAccessible = true
                val btManagerService = mServiceField.get(bluetoothAdapter)
                if (btManagerService != null) bluetoothMacAddress = btManagerService.javaClass.getMethod("getAddress").invoke(btManagerService) as String
            } catch (e: NoSuchFieldException) {
                err(e)
            } catch (e: NoSuchMethodException) {
                err(e)
            } catch (e: IllegalAccessException) {
                err(e)
            } catch (e: InvocationTargetException) {
                err(e)
            }

        } else {
            bluetoothMacAddress = bluetoothAdapter.address
        }
        return bluetoothMacAddress
    }

    fun getProcessName(): String? {
        val processName = getProcessNameCmdLine()
        if (processName == null) getProcessNameActivityManager()
        return processName
    }

    private fun getProcessNameCmdLine(): String? {
        var cmdlineReader: BufferedReader? = null
        try {
            cmdlineReader = BufferedReader(InputStreamReader(FileInputStream("/proc/" + Process.myPid() + "/cmdline"), "iso-8859-1"))
            var c: Int
            val processName = StringBuilder()
            while (true) {
                c = cmdlineReader.read()
                if (c < 1) break
                processName.append(c.toChar())
            }
            return processName.toString()
        } catch (ex: Exception) {
            err(ex)
        } finally {
            if (cmdlineReader != null) {
                try {
                    cmdlineReader.close()
                } catch (e: IOException) {
                    err(e)
                }

            }
        }
        return null
    }

    private fun getProcessNameActivityManager(): String? {
        val pid = android.os.Process.myPid()
        val manager = SupAndroid.appContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.runningAppProcesses
        if (infos != null) {
            for (processInfo in infos) {
                if (processInfo.pid == pid) {
                    return processInfo.processName
                }
            }
        }
        return null
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkName(): String? {
        val cm = SupAndroid.appContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetworkInfo
        return n?.extraInfo
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isHasInternetConnection(onResult: (Boolean) -> Unit) {
        ServiceNetworkCheck.check(onResult)
    }

    fun getBottomNavigationBarHeight(): Int {
        val resources = SupAndroid.appContext!!.resources
        val navBarExists = resources.getBoolean(SupAndroid.appContext!!.resources.getIdentifier("config_showNavigationBar", "bool", "android"))
        return if (navBarExists) resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android")) else 0
    }

    fun isTablet(): Boolean {
        return SupAndroid.appContext!!.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun getLanguageCode(): String {
        return Locale.getDefault().language.toLowerCase()
    }

    fun isEchoCancelerAvailable() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) AcousticEchoCanceler.isAvailable() else false

    fun isDirectToTV(): Boolean {
        return (SupAndroid.appContext!!.packageManager.hasSystemFeature("android.software.leanb‌​ack")
                || SupAndroid.appContext!!.packageManager.hasSystemFeature("android.software.live_tv")
                || (SupAndroid.appContext!!.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
                || Build.MODEL.toLowerCase().contains("tv box"))
    }

    fun appIsVisible(): Boolean {
        val activityManager = SupAndroid.appContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val proc = activityManager.runningAppProcesses

        if (proc == null) return true
        for (info in proc)
            if (info.processName == SupAndroid.appContext!!.packageName)
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                    return true

        return false

    }

    fun isDebug() = BuildConfig.DEBUG

    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun getMaxMemory(): Long {
        return Runtime.getRuntime().totalMemory()
    }

    fun getFreeMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    fun isMiui(): Boolean {
        return Miui.isMiui
    }

    fun setToClipboard(text: String) {
        val clipboard = SupAndroid.appContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", text)
        clipboard.setPrimaryClip(clip)
    }

    fun getFromClipboard(): String? {
        val clipboard = SupAndroid.appContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = clipboard.primaryClip
        if (primaryClip == null) return null
        return if (primaryClip.itemCount == 0)
            null
        else {
            val t = primaryClip.getItemAt(0).text
            if (t == null) return null
            return t.toString()
        }
    }


    //
    //  Package / Process
    //

    fun hasBroadcastReceiver(process: String, intent: Intent, context: Context): Boolean {
        val pm = context.packageManager
        val listeners = pm.queryBroadcastReceivers(intent, 0)
        for (info in listeners) {
            if (info.activityInfo.processName == process)
                return true
        }
        return false
    }

    @Suppress("DEPRECATION")
    fun checkServiceStarted(appId: String, serviceName: String): Boolean {

        val am = SupAndroid.appContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (rsi in am.getRunningServices(150))
            if (appId == rsi.service.packageName && serviceName.toLowerCase() == rsi.service.className.toLowerCase())
                return true

        return false
    }

    fun getSystemRootDir(): File {
        return Environment.getExternalStorageDirectory()
    }

    //
    //  Screen
    //

    fun getScreenRotation() =  SupAndroid.activity?.getWindowManager()?.getDefaultDisplay()?.getRotation() ?: Surface.ROTATION_0

    fun isScreenPortrait(): Boolean {
        return !isScreenLandscape()
    }

    fun isScreenLandscape(): Boolean {
        return getScreenW() > getScreenH()
    }

    fun getScreenOrientation(): Int {
        if (SupAndroid.appContext == null) return 0 // fixes android studio layout preview
        return (SupAndroid.appContext!!.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    }

    fun getScreenW(): Int {
        return SupAndroid.appContext!!.resources.displayMetrics.widthPixels
    }

    fun getScreenH(): Int {
        return SupAndroid.appContext!!.resources.displayMetrics.heightPixels
    }

    fun maxScreenSide(): Int {
        return getScreenW().coerceAtLeast(getScreenH())
    }

    fun minScreenSide(): Int {
        return getScreenW().coerceAtMost(getScreenH())
    }

    @Suppress("DEPRECATION")
    fun isScreenKeyLocked(): Boolean {
        val myKeyManager = SupAndroid.appContext!!.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return myKeyManager.inKeyguardRestrictedInputMode()
    }

    @Suppress("DEPRECATION")
    fun screenOn(time:Long=5000) {
        if ((SupAndroid.appContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn)
            return

        val pm = SupAndroid.appContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "FULL WAKE LOCK")

        screenLock.acquire()
        Thread {
            ToolsThreads.sleep(time)
            if (screenLock.isHeld)
                screenLock.release()
        }.start()

    }

}