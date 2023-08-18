package com.sup.dev.android.tools

import android.Manifest.permission.*
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads


object ToolsPermission {

    private var code = 1
    private val requests = ArrayList<Request>()

    private class Request(
            val code: Int,
            val onGranted: (String) -> Unit,
            val onPermissionRestriction: (String) -> Unit,
            val onAllPermissionsGranted: () -> Unit
    )

    //
    //  Result
    //

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: Array<Int>) {


        for (r in requests) {
            if (r.code == requestCode) {
                var permissionCounter = 0
                for (i in 0 until permissions.size) {
                    if (grantResults[i] == PERMISSION_GRANTED) {
                        permissionCounter++
                        r.onGranted.invoke(permissions[i])
                    } else {
                        r.onPermissionRestriction.invoke(permissions[i])
                    }
                }
                if(permissionCounter == permissions.size){
                    r.onAllPermissionsGranted.invoke()
                }
            }
        }

    }

    //
    //  Methods
    //

    fun hasPermission(permissions: String): Boolean {
        return hasPermissions(arrayOf(permissions))
    }

    fun hasPermissions(permissions: Array<String>): Boolean {
        var hasAll = true
        for (p in permissions) hasAll = hasAll && ContextCompat.checkSelfPermission(SupAndroid.appContext!!, p) == PERMISSION_GRANTED
        return hasAll
    }


    fun requestPermission(permission: String, onGranted: (String) -> Unit) {
        requestPermissions(arrayOf(permission), onGranted)
    }

    fun requestPermission(permission: String, onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermissions(arrayOf(permission), onGranted, onPermissionRestriction)
    }

    fun requestPermissions(permissions: Array<String>, onGranted: (String) -> Unit) {
        requestPermissions(permissions, onGranted, {})
    }

    fun requestPermissions(permissions: Array<String>, onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermissions(permissions, onGranted, onPermissionRestriction, {})
    }

    fun requestPermissions(permissions: Array<String>, onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit, onAllPermissionsGranted: () -> Unit) {
        val list = ArrayList<String>()
        for (p in permissions) {
            if (hasPermission(p)) onGranted.invoke(p)
            else list.add(p)
        }
        if (list.isEmpty()) {
            onAllPermissionsGranted.invoke()
            return
        }

        val request = Request(code++, onGranted, onPermissionRestriction, onAllPermissionsGranted)
        requests.add(request)
        ActivityCompat.requestPermissions(SupAndroid.activity!!, ToolsMapper.asArray(list), request.code)
    }


    //
    //
    //
    //  Requests Simple
    //
    //
    //

    fun requestReadPermission(onGranted: (String) -> Unit) {
        requestPermission(READ_EXTERNAL_STORAGE, onGranted)
    }

    fun requestWritePermission(onGranted: (String) -> Unit) {
        requestPermission(WRITE_EXTERNAL_STORAGE, onGranted)
    }

    fun requestReadPermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(READ_EXTERNAL_STORAGE, onGranted, onPermissionRestriction)
    }

    fun requestWritePermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(WRITE_EXTERNAL_STORAGE, onGranted, onPermissionRestriction)
    }

    fun requestCallPhonePermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(CALL_PHONE, onGranted, onPermissionRestriction)
    }

    fun requestOverlayPermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(SYSTEM_ALERT_WINDOW, onGranted, onPermissionRestriction)
    }

    fun requestMicrophonePermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(RECORD_AUDIO, onGranted, onPermissionRestriction)
    }

    fun requestBluetouchPermisiion(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(BLUETOOTH_ADMIN, onGranted, onPermissionRestriction)
    }

    fun requestCameraPermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(CAMERA, onGranted, onPermissionRestriction)
    }

    fun requestReadContactsPermission(onGranted: (String) -> Unit, onPermissionRestriction: (String) -> Unit) {
        requestPermission(READ_CONTACTS, onGranted, onPermissionRestriction)
    }

    //
    //  Checks
    //

    fun hasReadPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || hasPermission(READ_EXTERNAL_STORAGE)
    }

    fun hasWritePermission(): Boolean {
        return hasPermission(WRITE_EXTERNAL_STORAGE)
    }

    fun hasCallPhonePermission(): Boolean {
        return hasPermission(CALL_PHONE)
    }

    fun hasOverlayPermission(): Boolean {
        return hasPermission(SYSTEM_ALERT_WINDOW)
    }

    fun hasMicrophonePermission(): Boolean {
        return hasPermission(RECORD_AUDIO)
    }


}
