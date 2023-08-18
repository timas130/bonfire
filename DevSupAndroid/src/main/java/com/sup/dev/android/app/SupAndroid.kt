package com.sup.dev.android.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.screens.activity.SActivity
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.classes.callbacks.CallbacksList
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads


@SuppressLint("StaticFieldLeak")
object SupAndroid {

    val onLowMemotyCallbacks = CallbacksList()

    var SERVICE_FOREGROUND = 4000
    var SERVICE_NETWORK_CHECK = 4001

    var TEXT_APP_NAME: String? = null
    var TEXT_APP_CANCEL: String? = null
    var TEXT_APP_WHOOPS: String? = null
    var TEXT_APP_RETRY: String? = null
    var TEXT_APP_BACK: String? = null
    var TEXT_APP_DOWNLOADING: String? = null
    var TEXT_APP_SHARE: String? = null
    var TEXT_APP_SHARE_MESSAGE_HINT: String? = null
    var TEXT_APP_DOWNLOADED: String? = null
    var TEXT_APP_DONT_SHOW_AGAIN: String? = null
    var TEXT_APP_LINK: String? = null
    var TEXT_APP_CHOOSE: String? = null
    var TEXT_APP_LOADING: String? = null
    var TEXT_ERROR_NETWORK: String? = null
    var TEXT_ERROR_ACCOUNT_BANED: String? = null
    var TEXT_ERROR_GONE: String? = null
    var TEXT_ERROR_CANT_LOAD_IMAGE: String? = null
    var TEXT_ERROR_PERMISSION_FILES: String? = null
    var TEXT_ERROR_PERMISSION_MIC: String? = null
    var TEXT_ERROR_APP_NOT_FOUND: String? = null
    var TEXT_ERROR_CANT_FIND_IMAGES: String? = null
    var TEXT_ERROR_MAX_ITEMS_COUNT: String? = null

    var imgErrorNetwork: ImageLink? = null
    var imgErrorGone: ImageLink? = null

    var editMode = false
    var appContext: Context? = null
    var activity: SActivity? = null
    var activityClass: Class<out SActivity>? = null
    var activityIsVisible = false
    var activityIsDestroy = false
    var appId = ""

    fun onLowMemory() {
        onLowMemotyCallbacks.invoke()
    }

    fun addOnLowMemory(onLowMemory: () -> Unit) {
        onLowMemotyCallbacks.add(onLowMemory)
    }

    fun initEditMode(view: View) {
        if (!view.isInEditMode) return
        editMode = true
        init(view.context, "", SActivity::class.java)
    }


    fun init(appContext: Context, appId: String, activityClass: Class<out SActivity>) {
        this.appId = appId
        this.activityClass = activityClass
        this.appContext = appContext

        ToolsThreads.onMain = { onNextTime, runnable ->
            if ((!onNextTime) && ToolsAndroid.isMainThread()) runnable.invoke()
            else Handler(Looper.getMainLooper()).post {
                runnable.invoke()
            }
        }

        Debug.printer = { s -> Log.e("Debug", s) }
        Debug.printerInfo = { s -> Log.i("Debug", s) }
        Debug.exceptionPrinter = { th -> Log.e("Debug", "", th) }

        TEXT_APP_NAME = loadText("app_name")
        TEXT_APP_CANCEL = loadText("app_cancel")
        TEXT_APP_WHOOPS = loadText("app_whoops")
        TEXT_APP_RETRY = loadText("app_retry")
        TEXT_APP_BACK = loadText("app_back")
        TEXT_APP_DOWNLOADING = loadText("app_downloading")
        TEXT_APP_SHARE = loadText("app_share")
        TEXT_APP_SHARE_MESSAGE_HINT = loadText("app_share_message_hint")
        TEXT_APP_DOWNLOADED = loadText("app_downloaded")
        TEXT_APP_DONT_SHOW_AGAIN = loadText("app_dont_show_again")
        TEXT_APP_LINK = loadText("app_link")
        TEXT_APP_CHOOSE = loadText("app_choose")
        TEXT_APP_LOADING = loadText("app_loading")
        TEXT_ERROR_NETWORK = loadText("error_network")
        TEXT_ERROR_ACCOUNT_BANED = loadText("error_account_baned")
        TEXT_ERROR_GONE = loadText("error_gone")
        TEXT_ERROR_CANT_LOAD_IMAGE = loadText("error_cant_load_image")
        TEXT_ERROR_PERMISSION_FILES = loadText("error_permission_files")
        TEXT_ERROR_PERMISSION_MIC = loadText("error_permission_mic")
        TEXT_ERROR_APP_NOT_FOUND = loadText("error_app_not_found")
        TEXT_ERROR_CANT_FIND_IMAGES = loadText("error_cant_find_images")
        TEXT_ERROR_MAX_ITEMS_COUNT = loadText("error_max_items_count")

        val rNetwork = loadImage("error_network") ?: 0; if (rNetwork > 0) imgErrorNetwork = ImageLoader.load(rNetwork)
        val rGone = loadImage("error_gone") ?: 0; if (rGone > 0) imgErrorGone = ImageLoader.load(rGone)
    }

    private fun loadText(id: String): String? {
        try {
            return ToolsResources.s(id)
        } catch (e: IllegalArgumentException) {
            //err("Init warning: can't find vText with id [$id]")
            return null
        }
    }

    private fun loadImage(id: String): Int? {
        try {
            return ToolsResources.getDrawableId(id)
        } catch (e: IllegalArgumentException) {
            //err("Init warning: can't find image with id [$id]")
            return null
        }
    }

}