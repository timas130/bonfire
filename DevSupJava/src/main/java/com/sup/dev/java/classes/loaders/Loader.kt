package com.sup.dev.java.classes.loaders

import com.sup.dev.java.classes.callbacks.CallbacksList1

abstract class Loader<K> {

    private val callbacksList = CallbacksList1<K>()
    private var item: K? = null
    private var loadInProgress: Boolean = false

    fun get(callback: (K) -> Unit) {
        if (item != null)
            callback.invoke(item!!)
        else {
            callbacksList.add(callback)
            if (!loadInProgress) {
                loadInProgress = true
                load { item ->
                    this.item = item
                    callbacksList.invokeAndClear(item)
                }
            }
        }
    }

    fun getNow() = item

    protected abstract fun load(callback: (K) -> Unit)

}
