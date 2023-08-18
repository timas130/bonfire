package com.sup.dev.java.libs.debug

import com.sup.dev.java.tools.ToolsMapper
import java.io.PrintWriter
import java.io.StringWriter

fun log(vararg o: Any?) {
    Debug.logPrint("DXX", *o)
}

fun err(e: Throwable) {
    Debug.logPrint("DEE", e)
}

fun info(vararg o: Any?) {
    Debug.info("DII", *o)
}

fun err(vararg o: Any?) {
    Debug.err("DII", *o)
}

object Debug {

    var log = ArrayList<String>()
    var printer = { s: String -> System.err.println(s) }
    var printerInfo = { s: String -> System.err.println(s) }
    var exceptionPrinter = { ex: Throwable -> ex.printStackTrace() }

    private var time = 0L
    private var timeStepsCount = 0
    private var timeStepsCounter = 0
    private var printTimeEnabled = true
    private var minimumTimeToPrintMs = -1L

    fun addToLog(s:String){
        log.add(s)
        while (log.size > 5000){
            log.removeAt(0)
        }
    }

    fun saveTime() {
        time = System.nanoTime()
    }

    fun setMinimumTimeToPrint(ms: Long) {
        minimumTimeToPrintMs = ms
    }

    fun isEnablePrintTime() = printTimeEnabled

    fun enablePrintTime() {
        printTimeEnabled = true
    }

    fun disablePrintTime() {
        printTimeEnabled = false
    }

    fun printTime() {
        printTime("Time ms: ")
    }

    fun printTime(prefix: String = ""):Boolean {
        val timeMs = ToolsMapper.timeNanoToMs(System.nanoTime() - time)
        if (printTimeEnabled && timeMs >= minimumTimeToPrintMs) {
            val s = "DXX $prefix $timeMs left"
            addToLog(s)
            printer.invoke(s)
            return true
        }
        return false
    }

    fun printTimeAndSave(prefix: String = ""):Boolean {
        val b = printTime(prefix)
        saveTime()
        return b
    }

    fun printTimeMicro(prefix: String = "") {
        val timeMicro = (System.nanoTime() - time) / 1000
        if (printTimeEnabled && timeMicro >= minimumTimeToPrintMs * 1000) {
            val s = "DXX $prefix $timeMicro left"
            addToLog(s)
            printer.invoke(s)
        }
    }

    fun printTimeNano(prefix: String = "") {
        val timeNano = System.nanoTime() - time
        if (printTimeEnabled && timeNano >= ToolsMapper.timeMsToNano(minimumTimeToPrintMs)){
            val s = "DXX $prefix $timeNano left"
            addToLog(s)
            printer.invoke(s)
        }
    }

    fun saveTimeSteps(timeStepsCount: Int, prefix: String = "") {

        if (timeStepsCounter == 0) {
            this.timeStepsCount = timeStepsCount
            saveTime()
        }

        timeStepsCounter++
        if (timeStepsCounter >= timeStepsCount) {
            val timeMs = ToolsMapper.timeNanoToMs(System.nanoTime() - time)
            if (printTimeEnabled && timeMs >= minimumTimeToPrintMs) {
                val s  = "DXX $prefix $timeMs left [$timeStepsCounter steps]"
                addToLog(s)
                printer.invoke(s)
            }
            timeStepsCounter = 0
        }
    }

    fun saveTimeStepsMicro(timeStepsCount: Int, prefix: String = "") {

        if (timeStepsCounter == 0) {
            this.timeStepsCount = timeStepsCount
            saveTime()
        }

        timeStepsCounter++
        if (timeStepsCounter >= timeStepsCount) {
            val timeMicro = (System.nanoTime() - time) / 1000
            if (printTimeEnabled  && timeMicro >= minimumTimeToPrintMs * 1000){
                val s = "DXX $prefix $timeMicro left [$timeStepsCounter steps]"
                addToLog(s)
                printer.invoke(s)
            }
            timeStepsCounter = 0
        }
    }

