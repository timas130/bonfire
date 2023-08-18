package com.sup.dev.java.tools

import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.log
import java.util.*
import kotlin.reflect.KClass

object ToolsClass {

    fun instanceOf(child: KClass<*>, parent: Any): Boolean {
        return instanceOf(child.java, parent.javaClass)
    }

    fun instanceOf(child: Class<*>, parent: Any): Boolean {
        return instanceOf(child, parent.javaClass)
    }

    fun instanceOf(child: Any, parent: Any): Boolean {
        return instanceOf(child.javaClass, parent.javaClass)
    }

    fun instanceOf(child: Any, parent: KClass<*>): Boolean {
        return instanceOf(child.javaClass, parent.java)
    }

    fun instanceOf(child: Any, parent: Class<*>): Boolean {
        return instanceOf(child.javaClass, parent)
    }

    fun instanceOf(child: KClass<*>, parent: KClass<*>): Boolean {
        return instanceOf(child.java, parent.java)
    }

    fun instanceOf(child: Class<*>, parent: Class<*>): Boolean {
        return (instanceOfClass(child, parent)
                || instanceOfInterface(child, parent)
                || child.componentType != null && parent.componentType != null && instanceOfClass(child.componentType!!, parent.componentType!!)
                || child.componentType != null && parent.componentType != null && instanceOfInterface(child.componentType!!, parent.componentType!!))

    }

    //
    //  Class
    //

    fun instanceOfClass(child: Class<*>, parent: Any): Boolean {
        return instanceOfClass(child, parent.javaClass)
    }

    fun instanceOfClass(child: KClass<*>, parent: Any): Boolean {
        return instanceOfClass(child.java, parent.javaClass)
    }

    fun instanceOfClass(child: Any, parent: Any): Boolean {
        return instanceOfClass(child.javaClass, parent.javaClass)
    }

    fun instanceOfClass(child: Any, parent: Class<*>): Boolean {
        return instanceOfClass(child.javaClass, parent)
    }

    fun instanceOfClass(child: Any, parent: KClass<*>): Boolean {
        return instanceOfClass(child.javaClass, parent.java)
    }

    fun instanceOfClass(child: KClass<*>, parent: KClass<*>): Boolean {
        return instanceOfClass(child.java, parent.java)
    }

    fun instanceOfClass(child: Class<*>, parent: Class<*>): Boolean {
        if (child == parent) return true
        var c: Class<*>? = child.superclass
        while (c != null) {
            if (c == parent) return true
            c = c.superclass
        }
        return false
    }

    //
    //  Interface
    //

    fun instanceOfInterface(child: Class<*>, parent: Any): Boolean {
        return instanceOfInterface(child, parent.javaClass)
    }

    fun instanceOfInterface(child: KClass<*>, parent: Any): Boolean {
        return instanceOfInterface(child.java, parent.javaClass)
    }

    fun instanceOfInterface(child: Any, parent: Class<*>): Boolean {
        return instanceOfInterface(child.javaClass, parent)
    }

    fun instanceOfInterface(child: Any, parent: KClass<*>): Boolean {
        return instanceOfInterface(child.javaClass, parent.java)
    }

    fun instanceOfInterface(child: Any, parent: Any): Boolean {
        return instanceOfInterface(child.javaClass, parent.javaClass)
    }

    fun instanceOfInterface(child: KClass<*>, parent: KClass<*>): Boolean {
        return instanceOfInterface(child.java, parent.java)
    }

    fun instanceOfInterface(child: Class<*>, parent: Class<*>): Boolean {
        for (c in child.interfaces) {
            if (c == parent)
                return true
            if (instanceOf(c, parent))
                return true
        }
        return child.superclass != null && instanceOfInterface(child.superclass!!, parent)
    }

    fun thisInstanceOfInterface(child: KClass<*>, parent: KClass<*>): Boolean {
        return thisInstanceOfInterface(child.java, parent.java)
    }

    fun thisInstanceOfInterface(child: Class<*>, parent: KClass<*>): Boolean {
        return thisInstanceOfInterface(child, parent.java)
    }

    fun thisInstanceOfInterface(child: KClass<*>, parent: Class<*>): Boolean {
        return thisInstanceOfInterface(child.java, parent)
    }

    fun thisInstanceOfInterface(child: Class<*>, parent: Class<*>): Boolean {
        return thisInstanceOfInterface_getSuperclass(child, parent) != null
    }

    fun thisInstanceOfInterface_getSuperclass(child: Class<*>, parent: Class<*>): Class<*>? {
        for (c in child.interfaces)
            if (c == parent)
                return child

        if(child.superclass != null && child.superclass != Objects::class.java)
            return thisInstanceOfInterface_getSuperclass(child.superclass, parent)

        return null
    }


}