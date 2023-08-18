package com.sup.dev.android.views.support.adapters.pager

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sup.dev.android.views.support.adapters.CardAdapter
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.tools.ToolsClass
import java.util.ArrayList
import kotlin.reflect.KClass

open class PagerCardAdapter : PagerAdapter(), CardAdapter {

    private val holders = ArrayList<Holder>()
    val items: ArrayList<Card> = ArrayList()
    private val viewCash = HashList<Any, View>()
    private var cardW = ViewGroup.LayoutParams.MATCH_PARENT
    private var cardH = ViewGroup.LayoutParams.MATCH_PARENT

    private var notifyCount = 0

    val isEmpty: Boolean
        get() = items.isEmpty()

    val views: ArrayList<View>
        get() {

            val list = ArrayList<View>()
            for (h in holders)
                list.add(h.itemView)

            return list
        }

    //
    //  Adapter
    //

    override fun notifyUpdate() {
        notifyDataSetChanged()
    }

    override fun instantiateItem(parent: ViewGroup, p: Int): Any {
        val holder = getFreeHolder(parent)
        parent.addView(holder.itemView)

        var i = p
        while (i < p + notifyCount && i < items.size) {
            if (items[realPosition(p)] is NotifyItem)
                (items[realPosition(p)] as NotifyItem).notifyItem()
            i++
        }

        val card = items[realPosition(p)]
        val frame = holder.itemView
        holder.item = card
        holder.position = p

        var cardView = viewCash.removeOne(card::class)
        if (cardView == null) cardView = card.instanceView(frame)

        frame.removeAllViews()
        frame.addView(cardView)
        frame.tag = card::class
        (cardView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER

        card.bindCardView(cardView)
        if (frame.width == 0) frame.requestLayout()

        return card
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        for (h in getHoldersForItem(obj)) if (h.itemView == view) return true
        return false
    }

    override fun destroyItem(parent: ViewGroup, position: Int, ob: Any) {
        //  Нужно учитывать, что при уничтожении Holder в указанной позиции, можгут существовать другие Holder с таким-же объектом которые не нужно унитожать.
        //  Это важно для бесконечных Pager.
        for (h in getHoldersForPosition(position)) {
            parent.removeView((h.itemView))
            clearHolder(h)
        }
    }

    override fun getItemPosition(ob: Any): Int {
        val position = indexOf(ob as Card)
        return if (position < 0) POSITION_NONE else position
    }

    override fun getCount(): Int {
        return items.size
    }

    //
    //  Items
    //

    override fun updateAll() {
        for (c in items) c.update()
    }

    override operator fun get(i: Int): Card {
        return items[realPosition(i)]
    }

    override fun <K : Card> get(c: KClass<K>): ArrayList<K> {
        val list = ArrayList<K>()
        for (i in 0 until size())
            if (ToolsClass.instanceOf(get(i)::class, c))
                list.add(get(i) as K)
        return list
    }

    fun add(card: Card) {
        card.setCardAdapter(this)
        items.add(card)
        notifyDataSetChanged()
    }

    override fun add(i: Int, card: Card) {
        card.setCardAdapter(this)
        items.add(i, card)
        notifyDataSetChanged()
    }


    fun set(items: List<Card>) {
        clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun remove(card: Card) {
        if (items.remove(card)) notifyDataSetChanged()
    }

    fun remove(position: Int) {
        val p = realPosition(position)
        val item = items.removeAt(p)
        removeItemFromHolders(item)
        notifyDataSetChanged()
    }

    private fun removeItemFromHolders(item: Card) {
        for (h in holders) if (h.item === item) clearHolder(h)
    }

    private fun clearHolder(h: Holder) {
        if (h.itemView.childCount != 0) viewCash.add(h.itemView.tag, h.itemView.getChildAt(0))
        h.itemView.removeAllViews()
        h.item?.detachView()
        h.item = null
    }

    fun clear() {
        for (i in this.items) removeItemFromHolders(i)
        items.clear()
    }

    override fun indexOf(card: Card): Int {
        return items.indexOf(card)
    }

    override fun indexOf(checker: (Card) -> Boolean): Int {
        for (c in items) if (checker.invoke(c)) return indexOf(c)
        return -1
    }

    override fun <K : Card> find(checker: (Card) -> Boolean): K? {
        for (c in items) if (checker.invoke(c)) return c as K
        return null
    }

    override operator fun contains(card: Card): Boolean {
        return indexOf(card) > -1
    }

    open fun realPosition(position: Int): Int {
        return position
    }

    override fun size(): Int {
        return items.size
    }

    override fun getView(card: Card): View? {
        for (h in holders) if (h.item === card) return h.itemView.getChildAt(0)
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun <K : PagerCardAdapter> setNotifyCount(notifyCount: Int): K {
        this.notifyCount = notifyCount
        return this as K
    }

    override fun isVisible(card: Card): Boolean {
        return getView(card) != null
    }

    fun getViewIfVisible(position: Int): View? {
        val p = realPosition(position)
        for (h in holders)
            if (h.item === items[p])
                return h.itemView
        return null
    }

    fun setCardW(cardW: Int) {
        this.cardW = cardW
    }

    fun setCardH(cardH: Int) {
        this.cardH = cardH
    }

    //
    //  Holder
    //

    fun getHolderForView(view: View): Holder? {
        for (holder in holders) if (holder.itemView.getChildAt(0) == view) return holder
        return null
    }

    fun isItemPosition(item: Any, position: Int): Boolean {
        val p = realPosition(position)
        if (p < 0 || p >= items.size) return false
        return items[p] == item
    }

    fun getHoldersForItem(item: Any): ArrayList<Holder> {
        val list = ArrayList<Holder>()
        for (holder in holders) if (holder.item == item) list.add(holder)
        return list
    }

    private fun getHoldersForPosition(position: Int): ArrayList<Holder> {
        val list = ArrayList<Holder>()
        for (holder in holders) if (holder.position == position) list.add(holder)
        return list
    }

    private fun getFreeHolder(parent: ViewGroup): Holder {
        for (holder in holders) if (holder.itemView.parent == null) return holder
        val holder = Holder(parent.context)
        holders.add(holder)

        return holder
    }

    class Holder constructor(context: Context) {

        var item: Card? = null
        var position = -1
        var itemView: FrameLayout = FrameLayout(context)

    }
}