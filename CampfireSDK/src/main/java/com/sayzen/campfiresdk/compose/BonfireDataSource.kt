package com.sayzen.campfiresdk.compose

import android.util.Log
import androidx.annotation.CallSuper
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BonfireDataSource<T : JsonParsable>(protected var data: T) {
    protected val subscriber = EventBus
        .subscribe(EventNotification::class) {
            handleNotification(it)
        }

    @Suppress("PropertyName")
    protected val _flow = MutableStateFlow(data)
    val flow = _flow.asStateFlow()

    open fun handleNotification(ev: EventNotification) {}

    open fun edit(cond: Boolean, editor: T.() -> Unit) {
        if (!cond) return

        Log.d("BonfireDataSource", "editing type=${data.javaClass.simpleName}")

        data.editor()

        // fixme: fuck this...
        val json = data.json(true, Json())

        val copy = data.javaClass.getConstructor().newInstance()
        copy.json(false, json)

        data = copy
        _flow.tryEmit(copy)
    }

    @CallSuper
    open fun destroy() {
        subscriber.unsubscribe()
    }
}
