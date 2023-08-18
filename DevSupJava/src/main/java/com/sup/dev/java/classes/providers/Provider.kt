package com.sup.dev.java.classes.providers

interface Provider<K> {
    fun provide(): K
}