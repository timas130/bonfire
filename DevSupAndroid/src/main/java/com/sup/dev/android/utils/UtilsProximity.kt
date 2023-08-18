package com.sup.dev.android.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager

import android.content.Context.SENSOR_SERVICE
import com.sup.dev.android.app.SupAndroid

class UtilsProximity(
        onScreenOffChanged: (Boolean) -> Unit
) {

    companion object {
        var isScreenOffBySensor = false
    }

    private val powerManager: PowerManager = SupAndroid.appContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wakeLock: PowerManager.WakeLock
    private val litener:SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            isScreenOffBySensor = event.values[0] == 0f
            if (event.values.isNotEmpty()) onScreenOffChanged.invoke(event.values[0] == 0f)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }
    }

    init {

        var powerValue = 0x00000020

        try {
            powerValue = PowerManager::class.java.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null)
        } catch (ignored: Throwable) {
        }

        wakeLock = powerManager.newWakeLock(powerValue, "myapp:incall")

        val mgr = SupAndroid.appContext!!.getSystemService(SENSOR_SERVICE) as SensorManager
        val myProximitySensor = mgr.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        mgr.registerListener(litener, myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        if (!wakeLock.isHeld) wakeLock.acquire(10 * 60 * 1000L)

    }

    fun release() {
        if (wakeLock.isHeld) wakeLock.release()
        val mgr = SupAndroid.appContext!!.getSystemService(SENSOR_SERVICE) as SensorManager
        mgr.unregisterListener(litener)
        isScreenOffBySensor = false
    }
}