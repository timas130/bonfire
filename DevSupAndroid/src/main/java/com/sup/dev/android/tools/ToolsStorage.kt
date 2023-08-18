package com.sup.dev.android.tools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.libs.json.JsonParsable
import com.sup.dev.java.tools.ToolsThreads
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
object ToolsStorage {

    var externalFileNamePrefix = "f"
    var preferences: SharedPreferences = SupAndroid.appContext!!.getSharedPreferences("android_app_pref", Activity.MODE_PRIVATE)

    fun init(storageKey: String) {
        preferences = SupAndroid.appContext!!.getSharedPreferences(storageKey, Activity.MODE_PRIVATE)
    }

    operator fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    //
    //  Get
    //

    fun getBoolean(key: String, def: Boolean): Boolean {
        return preferences.getBoolean(key, def)
    }

    fun getInt(key: String, def: Int): Int {
        return preferences.getInt(key, def)
    }

    fun getLong(key: String, def: Long): Long {
        return preferences.getLong(key, def)
    }

    fun getFloat(key: String, def: Float): Float {
        return preferences.getFloat(key, def)
    }

    fun getString(key: String, string: String?): String? {
        return preferences.getString(key, string)
    }

    fun getBytes(key: String): ByteArray? {
        val s = preferences.getString(key, null) ?: return null
        return s.toByteArray()
    }

    fun getJson(key: String): Json? {
        val s = getString(key, null) ?: return null
        return Json(s)
    }

    fun getJsonArray(key: String): JsonArray? {
        val string = getString(key, null)
        return if (string == null || string.isEmpty()) null else JsonArray(string)
    }

    fun <K : JsonParsable> getJsonParsable(key: String, cc: KClass<K>): K? {
        val json = getJson(key) ?: return null
        return Json().put("x", json).getJsonParsable("x", cc, null)
    }

    fun <K : JsonParsable> getJsonParsables(key: String, cc: KClass<K>): Array<K>? {
        val json = getJsonArray(key) ?: return null
        return Json().put("x", json).getJsonParsables("x", cc)
    }

    //
    //  Put
    //

    fun put(key: String, v: Boolean?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putBoolean(key, v).apply()
    }

    fun put(key: String, v: Int?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putInt(key, v).apply()
    }

    fun put(key: String, v: Long?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putLong(key, v).apply()
    }

    fun put(key: String, v: Float?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putFloat(key, v).apply()
    }

    fun put(key: String, v: String?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putString(key, v).apply()
    }

    fun put(key: String, v: ByteArray?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putString(key, String(v)).apply()
    }

    fun put(key: String, v: Json?) {
        if (v == null) {
            clear(key)
            return
        }
        put(key, v.toString())
    }

    fun put(key: String, v: JsonParsable?) {
        put(key, v?.json(false, Json()))
    }

    fun put(key: String, v: JsonArray?) {
        if (v == null) {
            clear(key)
            return
        }
        preferences.edit().putString(key, v.toString()).apply()
    }

    fun put(key: String, x: Array<out JsonParsable>?) {
        if (x == null) {
            clear(key)
            return
        }
        val jsonArray = JsonArray()
        for (i in x) jsonArray.put(i.json(true, Json()))
        put(key, jsonArray)
    }

    //
    //  Remove
    //

    fun clear(key: String) {
        preferences.edit().remove(key).apply()
    }


    //
    //  Files
    //

    fun saveImageInDownloadFolder(bitmap: Bitmap, onComplete: (File) -> Unit = {}) {
        ToolsPermission.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGranted = {
                    try {
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs()
                        val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/" + externalFileNamePrefix + "_" + System.currentTimeMillis() + ".png")
                        f.createNewFile()
                        val out = FileOutputStream(f)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.close()
                        ToolsThreads.main { onComplete.invoke(f) }

                        //  Without this, the picture will be hidden until open gallery.
                        if (SupAndroid.activity != null && !SupAndroid.activityIsDestroy) {
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            mediaScanIntent.data = Uri.fromFile(f)
                            SupAndroid.activity!!.sendBroadcast(mediaScanIntent)
                        }
                    } catch (e: Exception) {
                        if (e.toString().contains("Permission denied")) {
                            ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES)
                        } else {
                            throw RuntimeException(e)
                        }
                    }
                },
                onPermissionRestriction = { ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES) })


    }

    fun saveFileInDownloadFolder(bytes: ByteArray, ex: String, onComplete: (File) -> Unit, onPermissionPermissionRestriction: (String) -> Unit = {}) {
        saveFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/" + externalFileNamePrefix + "_" + System.currentTimeMillis() + "." + ex).absolutePath,
                bytes, onComplete, onPermissionPermissionRestriction)
    }

    fun saveFile(patch: String, bytes: ByteArray, onComplete: (File) -> Unit, onPermissionPermissionRestriction: (String) -> Unit = {}) {
        ToolsPermission.requestWritePermission({
            val f = File(patch)
            f.delete()
            if (f.parentFile != null) f.parentFile.mkdirs()
            try {
                val out = FileOutputStream(f)
                out.write(bytes)
                out.close()
                ToolsThreads.main { onComplete.invoke(f) }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }, onPermissionPermissionRestriction)
    }
}