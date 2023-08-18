package com.sup.dev.android.tools

import android.graphics.Bitmap
import android.os.Environment
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ToolsFilesAndroid {

    //
    //  Files
    //

    @Throws(IOException::class)
    fun writeFile(patch: String, bytes: ByteArray): File {

        val file = File(patch)
        val parent = file.parentFile
        if (parent != null && !parent.exists())
            parent.mkdirs()

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(bytes)
            out.close()
        } finally {
            try {
                out?.close()
            } catch (ex: IOException) {
                err(ex)
            }

        }

        return file
    }

    @Throws(IOException::class)
    fun readAsZip(filePath: String): ByteArray {
        val bytesOutputStream = ByteArrayOutputStream()
        val zip = ZipOutputStream(bytesOutputStream)

        addFolderToZip("", filePath, zip)

        zip.flush()
        zip.close()

        return bytesOutputStream.toByteArray()
    }

    @Throws(IOException::class)
    private fun addFileToZip(path: String, srcFile: String, zip: ZipOutputStream, flag: Boolean) {
        val folder = File(srcFile)

        if (flag) {
            zip.putNextEntry(ZipEntry(path + "/" + folder.name + "/"))
        } else {
            if (folder.isDirectory) {
                addFolderToZip(path, srcFile, zip)
            } else {

                val buf = ByteArray(1024)
                var len: Int
                val inputStream = FileInputStream(srcFile)
                zip.putNextEntry(ZipEntry(path + "/" + folder.name))
                while (true) {
                    len = inputStream.read(buf)
                    if(len < 1)break
                    zip.write(buf, 0, len)
                }

            }
        }
    }

    @Throws(IOException::class)
    private fun addFolderToZip(path: String, srcFolder: String, zip: ZipOutputStream) {
        val folder = File(srcFolder)

        if (folder.list()!!.size == 0) {
            println(folder.name)
            addFileToZip(path, srcFolder, zip, true)
        } else {
            for (fileName in folder.list()!!) {
                if (path == "")
                    addFileToZip(folder.name, "$srcFolder/$fileName", zip, false)
                else
                    addFileToZip(path + "/" + folder.name, "$srcFolder/$fileName", zip, false)
            }
        }
    }

    @Throws(IOException::class)
    fun unpackZip(path: String, zipFile: File) {
        unpackZip(path, FileInputStream(zipFile))
    }

    @Throws(IOException::class)
    fun unpackZip(path: String, zipFile: ByteArray) {
        unpackZip(path, ByteArrayInputStream(zipFile))
    }

    @Throws(IOException::class)
    fun unpackZip(path: String, inp: InputStream) {
        var zis: ZipInputStream? = null
        try {
            var filename: String
            zis = ZipInputStream(BufferedInputStream(inp))
            var ze: ZipEntry?
            val buffer = ByteArray(1024)
            var count: Int
            while (true) {
                ze = zis.nextEntry
                if (ze == null) break
                filename = ze.name
                val file = File(path + filename)
                if (ze.isDirectory) continue

                val parentFile = file.parentFile
                parentFile?.mkdirs()

                val fos = FileOutputStream(path + filename)
                while (true) {
                    count = zis.read(buffer)
                    if (count == -1) break
                    fos.write(buffer, 0, count)
                }
                fos.close()
                zis.closeEntry()
            }
            zis.close()
        } finally {
            try {
                inp.close()
            } catch (ex: IOException) {
                err(ex)
            }

            try {
                zis?.close()
            } catch (ex: IOException) {
                err(ex)
            }

        }
    }

    //
    //  Bitmap
    //

    fun saveImageInCameraFolder(bitmap: Bitmap, onResult: (String)->Unit, onPermissionPermissionRestriction: (String)->Unit={}) {
        ToolsPermission.requestWritePermission({
            val file = createJpgFileInCameraFolder()
            writeBitmap(bitmap, file)
            onResult.invoke(file.absolutePath)
        }, onPermissionPermissionRestriction)
    }

    private fun createJpgFileInCameraFolder(): File {

        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        storageDir.mkdir()
        val fileName = "/Camera/" + System.currentTimeMillis() + ".jpg"
        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        return File(storageDir, fileName)
    }

    private fun writeBitmap(bitmap: Bitmap, file: File) {

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (ex: Exception) {
            err(ex)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ex: IOException) {
                    err(ex)
                }

            }
        }
    }

    //
    //  Getters
    //

    fun getDiskCacheDir(): File {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            val externalCacheDir = SupAndroid.appContext!!.externalCacheDir
            if (externalCacheDir != null)
                return File(SupAndroid.appContext!!.externalCacheDir!!.path)
        }

        return File(SupAndroid.appContext!!.cacheDir.path)
    }
}
