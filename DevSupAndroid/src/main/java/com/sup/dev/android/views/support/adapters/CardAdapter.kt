package com.sup.dev.android.views.support.adapters

import android.view.View
import com.sup.dev.android.views.cards.Card
import java.util.ArrayList
import kotlin.reflect.KClass

interface CardAdapter {

    fun getView(card: Card): View?

    fun remove(card: Card)

    fun indexOf(card: Card): Int

    fun indexOf(checker:(Card)->Boolean): Int

    fun <K : Card>find(checker:(Card)->Boolean): K?

    fun size(): Int

    fun  <K : Card> get(c: KClass<K>): ArrayList<K>

    operator fun get(i: Int): Card

    operator fun contains(card: Card): Boolean

    fun add(i: Int, card: Card)

    fun isVisible(card: Card): Boolean

    fun updateAll()

    fun notifyUpdate()
}

