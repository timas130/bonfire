package com.sup.dev.android.magic_box

import android.os.Environment
import com.sup.dev.java.libs.debug.err
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


object Miui {

    val isMiui: Boolean
        get() = buildProperties!!.containsKey("ro.miui.ui.version.name")

    private val BUILD_PROP_FILE = File(Environment.getRootDirectory(), "build.prop")
    private var sBuildProperties: Properties? = null
    private val sBuildPropertiesLock = Any()

    private val buildProperties: Properties?
        get() {
            synchronized(sBuildPropertiesLock) {
                if (sBuildProperties == null) {
                    sBuildProperties = Properties()
                    var fis: FileInputStream? = null
                    try {
                        fis = FileInputStream(BUILD_PROP_FILE)
                        sBuildProperties!!.load(fis)
                    } catch (e: IOException) {
                        err(e)
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close()
                            } catch (e: IOException) {
                                err(e)
                            }

                        }
                    }
                }
            }
            return sBuildProperties
        }

}
