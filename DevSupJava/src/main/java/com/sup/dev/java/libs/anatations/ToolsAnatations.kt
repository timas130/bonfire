package com.sup.dev.java.libs.anatations

import java.io.File
import java.util.ArrayList
import java.util.jar.JarFile

object ToolsAnatations {

    fun findAnnotatedClasses(an: Class<out Annotation>, file: File): ArrayList<Class<*>> {
        val ret = ArrayList<Class<*>>()

        if (file.isDirectory)
            scanDirectory("", file, an, ret)
        else
            scanJar(file, an, ret)

        return ret
    }

    private fun scanDirectory(pkg: String, dir: File, an: Class<out Annotation>, entries: ArrayList<Class<*>>) {
        val contents = dir.listFiles()
        for (f in contents) {
            if (f.isDirectory) {
                scanDirectory(if (pkg.length > 0) pkg + '.'.toString() + f.name else f.name, f, an, entries)
            } else if (f.name.endsWith(".class") || f.name.endsWith(".java") || f.name.endsWith(".kt")) {
                val clName = pkg + '.'.toString() + f.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                try {
                    val cl = Thread.currentThread().contextClassLoader.loadClass(clName)
                    if (cl.isAnnotationPresent(an) && !entries.contains(cl))
                        entries.add(cl)
                } catch (ignored: Throwable) {

                }

            }


        }
    }

    private fun scanJar(file: File, an: Class<out Annotation>, ret: ArrayList<Class<*>>) {

        try {
            JarFile(file).use { jar ->

                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".class")) {
                        var name = entry.name.substring(0, entry.name.length - 6)
                        name = name.replace('/', '.')
                        try {
                            val cl = Thread.currentThread().contextClassLoader.loadClass(name)
                            if (cl.isAnnotationPresent(an) && !ret.contains(cl)) ret.add(cl)
                        } catch (ignored: Throwable) {

                        }

                    }
                }
            }
        } catch (ignored: Throwable) {

        }

    }

}