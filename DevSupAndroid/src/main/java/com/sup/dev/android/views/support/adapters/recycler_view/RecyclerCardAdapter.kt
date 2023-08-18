package com.sup.dev.android.views.support.adapters.recycler_view

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.sup.dev.android.models.EventConfigurationChanged
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.CardAdapter
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.java.classes.callbacks.CallbacksList
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsClass
import kotlin.reflect.KClass


open class RecyclerCardAdapter : RecyclerView.Adapter<RecyclerCardAdapter.Holder>(), CardAdapter {

    private val eventBus = EventBus.subscribe(EventConfigurationChanged::class) { updateAll() }

    private val viewCash = HashList<KClass<out Card>, View>()
    private val items = ArrayList<Card>()
    private val holders = ArrayList<Holder>()
    private val onItemsChangeListeners = CallbacksList()
    private var cardW = RecyclerView.LayoutParams.MATCH_PARENT
    private var cardH = RecyclerView.LayoutParams.WRAP_CONTENT

    private var notifyCount = 0

    val isScrolledToLastItem: Boolean
        get() = if (isEmpty) true else isVisible(get(size() - 1))

    val isEmpty: Boolean
        get() = itemCount == 0

    //
    //  Bind
    //

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var i = position
        while (i < position + notifyCount && i < items.size) {
            if (items[i] is NotifyItem)
                (items[i] as NotifyItem).notifyItem()
            i++
        }
        if (holder.item != null) holder.item?.onDetachView()
        removeItemFromHolders(items[position])
        holder.item = items[position]

        val card = items[position]
        val frame = holder.itemView as FrameLayout

        val tag: KClass<out Card>? = frame.tag as KClass<out Card>?

        if (frame.childCount != 0 && tag != null)
            viewCash.add(tag, frame.getChildAt(0))
        frame.removeAllViews()

        var cardView = viewCash.removeOne(card::class)
        if (cardView == null)
            cardView = card.instanceView(frame)


        frame.addView(ToolsView.removeFromParent(cardView))
        frame.tag = card::class

        card.bindCardView(cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = FrameLayout(parent.context)
        view.layoutParams = RecyclerView.LayoutParams(cardW, cardH)
        val holder = Holder(view)
        holders.add(holder)
        return holder
    }

    private fun removeItemFromHolders(item: Card) {
        for (h in holders)
            if (h.item === item) {
                item.detachView()
                h.item = null
            }
    }

    //
    //  Items
    //

    fun add(list: List<Card>) {
        for (card in list)
            add(card)
    }

    fun add(card: Card) {
        add(items.size, card)
    }

    override fun add(p: Int, card: Card) {
        card.setCardAdapter(this)
        items.add(p, card)
        notifyItemInserted(p)
        onItemsChangeListeners.invoke()
    }

    override fun remove(card: Card) {
        card.setCardAdapter(null)
        val position = indexOf(card)
        if (position == -1) return
        remove(position)
    }

    fun remove(c: KClass<out Card>) {
        var i = 0
        while (i < itemCount) {
            if (ToolsClass.instanceOf(get(i)::class, c))
                remove(i--)
            i++
        }
    }

    fun remove(list: List<Card>) {
        for (card in list) remove(card)
    }

    open fun remove(position: Int) {
        val card = items.removeAt(position)
        notifyItemRemoved(position)
        removeItemFromHolders(card)
        onItemsChangeListeners.invoke()
    }

    fun replace(index: Int, o: Card) {
        removeItemFromHolders(items[index])
        items[index] = o
        o.setCardAdapter(this)
        notifyItemChanged(index)
    }

    fun clear() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
        for (h in holders) {
            h.item?.onDetachView()
            h.item = null
        }
    }

    fun containsSame(card: Card): Boolean {
        for (i in items) if (i == card) return true
        return false
    }

    override fun updateAll() {
        for (i in getAll()) i.update()
    }

    //
    //  Listeners
    //

    fun addItemsChangeListener(callback: () -> Unit) {
        onItemsChangeListeners.add(callback)
    }

    fun removeItemsChangeListener(callback: () -> Unit) {
        onItemsChangeListeners.remove(callback)
    }

    //
    //  Notify
    //

    fun notifyAllChanged() {
        notifyItemRangeChanged(0, itemCount)
    }

    //
    //  Setters
    //

    open fun setNotifyCount(notifyCount: Int): RecyclerCardAdapter {
        this.notifyCount = notifyCount
        return this
    }

    fun setCardW(cardW: Int) {
        this.cardW = cardW
    }

    fun setCardH(cardH: Int) {
        this.cardH = cardH
    }

    //
    //  Getters
    //

    fun directItems() = items

    override fun isVisible(card: Card): Boolean {
        return getView(card) != null
    }

    override fun notifyUpdate() {
      super.notifyDataSetChanged()
    }

    fun isScrolledToLastItems(count: Int): Boolean {
        if (size() < count) return true
        for (i in 0 until count)
            if (isVisible(get(size() - count)))
                return true
        return false
    }

    fun contains(c: KClass<out Card>): Boolean {
        for (i in 0 until itemCount)
            if (ToolsClass.instanceOf(get(i)::class, c))
                return true
        return false
    }

    fun size(c: KClass<out Card>): Int {
        var x = 0
        for (i in 0 until itemCount)
            if (ToolsClass.instanceOf(get(i)::class, c))
                x++
        return x
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun size(): Int {
        return itemCount
    }

    override operator fun get(index: Int): Card {
        return items[index]
    }

    fun getOrNull(index: Int): Card? {
        return items.getOrNull(index)
    }

    override fun indexOf(o: Card): Int {
        return items.indexOf(o)
    }

    override fun indexOf(checker: (Card) -> Boolean): Int {
        for (c in items) if (checker.invoke(c)) return indexOf(c)
        return -1
    }

    override fun <K : Card> find(checker: (Card) -> Boolean): K? {
        for (c in items) if (checker.invoke(c)) return c as K
        return null
    }

    override operator fun contains(o: Card): Boolean {
        return items.contains(o)
    }

    fun getByTag(tag: Any?): ArrayList<Card> {
        val list = ArrayList<Card>()
        for (i in 0 until itemCount)
            if (get(i).tag == null && tag == null || tag != null && get(i).tag != null && get(i).tag!!.equals(tag))
                list.add(get(i))
        return list
    }

    override fun <K : Card> get(c: KClass<K>): ArrayList<K> {
        val list = ArrayList<K>()
        for (i in 0 until itemCount)
            if (ToolsClass.instanceOf(get(i)::class, c))
                list.add(get(i) as K)
        return list
    }

    fun getAll() = Array(itemCount) { get(it) }

    override fun getView(item: Card): View? {
        var view: View? = null

        for (h in holders)
            if (h.item === item)
                view = h.itemView

        if (view != null) {
            val frame = view as FrameLayout?
            if (frame!!.childCount == 1)
                return frame.getChildAt(0)
        }

        return null
    }

    //
    //  Holder
    //

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var item: Card? = null
    }

}
