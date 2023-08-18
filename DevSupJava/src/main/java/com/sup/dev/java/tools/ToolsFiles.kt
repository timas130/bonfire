package com.sup.dev.java.tools

import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.err
import java.io.*
import java.nio.charset.Charset
import kotlin.collections.ArrayList

object ToolsFiles {

    fun readFileSalient(file: String): ByteArray? {
        return readFileSalient(File(file))
    }

    fun readFileSalient(file: File): ByteArray? {
        return readFileSalient(file, 0)
    }

    fun readFileSalient(file: File, bytesCount: Int = 0): ByteArray? {
        try {
            return readFile(file, bytesCount)
        } catch (e: IOException) {
            return null
        }

    }

    fun readFile(file: String) = readFile(File(file))

    fun readFile(file: File) = readFile(file, 0)

    @Throws(IOException::class)
    fun readFile(file: File, bytesCount: Int = 0): ByteArray {
        try {
            val inputStream = DataInputStream(FileInputStream(file))
            val bytes = ByteArray(if (bytesCount < 1 || bytesCount > inputStream.available()) inputStream.available() else bytesCount)
            inputStream.read(bytes, 0, bytes.size)
            inputStream.close()
            return bytes
        } catch (t: Throwable) {
            err("!!! FILE PATCH [${file.absolutePath}]")
            throw t
        }
    }

    @Throws(FileNotFoundException::class)
    fun br(path: String): BufferedReader {
        return BufferedReader(InputStreamReader(FileInputStream(path), Charset.forName("UTF8")))
    }

    @Throws(FileNotFoundException::class)
    fun bw(path: String): BufferedWriter {
        return BufferedWriter(OutputStreamWriter(FileOutputStream(path), Charset.forName("UTF8")))
    }

    fun readLineOrNull(file: File, l: Long = 0):String?{
        return try{
            readLine(file.absolutePath, l)
        }catch (e:Exception){
            null
        }
    }

    fun readLine(file: File, l: Long = 0) = readLine(file.absolutePath, l)

    fun readLine(path: String, l: Long = 0): String {
        var lV = l
        var br: BufferedReader? = null
        try {
            br = br(path)
            while (lV-- != 0L) br.readLine()

            return br.readLine()

        } catch (e: Exception) {
            if (e is FileNotFoundException)
                Debug.print("file [" + File(path).absolutePath + "]")
            throw RuntimeException(e)
        } finally {
            try {
                br?.close()
            } catch (ignored: IOException) {

            }

        }
    }

    fun readListOrNull(path: String): ArrayList<String>? {
        return try{
            readList(path)
        }catch (e:Exception){
            null
        }
    }

    fun readList(path: String): ArrayList<String> {
        val list = ArrayList<String>()
        var br: BufferedReader? = null
        try {

            br = br(path)
            while (br.ready())
                list.add(br.readLine())

        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                br?.close()
            } catch (ignored: IOException) {

            }

        }

        return list
    }

    fun readString(path: String): String {
        val s = StringBuilder()
        var br: BufferedReader? = null
        try {

            br = br(path)
            while (br.ready())
                s.append(br.readLine())

        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                br?.close()
            } catch (ignored: IOException) {

            }

        }

        return s.toString()
    }


    // pos: -1 to end
    fun replaceLine(path: String, pos: Long, value: String) {

        val tmpFileName = path + "x" + System.currentTimeMillis()

        var br: BufferedReader? = null
        var bw: BufferedWriter? = null
        try {
            br = br(path)
            bw = bw(tmpFileName)

            var added = false
            var line: String
            var p = 0
            while (br.ready()) {
                line = br.readLine()
                if (p++.toLong() == pos) {
                    line = value
                    added = true
                }
                bw.write(line + "\n")
            }

            if (!added)
                bw.write(value + "\n")

        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                br?.close()
            } catch (ignored: IOException) {

            }

            try {
                bw?.close()
            } catch (ignored: IOException) {

            }

        }

        val oldFile = File(path)
        oldFile.delete()


        val newFile = File(tmpFileName)
        newFile.renameTo(oldFile)
    }

    fun addLines(path: String, pos: Long, vararg value: String) {

        val tmpFileName = path + "x" + System.currentTimeMillis()

        var br: BufferedReader? = null
        var bw: BufferedWriter? = null
        try {
            br = br(path)
            bw = bw(tmpFileName)

            var added = false
            var line: String
            var p = 0
            while (br.ready()) {
                line = br.readLine()
                if (p++.toLong() == pos) {
                    for(v in value) bw.write(v + "\n")
                    added = true
                }
                bw.write(line + "\n")
            }

            if (!added)
                for(v in value) bw.write(v + "\n")

        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                br?.close()
            } catch (ignored: IOException) {

            }

            try {
                bw?.close()
            } catch (ignored: IOException) {

            }

        }

        val oldFile = File(path)
        oldFile.delete()


        val newFile = File(tmpFileName)
        newFile.renameTo(oldFile)
    }

    @Throws(IOException::class)
    fun copyFile(source: String, dest: String) {
        copyFile(File(source), File(dest))
    }

    @Throws(IOException::class)
    fun copyFile(source: File, dest: File) {
        var iStream: InputStream? = null
        var os: OutputStream? = null
        try {
            iStream = FileInputStream(source)
            os = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length: Int = iStream.read(buffer)
            while (length > 0) {
                os.write(buffer, 0, length)
                length = iStream.read(buffer)
            }
        } finally {
            iStream!!.close()
            os!!.close()
        }
    }

    fun delete(fileOrDirectory: File) {
        if (!fileOrDirectory.exists()) return
        if (fileOrDirectory.isDirectory)
            for (file in fileOrDirectory.listFiles())
                delete(file)
        fileOrDirectory.delete()
    }

    /* fun getDiskCacheDir(): File? {
         if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
             val externalCacheDir: File = SupAndroid.appContext!!.getExternalCacheDir()
             if (externalCacheDir != null) return File(SupAndroid.appContext!!.getExternalCacheDir().getPath())
         }
         return File(SupAndroid.appContext!!.getCacheDir().getPath())
     }*/

}
