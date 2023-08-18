package com.sup.dev.android.views.support.adapters.pager

import android.view.ViewGroup
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsThreads
import java.util.ArrayList

class PagerCardAdapterLoader<X>(private val loader: ((Array<X>) -> Unit, ArrayList<Card>) -> Unit, private val mapper: (X)->Card) : PagerCardAdapter() {

    private var startLoadOffset = 0
    //
    //  Getters
    //

    var isLock: Boolean = false
        private set
    var isInProgress: Boolean = false
        private set
    private var onErrorAndEmpty: (()->Unit)? = null
    private var onEmpty: (()->Unit)? = null
    private var onLoadingAndEmpty: (()->Unit)? = null
    private var onLoadingAndNotEmpty: (()->Unit)? = null
    private var onLoadedNotEmpty: (()->Unit)? = null

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val o = super.instantiateItem(parent, position)

        if (isLock || isInProgress) return o

        if (position >= size() - 1 - startLoadOffset) {
            isInProgress = true
            ToolsThreads.main(true, ({ this.loadNow() }))
        }

        return o
    }


    fun load() {
        if (isInProgress) return
        isInProgress = true
        loadNow()
    }

    private fun loadNow() {
        isLock = false

        if (isEmpty) {
            if (onLoadingAndEmpty != null) onLoadingAndEmpty!!.invoke()
        } else {
            if (onLoadingAndNotEmpty != null) onLoadingAndNotEmpty!!.invoke()
        }

        loader.invoke(({ this.onLoaded(it) }), items)
    }

    private fun onLoaded(result: Array<X>?) {

        isInProgress = false
        val isEmpty = isEmpty

        if (result == null) {
            if (isEmpty && onErrorAndEmpty != null) onErrorAndEmpty!!.invoke()
            return
        }


        if (isEmpty && result.isEmpty()) {
            lock()
            if (onEmpty != null) onEmpty!!.invoke()
        } else {
            if (result.isEmpty()) lock()
        }

        for (aResult in result) add(mapper.invoke(aResult))

        if (!isEmpty || result.isNotEmpty())
            if (onLoadedNotEmpty != null) onLoadedNotEmpty!!.invoke()

    }

    fun reload() {
        load()
    }


    fun lock() {
        isLock = true
    }

    fun unlock() {
        isLock = false
    }

    //
    //  Setters
    //


    fun setOnLoadedNotEmpty(onLoadedNotEmpty: ()->Unit): PagerCardAdapterLoader<X> {
        this.onLoadedNotEmpty = onLoadedNotEmpty
        return this
    }

    fun setOnLoadingAndNotEmpty(onLoadingAndNotEmpty: ()->Unit): PagerCardAdapterLoader<X> {
        this.onLoadingAndNotEmpty = onLoadingAndNotEmpty
        return this
    }

    fun setOnLoadingAndEmpty(onLoadingAndEmpty: ()->Unit): PagerCardAdapterLoader<X> {
        this.onLoadingAndEmpty = onLoadingAndEmpty
        return this
    }

    fun setOnEmpty(onEmpty: ()->Unit): PagerCardAdapterLoader<X> {
        this.onEmpty = onEmpty
        return this
    }

    fun setOnErrorAndEmpty(onErrorAndEmpty: ()->Unit): PagerCardAdapterLoader<X> {
        this.onErrorAndEmpty = onErrorAndEmpty
        return this
    }

    fun setStartLoadOffset(startLoadOffset: Int): PagerCardAdapterLoader<X> {
        this.startLoadOffset = startLoadOffset
        return this
    }

}
