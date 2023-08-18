package com.sup.dev.java.libs.eventBus

import com.sup.dev.java.classes.Subscribe
import java.io.Serializable
import java.util.HashMap
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
class EventBusSubscriber {

    //
    //  Getters
    //

    val tag: Any
    private val subscriptions = HashMap<KClass<*>, (Any) -> Unit>()

    private var subscribed: Boolean = false

    internal constructor() {
        this.tag = this
    }

    internal constructor(tag: Any) {
        this.tag = tag
    }

    //
    //  Methods
    //

    fun <K : Any> subscribe(eventClass: KClass<K>, onEvent: (K) -> Unit): EventBusSubscriber {
        subscriptions[eventClass] = onEvent as (Any) -> Unit
        if (!subscribed) {
            subscribed = true
            EventBus.subscribe(this)
        }
        return this
    }

    fun post(event: Any) {
        EventBus.post(event)
    }

    fun postMultiProcess(event: Serializable) {
        EventBus.post(event)
    }

    fun unsubscribe() {
        subscribed = false
        EventBus.unsubscribe(this)
    }

    fun unsubscribe(eventClass: KClass<*>) {
        subscriptions.remove(eventClass)
        if (subscriptions.isEmpty()) unsubscribe()
    }

    //
    //  Internal
    //

    fun onEvent(event: Any) {

        val classes = subscriptions.keys
        for (cl in classes) {
            if (cl == event::class) {
                val callbackSource = subscriptions[cl]

                if (callbackSource is Subscribe && !(callbackSource as Subscribe).isSubscribed()) {
                    subscriptions.remove(cl)
                    continue
                }

                callbackSource!!.invoke(event)

                if (callbackSource is Subscribe && !(callbackSource as Subscribe).isSubscribed()) subscriptions.remove(cl)

            }
        }
    }
}
