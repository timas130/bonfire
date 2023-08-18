package com.sup.dev.java.classes.items

class Pair<L, R>(var left: L, var right: R) {
    operator fun set(left: L, right: R) {
        this.left = left
        this.right = right
    }

    override fun toString(): String {
        return "Pair: r[$right] l[$left]"
    }

}