package com.sup.dev.java.tools

import java.text.SimpleDateFormat
import java.util.*

object ToolsDate {

    private val format1 = "dd MMM yyyy  HH:mm"
    private val format2 = "dd MMM  HH:mm"
    private val format3 = "HH:mm"

    val currentDayOfWeek: Int
        get() {
            var i = GregorianCalendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
            if (i == -1) i = 6
            return i
        }


    val currentHourseOfDay: Int
        get() = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val currentMinutesOfHourse: Int
        get() = GregorianCalendar.getInstance().get(Calendar.MINUTE)

    val nowDay: Calendar
        get() = GregorianCalendar.getInstance()


    fun getStartOfDay(): Long {
        val calendar = GregorianCalendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val calendar = GregorianCalendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    fun getStartOfNextMonth(): Long {
        val calendar = GregorianCalendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    fun getLocalTime(date: Long) = date + getTimeZone()

    fun getTimeZone() = Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()

    fun getTimeZoneHours() = (getTimeZone() / 1000 / 60 / 60).toInt()

    fun getTimeZoneName(): String {
        val cal = Calendar.getInstance()
        val milliDiff = cal.get(Calendar.ZONE_OFFSET).toLong()
        val ids = TimeZone.getAvailableIDs()
        var name = ""
        for (id in ids) {
            val tz = TimeZone.getTimeZone(id)
            if (tz.rawOffset.toLong() == milliDiff) {
                name = id
                break
            }
        }
        return name
    }

    fun getStartOfDay(date: Long) = getStartOfDay_GlobalTimeZone(date) - getTimeZone()

    fun getStartOfDay_GlobalTimeZone(date: Long):Long{
        val calendar = GregorianCalendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun format_yyyy_MM_dd(date: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
    }

    fun format_kk_mm_ss(date: Long): String {
        return SimpleDateFormat("kk:mm:ss", Locale.getDefault()).format(Date(date))
    }

    fun format_hh_mm_ss(date: Long): String {
        return format_hh_mm_ss(Date(date))
    }

    fun format_hh_mm_ss(date: Date): String {
        return SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(date)
    }

    fun format_HH_mm_ss(date: Date): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
    }

    fun format_dd_MM_yyyy(date: Date): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
    }

    fun timeToString(h: Int, m: Int, s: Int): String {
        var text = ""

        text += if (h < 10) "0$h" else h
        text += ":"
        text += if (m < 10) "0$m" else m
        text += ":"
        text += if (s < 10) "0$s" else s


        return text
    }

    fun timeToString(h: Int, m: Int): String {
        var s = ""

        s += if (h < 10) "0$h" else h
        s += ":"
        s += if (m < 10) "0$m" else m

        return s
    }

    fun dayTimeToString_Ms(ms: Int) = dayTimeToString_Ms(ms.toLong())
    fun dayTimeToString_Ms(ms: Long) = dayTimeToString_Sec(ms / 1000)
    fun dayTimeToString_Ms_HH_MM_SS(ms: Long) = dayTimeToString_Sec_HH_MM_SS(ms / 1000)

    fun dayTimeToString_Sec(sec: Int) = dayTimeToString_Sec(sec.toLong())
    fun dayTimeToString_Sec(sec: Long) = dayTimeToString_Min(sec / 60)
    fun dayTimeToString_Sec_HH_MM_SS(sec: Long) = timeToString(sec.toInt() / 60 / 60, (sec.toInt()%(60*60))/60, sec.toInt() % 60)

    fun dayTimeToString_Min(dayTime: Int) = dayTimeToString_Min(dayTime.toLong())
    fun dayTimeToString_Min(dayTime: Long) = timeToString(dayTime.toInt() / 60, dayTime.toInt() % 60)

    fun dateToString(time: Long): String {
        val nowDate = GregorianCalendar.getInstance()
        val date = GregorianCalendar.getInstance()

        nowDate.timeInMillis = System.currentTimeMillis()
        date.timeInMillis = time
        val formatter: SimpleDateFormat
        if (nowDate.get(Calendar.YEAR) != date.get(Calendar.YEAR)) {
            formatter = SimpleDateFormat(format1)
        } else {
            if (nowDate.get(Calendar.MONTH) != date.get(Calendar.MONTH) || nowDate.get(Calendar.DAY_OF_MONTH) != date.get(Calendar.DAY_OF_MONTH)) {
                formatter = SimpleDateFormat(format2)
            } else {
                formatter = SimpleDateFormat(format3)
            }
        }
        return formatter.format(Date(time))
    }

    fun dateToStringFull(time: Long): String {
        return SimpleDateFormat(format1).format(Date(time))
    }

    fun getCurrentMonthOfYear() = GregorianCalendar.getInstance().get(Calendar.MONTH) + 1

    fun getCurrentMonthOfYear(date: Long): Int {
        val instance = GregorianCalendar.getInstance()
        instance.timeInMillis = date
        return instance.get(Calendar.MONTH)
    }

    fun getCurrentDayOfMonth() = GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getCurrentDayOfMonth(date: Long): Int {
        val instance = GregorianCalendar.getInstance()
        instance.timeInMillis = date
        return instance.get(Calendar.DAY_OF_MONTH)
    }

    fun getCurrentHourOfDay() = getCurrentMinutesOfDay() / 60

    fun getCurrentMinutesOfDay() = getCurrentSecondsOfDay() / 60

    fun getCurrentMinutesOfHour() = getCurrentMinutesOfDay() % 60

    fun getCurrentSecondsOfDay() = getCurrentMillisecondsOfDay() / 1000

    fun getCurrentSecondsOfHour() = getCurrentSecondsOfDay() % (60 * 60)

    fun getCurrentSecondsOfMinute() = getCurrentSecondsOfDay() % 60

    fun getCurrentMillisecondsOfDay(): Long {
        val calendar = GregorianCalendar.getInstance()
        val minutes = calendar.get(Calendar.MINUTE)
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val seconds = calendar.get(Calendar.SECOND)
        val milliseconds = calendar.get(Calendar.MILLISECOND)
        return ((hours * 60L + minutes) * 60 + seconds) * 1000 + milliseconds
    }

    fun getLocalDateTime(serverDateTime: String, serverTimezoneId: String = "UTC", format: String = "yyyy-MM-dd HH:mm:ss"): String {
        return try {
            val dateFormat = SimpleDateFormat(format)
            dateFormat.timeZone = TimeZone.getTimeZone(serverTimezoneId) // timezone of server
            val date = dateFormat.parse(serverDateTime)
            dateFormat.timeZone = Calendar.getInstance().timeZone        // timezone of device
            dateFormat.format(date)
        } catch (e: Exception) {
            serverDateTime
        }
    }


}