    fun saveTimeStepsNano(timeStepsCount: Int, prefix: String = "") {

        if (timeStepsCounter == 0) {
            this.timeStepsCount = timeStepsCount
            saveTime()
        }

        timeStepsCounter++
        if (timeStepsCounter >= timeStepsCount) {
            val timeNano = System.nanoTime() - time
            if (printTimeEnabled && timeNano >= ToolsMapper.timeMsToNano(minimumTimeToPrintMs)) {
                val s = "DXX $prefix $timeNano left [$timeStepsCounter steps]"
                addToLog(s)
                printer.invoke(s)
            }
            timeStepsCounter = 0
        }
    }


    fun getTimeMicro(): Long = (System.nanoTime() - time) / 1000


    fun printStack() {
        printStack("DXX Stack trace")
    }

    fun printStack(message: String) {
        logPrint(Throwable(message))
    }

    fun getTimeLeft() = (System.nanoTime() - time) / 1000000

    fun getTimeLeftAndSave(): Long {
        val t = getTimeLeft()
        saveTime()
        return t
    }


    //
    //  Log
    //


    fun info(vararg o: Any?) {
        val s = asLogString("XInfo", *o)
        addToLog(s)
        printerInfo.invoke(s)
    }

    fun logPrint(vararg o: Any?) {
        print(*o)
    }

    fun logPrint(throwable: Throwable) {
        addToLog(getStack(throwable))
        exceptionPrinter.invoke(throwable)
    }

    fun err(vararg o: Any?) {
        print(*o)
    }

    internal fun print(vararg o: Any?) {
        val s = asLogString(*o)
        addToLog(s)
        printer.invoke(s)
    }

    private fun logParams(vararg o: Any) {
        var s = ""
        var i = 0
        while (i + 1 < o.size) {
            s += o[i].toString() + "[" + o[i + 1] + "] "
            i += 2
        }
        if (o.size % 2 == 1)
            s += "...[" + o[o.size - 1]
        logPrint("DXX", s)
    }


    fun logColor(color: Int) {
        logColor(null, color)
    }

    fun logColor(prefix: String?, color: Int) {
        val a = color shr 24 and 0xff
        val r = color shl 8 shr 24 and 0xff
        val g = color shl 16 shr 24 and 0xff
        val b = color shl 24 shr 24 and 0xff

        if (prefix == null) logParams("a", a, "r", r, "g", g, "b", b)
        else logParams(prefix, "", "a", a, "r", r, "g", g, "b", b)
    }

    fun c(v: Array<Any>): String {
        return asString(v, false)
    }

    fun asLogString(vararg o: Any?): String {
        if (o.isEmpty()) return ""

        val s = StringBuilder(o[0].toString() + "")
        for (i in 1 until o.size) s.append(", ").append(o[i])
        for (i in 1 until o.size) if (o[i] is Throwable) logPrint(o[i] as Throwable)

        return s.toString()
    }

    fun asString(v: Array<Any?>): String {
        return asString(v, false)
    }

    fun asString(v: Array<out Any?>?, ignoreNull: Boolean): String {
        val out: StringBuilder
        if (v == null && !ignoreNull)
            out = StringBuilder("Object[] = null")
        else if (v == null)
            out = StringBuilder("")
        else if (v.size == 0)
            out = StringBuilder("Object[] is empty")
        else {
            out = StringBuilder(v[0].toString() + "")
            for (i in 1 until v.size)
                out.append(", ").append(v[i])
        }
        return out.toString()
    }

    fun asString(v: List<*>?): String {
        val out: StringBuilder
        if (v == null)
            out = StringBuilder("List = null")
        else if (v.size == 0)
            out = StringBuilder("List[] is empty")
        else {
            out = StringBuilder(v[0].toString() + "")
            for (i in 1 until v.size)
                out.append(", ").append(v[i])
        }
        return out.toString()
    }

    fun getStack(e: Throwable): String {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}