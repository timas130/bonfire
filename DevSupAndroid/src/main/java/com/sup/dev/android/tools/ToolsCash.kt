package com.sup.dev.android.tools

import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

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

            var os: FileOutputStream? = null
            try {
                os = FileOutputStream(file)
                os.write(data)
                os.flush()
            } catch (ex: IOException) {
                err(ex)
            } finally {
                if (os != null)
                    try {
                        os.close()
                    } catch (e: IOException) {
                        err(e)
                    }

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

                val listFiles = cacheDir.listFiles()
                Arrays.sort(listFiles) { i1, i2 -> (i1.lastModified() - i2.lastModified()).toInt() }
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

        if (!file.exists()) return null

        val data = ByteArray(file.length().toInt())
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(file)
            inputStream.read(data)
            file.setLastModified(System.currentTimeMillis())
        } catch (ex: Exception) {
            err(ex)
        } finally {
            try {
                inputStream!!.close()
            } catch (e: Exception) {
                err(e)
            }

        }
        return data
    }

    fun clear(name: String) {
        File(SupAndroid.appContext!!.cacheDir, name).delete()
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