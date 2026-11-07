package com.sup.dev.android.tools

import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

object ToolsCash {

    private var maxCashSize = 1024L * 1024 * 20
    private var overClearSize = 1024L * 1024 * 20

    fun init(cashSize: Long) {
        init(cashSize, cashSize)
    }

    fun init(cashSize: Long, overClearSize: Long) {
        maxCashSize = cashSize
        ToolsCash.overClearSize = overClearSize
    }

    fun put(data: ByteArray, name: String, immortal: Boolean=false): File? {

        try {
            clearIfNeed()

            val cacheDir = if (immortal) SupAndroid.appContext!!.filesDir else SupAndroid.appContext!!.cacheDir

            val file = File(cacheDir, name)

            val tmp = File.createTempFile("tmp_$name", ".tmp", cacheDir)
            try {
                FileOutputStream(tmp).use { it.write(data) }
                if (!tmp.renameTo(file)) throw IOException("Can't move ${tmp.name} to $name")
            } catch (e: Exception) {
                tmp.delete()
                throw e
            }

            return file
        } catch (e: Exception) {
            err(e)
        }
        return null
    }

    fun clearIfNeed() {
        try {
            val cacheDir = SupAndroid.appContext!!.cacheDir
            var cashSize = getDirSize(cacheDir)
            if (cashSize > overClearSize + maxCashSize) {

                val listFiles = cacheDir.listFiles()!!.map { it to it.lastModified() }.sortedBy { it.second }.map { it.first }
                var index = 0

                while (cashSize > maxCashSize && index < listFiles.size) {
                    cashSize -= listFiles[index].length()
                    try {
                        listFiles[index].delete()
                    } catch (e: Exception) {
                        err(e)
                    }
                    index++
                }

            }
        } catch (e: Exception) {
            err(e)
        }
    }


    fun get(name: String) = get(name, SupAndroid.appContext!!.cacheDir)?: get(name, SupAndroid.appContext!!.filesDir)

    private fun get(name: String, cacheDir:File): ByteArray? {
        val file = File(cacheDir, name)

        if (!file.exists() || file.length() == 0L) return null

        try {
            val data = file.readBytes()
            file.setLastModified(System.currentTimeMillis())
            return data
        } catch (ex: Exception) {
            err(ex)
            return null
        }
    }

    fun clear(name: String) {
        File(SupAndroid.appContext!!.cacheDir, name).delete()
        File(SupAndroid.appContext!!.filesDir, name).delete()
    }

    private fun getDirSize(dir: File): Long {
        var size: Long = 0
        val files = dir.listFiles()

        for (file in files!!)
            if (file.isFile)
                size += file.length()

        return size
    }

}