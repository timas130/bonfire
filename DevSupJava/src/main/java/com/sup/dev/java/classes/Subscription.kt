package com.sup.dev.java.classes

open class Subscription : Subscribe {

    private var subscribed:Boolean = true

    override fun isSubscribed(): Boolean {
        return subscribed
    }

    fun unsubscribe() {
        subscribed = false
    }

}
