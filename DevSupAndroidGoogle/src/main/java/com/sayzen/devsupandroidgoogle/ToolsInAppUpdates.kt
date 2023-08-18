package com.sayzen.devsupandroidgoogle

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.sayzen.devsupandroidgoogle.events.EventInAppUpdatesChanged
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus

object ToolsInAppUpdates {

    private var appUpdateInfo: AppUpdateInfo? = null
    private var apiIsNotAvailable = false
    private var started = false

    fun start() {
        if (started) return
        started = true

        val appUpdateManager = AppUpdateManagerFactory.create(SupAndroid.appContext!!)

        appUpdateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    this.appUpdateInfo = appUpdateInfo
                    EventBus.post(EventInAppUpdatesChanged())
                }
                .addOnFailureListener { e ->
                    err(e)
                    apiIsNotAvailable = true
                    EventBus.post(EventInAppUpdatesChanged())
                }
    }


    fun isApiNotAvailable() = apiIsNotAvailable
    fun isNone() = !apiIsNotAvailable && (appUpdateInfo == null || appUpdateInfo?.updateAvailability() == UpdateAvailability.UNKNOWN)
    fun isAvailable() = appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
    fun isNotAvailable() = appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE
    fun isDownloaded() = appUpdateInfo?.installStatus() == InstallStatus.DOWNLOADED
    fun isCanceled() = appUpdateInfo?.installStatus() == InstallStatus.CANCELED
    fun isDownloading() = appUpdateInfo?.installStatus() == InstallStatus.DOWNLOADING
    fun isFailed() = appUpdateInfo?.installStatus() == InstallStatus.FAILED
    fun isInstalled() = appUpdateInfo?.installStatus() == InstallStatus.INSTALLED
    fun isInstalling() = appUpdateInfo?.installStatus() == InstallStatus.INSTALLING
    fun isPending() = appUpdateInfo?.installStatus() == InstallStatus.PENDING

    fun showDialog_Flexible(onError:(Exception)->Unit) {
        if (appUpdateInfo == null) return
        if (SupAndroid.activity == null || SupAndroid.activityIsDestroy) return
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(SupAndroid.activity!!)
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo!!,
                    AppUpdateType.FLEXIBLE,
                    SupAndroid.activity!!,
                    568
            )
        } catch (e: Exception) {
            err(e)
            onError.invoke(e)
        }
    }

}