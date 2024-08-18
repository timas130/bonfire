package com.sup.dev.android.tools

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.sup.dev.android.app.SupAndroid
import java.io.RandomAccessFile
import java.util.*
import kotlin.math.ceil

// "Borrowed" from
// https://github.com/DrKLO/Telegram/blob/master/TMessagesProj/src/main/java/org/telegram/messenger/SharedConfig.java

object ToolsPerformance {
    enum class PerformanceClass {
        Low,
        Average,
        High
    }

    val performanceClass by lazy { measureDevicePerformanceClass() }

    private val LOW_SOC: IntArray = intArrayOf(
        -1775228513,  // EXYNOS 850
        802464304,  // EXYNOS 7872
        802464333,  // EXYNOS 7880
        802464302,  // EXYNOS 7870
        2067362118,  // MSM8953
        2067362060,  // MSM8937
        2067362084,  // MSM8940
        2067362241,  // MSM8992
        2067362117,  // MSM8952
        2067361998,  // MSM8917
        -1853602818 // SDM439
    )

    private fun measureDevicePerformanceClass(): PerformanceClass {
        val androidVersion = Build.VERSION.SDK_INT
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val activityManager = SupAndroid.appContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryClass = activityManager.memoryClass

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hash = Build.SOC_MODEL.uppercase(Locale.getDefault()).hashCode()
            if (LOW_SOC.contains(hash)) {
                return PerformanceClass.Low
            }
        }

        var totalCpuFreq = 0
        var freqResolved = 0
        for (i in 0 until cpuCount) {
            try {
                val reader = RandomAccessFile(
                    String.format(
                        Locale.ENGLISH,
                        "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq",
                        i
                    ), "r"
                )
                val line = reader.readLine()
                if (line != null) {
                    totalCpuFreq += line.toInt() / 1000
                    freqResolved++
                }
                reader.close()
            } catch (ignore: Throwable) {
            }
        }
        val maxCpuFreq = if (freqResolved == 0) {
            -1
        } else {
            ceil((totalCpuFreq / freqResolved.toFloat()).toDouble()).toInt()
        }

        var ram = -1L
        try {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            ram = memoryInfo.totalMem
        } catch (ignore: Exception) {
        }

        val performanceClass = if (
            androidVersion < 21 ||
            cpuCount <= 2 ||
            memoryClass <= 100 ||
            cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 ||
            cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 ||
            cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24 ||
            ram != -1L && ram < 2L * 1024L * 1024L * 1024L
        ) {
            PerformanceClass.Low
        } else if (
            cpuCount < 8 ||
            memoryClass <= 160 ||
            maxCpuFreq != -1 && maxCpuFreq <= 2055 ||
            maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23
        ) {
            PerformanceClass.Average
        } else {
            PerformanceClass.High
        }

        Log.d("ToolsPerformance", "PerformanceClass determined: $performanceClass")

        return performanceClass
    }
}
