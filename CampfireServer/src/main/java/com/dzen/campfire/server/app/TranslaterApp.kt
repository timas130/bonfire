package com.dzen.campfire.server.app

import com.sup.dev.java.libs.debug.err
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList


object TranslaterApp {


    fun checkKeys() {
        try {
            val brE = BufferedReader(InputStreamReader(FileInputStream("C:\\InProgress\\Test\\app\\src\\main\\res\\values\\strings.xml")))
            val brR = BufferedReader(InputStreamReader(FileInputStream("C:\\InProgress\\Test\\app\\src\\main\\res\\values-ru\\strings.xml")))

            val keysE = ArrayList<String>()
            val keysR = ArrayList<String>()

            while (brE.ready()) {
                var s = brE.readLine()
                if (s.contains("name") && !s.contains("translatable=\"false\"")) {
                    s = s.substring(s.indexOf("name=\"") + "name=\"".length)
                    s = s.substring(0, s.indexOf("\">"))
                    keysE.add(s)
                }
            }

            while (brR.ready()) {
                var s = brR.readLine()
                if (s.contains("name")) {
                    s = s.substring(s.indexOf("name=\"") + "name=\"".length)
                    s = s.substring(0, s.indexOf("\">"))
                    keysR.add(s)
                }
            }

            for (e in keysE) if (!keysR.contains(e)) print(e)


        } catch (ex: Exception) {
            err(ex)
        }

        System.exit(1)
    }

}
