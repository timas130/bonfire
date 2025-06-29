package com.sayzen.campfiresdk.controllers

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import com.dzen.campfire.api.API_TRANSLATE
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.project.EventAppUpdateAvailable
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerAppUpdate {
    enum class UpdateStatus {
        UNKNOWN,
        UNAVAILABLE,
        // can be the status of an EventAppUpdateAvailable
        AVAILABLE,
        DOWNLOADING,
        DOWNLOADED
    }

    var updateStatus = UpdateStatus.UNKNOWN
    private lateinit var updateManager: AppUpdateManager
    private var updateInfo: AppUpdateInfo? = null
    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    fun isUpdateUnavailable() = updateStatus == UpdateStatus.UNAVAILABLE || updateStatus == UpdateStatus.UNKNOWN
    fun isUpdateAvailable() = updateStatus == UpdateStatus.AVAILABLE
    fun isUpdateDownloading() = updateStatus == UpdateStatus.DOWNLOADING
    fun isUpdateDownloaded() = updateStatus == UpdateStatus.DOWNLOADED

    private fun postEvent() {
        EventBus.post(EventAppUpdateAvailable(updateStatus))
    }

    fun init() {
        updateManager = AppUpdateManagerFactory.create(SupAndroid.appContext!!)
        updateManager.registerListener { state ->
            Log.d("ControllerAppUpdate", "update state: $state")
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                if (updateStatus != UpdateStatus.DOWNLOADING) {
                    updateStatus = UpdateStatus.DOWNLOADING
                    postEvent()
                }
            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                updateStatus = UpdateStatus.DOWNLOADED
                postEvent()
                showAlertForCompleteUpdate()
            }
        }

        refreshUpdateInfo()
    }

    private fun refreshUpdateInfo() {
        updateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            Log.d("ControllerAppUpdate", "updateInfo: $updateInfo")
            this.updateInfo = updateInfo
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateStatus = UpdateStatus.AVAILABLE
                postEvent()
            } else {
                updateStatus = UpdateStatus.UNAVAILABLE
            }
        }
    }

    fun registerForActivityResult() {
        activityResultLauncher = SupAndroid.activity!!.registerForActivityResult(
            StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                // make the update available again if the user has canceled it
                updateStatus = UpdateStatus.AVAILABLE
                postEvent()
            }
            if (result.resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                ToolsToast.show(R.string.app_update_failed)
            }
        }
    }

    fun startUpdate() {
        val updateInfo = updateInfo ?: return
        val activityResultLauncher = activityResultLauncher ?: return
        updateManager.startUpdateFlowForResult(
            updateInfo,
            activityResultLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        )
        this.updateInfo = null
        refreshUpdateInfo()
    }

    fun completeUpdate() {
        updateManager.completeUpdate()
    }

    private fun showAlertForCompleteUpdate() {
        SplashAlert()
                .setText(t(API_TRANSLATE.app_update_complete_alert))
                .setOnCancel(t(API_TRANSLATE.app_not_now))
                .setOnEnter(t(API_TRANSLATE.app_restart)) { completeUpdate() }
                .asSheetShow()
    }
}
