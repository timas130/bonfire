package com.sup.dev.java.libs.eventBus

import com.sup.dev.java.classes.Subscribe
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.tools.ToolsThreads
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.ArrayList
import kotlin.reflect.KClass


object EventBus {

    private val subscribersHard = HashList<KClass<*>, Item2<Any, (Any) -> Unit>>()
    private val subscribersWeak = ArrayList<WeakReference<EventBusSubscriber>>()

    private var postMultiProcessCallback: (Serializable) -> Unit = {}

    fun setPostMultiProcessCallback(postMultiProcessCallback: (Serializable) -> Unit) {
        EventBus.postMultiProcessCallback = postMultiProcessCallback
    }

    //
    //  Subscription
    //

    fun <K : Any> subscribeHard(eventClass: KClass<K>, onEvent: (K) -> Unit) {
        subscribeHard(onEvent, eventClass, onEvent)
    }

    @Suppress("UNCHECKED_CAST")
    fun <K : Any> subscribeHard(tag: Any, eventClass: KClass<K>, onEvent: (K) -> Unit) {
        subscribersHard.add(eventClass, Item2(tag, onEvent as (Any) -> Unit))
    }

    fun <K : Any> subscribe(eventClass: KClass<K>, onEvent: (K) -> Unit) = EventBusSubscriber().subscribe(eventClass, onEvent)

    fun subscribe(eventBusSubscriber: EventBusSubscriber) {
        subscribersWeak.add(WeakReference(eventBusSubscriber))
    }

    fun unsubscribe(tag: Any) {

        for (eventClass in subscribersHard.getKeys()) {
            val list = subscribersHard.getAllOriginal(eventClass)
            if (list != null) {
                var i = 0
                while (i < list.size) {
                    if (list[i].a1 === tag)
                        list.removeAt(i--)
                    i++
                }
            }
        }

        var i = 0
        while (i < subscribersWeak.size) {
            val sub = subscribersWeak[i].get()
            if (sub == null || sub.tag === tag)
                subscribersWeak.removeAt(i--)
            i++
        }
    }

    //
    //  Post
    //

    fun post(event: Any) {
        ToolsThreads.main {
            val list = subscribersHard.getAllOriginal(event::class)
            if (list != null) {
                var i = 0
                while (i < list.size) {

                    val callbackSource = list[i].a2

                    if (callbackSource is Subscribe && !(callbackSource as Subscribe).isSubscribed()) {
                        list.removeAt(i--)
                        i++
                        continue
                    }

                    callbackSource.invoke(event)

                    if (callbackSource is Subscribe && !(callbackSource as Subscribe).isSubscribed()) list.removeAt(i--)
                    i++
                }
            }

            val subscribersWeakCopy = ArrayList<WeakReference<EventBusSubscriber>>()
            for (i in subscribersWeak) subscribersWeakCopy.add(i)

            var i = 0
            while (i < subscribersWeakCopy.size) {
                val sub = subscribersWeakCopy[i].get()
                if (sub == null)
                    subscribersWeak.remove(subscribersWeakCopy[i])
                else
                    sub.onEvent(event)
                i++
            }
        }
    }

    fun postMultiprocess(event: Serializable) {
        post(event)
        postMultiProcessCallback.invoke(event)
    }


}
