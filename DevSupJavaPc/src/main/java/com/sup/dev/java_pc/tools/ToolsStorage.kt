package com.sup.dev.java_pc.tools

import com.sup.dev.java.tools.ToolsFiles
import java.io.File
import java.io.IOException


object ToolsStorage {

    @Throws(IOException::class)
    fun readString(file: String): String {
        val br = ToolsFiles.br(file)

        val s = StringBuilder()
        while (br.ready())
            s.append(br.readLine())

        return s.toString()
    }


    fun write(s: String, file: String) {

        try {
            val f = File(file)
            if (!f.exists()) {
                if (f.parentFile != null)
                    f.parentFile.mkdirs()
                f.createNewFile()
            }

            val bw = ToolsFiles.bw(file)
            bw.write(s)
            bw.flush()
            bw.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

}
