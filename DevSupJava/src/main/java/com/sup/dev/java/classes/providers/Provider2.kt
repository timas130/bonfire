package com.sup.dev.java.classes.providers

interface Provider2<A1, A2, I> {
    fun provide(a1: A1, a2: A2): I
}