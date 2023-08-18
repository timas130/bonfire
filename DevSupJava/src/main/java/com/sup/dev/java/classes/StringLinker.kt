package com.sup.dev.java.classes

class StringLinker {
    private val stringBuilder = StringBuilder()
    private var splitter = ""
    fun setSplitter(splitter: String) {
        this.splitter = splitter
    }

    fun add(s: String?) {
        if (!isEmpty) stringBuilder.append(splitter)
        stringBuilder.append(s)
    }

    val isEmpty: Boolean
        get() = stringBuilder.toString().isEmpty()

    override fun toString(): String {
        return stringBuilder.toString()
    }
}