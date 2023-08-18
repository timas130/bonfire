package com.sup.dev.java.classes.providers

interface Provider1<A, I> {
    fun provide(a: A): I
}