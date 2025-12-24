package com.sup.dev.java.tools

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ToolsDate {
    private const val format_full = "EEE, dd.MM.yyyy HH:mm:ss"
    private const val format_short1 = "d MMM y HH:mm"
    private const val format_short2 = "d MMM HH:mm"
    private const val format_short3 = "HH:mm"

    // 0 - Monday, 6 - Sunday
    val currentDayOfWeek: Int
        get() = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

    fun dateToString(time: Long, compact: Boolean = true): String {
        val date = Date(time)
        if (!compact) {
            val formatter = SimpleDateFormat(format_short1, Locale.getDefault())
            return formatter.format(date)
        }

        val calendarNow = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.setTime(date)

        val formatter: SimpleDateFormat
        if (calendarNow.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) {
            formatter = SimpleDateFormat(format_short1, Locale.getDefault())
        } else if (calendarNow.get(Calendar.DAY_OF_YEAR) != calendar.get(Calendar.DAY_OF_YEAR)) {
            formatter = SimpleDateFormat(format_short2, Locale.getDefault())
        } else {
            formatter = SimpleDateFormat(format_short3, Locale.getDefault())
        }

        return formatter.format(date)
    }

    fun dateToStringFull(time: Long): String {
        val formatter = SimpleDateFormat(format_full, Locale.getDefault())
        val formatted = formatter.format(Date(time))
        val capitalized = formatted.substring(0, 1).uppercase() + formatted.substring(1)
        return capitalized
    }

    fun getStartOfDay(date: Long? = null): Long {
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.timeInMillis = date
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getCurrentMillisecondsOfDay(): Long {
        val calendar = Calendar.getInstance()
        return (
            calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
            calendar.get(Calendar.MINUTE) * 60 * 1000 +
            calendar.get(Calendar.SECOND) * 60 +
            calendar.get(Calendar.MILLISECOND)
        ).toLong()
    }

    fun getCurrentMinutesOfDay(): Long {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60L + calendar.get(Calendar.MINUTE)
    }

    fun getCurrentDayOfMonth() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toLong()

    fun timeToString(hours: Int, minutes: Int) = hours.toString() +
        ":" + (if (minutes < 10) "0" else "") + minutes.toString()

    fun timeToString(time: Long): String {
        val hours = time / (1000 * 60 * 60)
        val minutes = (time / (1000 * 60)) % 60
        val seconds = (time / 1000) % 60

        return hours.toString() +
            ":" + (if (minutes < 10) "0" else "") + minutes.toString() +
            ":" + (if (seconds < 10) "0" else "") + seconds.toString()
    }
}
