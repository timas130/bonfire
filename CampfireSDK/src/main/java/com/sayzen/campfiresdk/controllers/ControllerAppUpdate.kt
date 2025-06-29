package com.sayzen.campfiresdk.controllers

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import com.dzen.campfire.api.API_TRANSLATE
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.model.*
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
    private lateinit var updateInfo: AppUpdateInfo
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    fun isUpdateUnavailable() = updateStatus == UpdateStatus.UNAVAILABLE || updateStatus == UpdateStatus.UNKNOWN
    fun isUpdateAvailable() = updateStatus == UpdateStatus.AVAILABLE
    fun isUpdateDownloading() = updateStatus == UpdateStatus.DOWNLOADING
    fun isUpdateDownloaded() = updateStatus == UpdateStatus.DOWNLOADED

    private fun postEvent() = EventBus.post(EventAppUpdateAvailable(updateStatus, updateInfo.availableVersionCode()))

    fun init() {
        updateManager = AppUpdateManagerFactory.create(SupAndroid.appContext!!)
        updateManager.registerListener { state ->
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

        updateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
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
        updateManager.startUpdateFlowForResult(
            updateInfo,
            activityResultLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        )
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
