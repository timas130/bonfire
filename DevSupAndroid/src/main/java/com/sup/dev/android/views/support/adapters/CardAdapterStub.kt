package com.sup.dev.android.views.support.adapters

import com.sup.dev.android.views.cards.Card
import java.lang.IndexOutOfBoundsException
import java.util.ArrayList
import kotlin.reflect.KClass

class CardAdapterStub : CardAdapter {

    companion object{

        val INSTANCE = CardAdapterStub()

    }

    override fun getView(card: Card) = null

    override fun remove(card: Card) {

    }

    override fun indexOf(card: Card) = -1

    override fun indexOf(checker: (Card) -> Boolean) = -1

    override fun <K : Card> find(checker: (Card) -> Boolean)  = null

    override fun size() = 0
    override fun <K : Card> get(c: KClass<K>): ArrayList<K> {
        return ArrayList()
    }

    override fun get(i: Int): Card {
       throw IndexOutOfBoundsException()
    }

    override fun contains(card: Card) = false

    override fun add(i: Int, card: Card) {
    }

    override fun isVisible(card: Card) = false

    override fun notifyUpdate() {

    }

    override fun updateAll() {

    }
}