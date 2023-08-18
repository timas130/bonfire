package com.sup.dev.android.tools

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.tools.ToolsThreads

object ToolsVibration {

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun vibrate() {
        vibrate(500)
    }

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun vibrate(time: Long) {
        val v = SupAndroid.appContext!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(time)
        }
    }

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun vibratePattern(vararg time: Long) {
        vibratePattern(time, 0)
    }

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun vibratePattern(time: LongArray, index: Int) {
        vibrate(time[index])
        if (index < time.size - 1)
            ToolsThreads.main(time[index]) { vibratePattern(time, index + 1) }
    }

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun vibratePatternWithSleeps(vararg time: Long) {
        vibratePatternWithSleeps(time, 0)
    }
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun vibratePatternWithSleeps(time: LongArray, index: Int) {
        vibrate(time[index])
        if (index < time.size - 1)
            ToolsThreads.main(time[index] + time[index + 1]) { vibratePattern(time, index + 2) }
    }

}