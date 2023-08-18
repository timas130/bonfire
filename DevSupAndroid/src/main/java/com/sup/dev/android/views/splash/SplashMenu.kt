package com.sup.dev.android.views.splash

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewIcon

open class SplashMenu : SplashRecycler() {

    val myAdapter: RecyclerCardAdapter = RecyclerCardAdapter()
    private var onGlobalSelected: (SplashMenu, String?) -> Unit = { _, _ -> }

    private var prefCount = 0
    private var autoHide = true

    init {
        vRecycler.layoutManager = LinearLayoutManager(view.context)
        setAdapter<SplashRecycler>(myAdapter)
        vRoot.minimumWidth = ToolsView.dpToPx(256).toInt()
        vRecycler.itemAnimator = null
    }

    override fun onShow() {
        vRecycler.isVerticalScrollBarEnabled = true
        super.onShow()
        finishItemBuilding()
        iconBuilder?.finishItemBuilding()

        for (c in myAdapter.get(CardSpoiler::class)) if (c.cards.isEmpty() && !c.isHasOnExpandChangedCallback()) myAdapter.remove(c)
    }

    fun setAutoHide(b: Boolean): SplashMenu {
        autoHide = b
        return this
    }

    override fun setTitle(title: Int): SplashMenu {
        super.setTitle(title)
        return this
    }

    override fun setTitle(title: String?): SplashMenu {
        super.setTitle(title)
        return this
    }

    override fun setTitleBackgroundColor(color: Int): SplashMenu {
        super.setTitleBackgroundColor(color)
        return this
    }

    override fun setTitleBackgroundColorRes(color: Int): SplashMenu {
        super.setTitleBackgroundColorRes(color)
        return this
    }

    fun addTitleView(view: View): SplashMenu {
        myAdapter.add(0, CardView(view))
        return this
    }

    //
    //  Item
    //

    private var buildItem: Item? = null
    private var skipThisItem = false
    private var skipGroup = false
    private var spoiler: CardSpoiler? = null

    fun getMenuItem(index: Int): CardMenu {
        return myAdapter.get(index) as CardMenu
    }

    fun setItemVisible(index: Int, visible: Boolean) {
        if (myAdapter.size() > index) {
            (myAdapter.get(index) as CardMenu).setVisible(visible)
        }
    }

    fun getItemsCount() = myAdapter.size()

    fun clear() {
        finishItemBuilding()
        myAdapter.clear()
        prefCount = 0
    }

    private fun add(item: Item) {
        item.card = CardMenu()
        item.card?.text = item.text
        item.card?.description = item.description
        item.card?.chipText = item.chipText
        item.card?.setIcon(item.icon)
        item.card?.setIcon(item.iconDrawable)
        item.card?.setIconFilter(item.iconFilter)
        item.card?.setBackground(item.bg)
        if (item.textColor != null) item.card?.setTextColor(item.textColor!!)
        if (item.textSize != null) item.card?.setTextSize(item.textSize!!)
        item.card?.setOnClick {
            if (isHided()) return@setOnClick
            item.onClick.invoke(ClickEvent(this, item.card!!))
            onGlobalSelected.invoke(this, item.text)
            if (autoHide) hide()
        }
        if (item.onLongClick != null) {
            item.card?.setOnLongClick { _ ->
                item.onLongClick!!.invoke(ClickEvent(this, item.card!!))
                onGlobalSelected.invoke(this, item.text)
                if (autoHide) hide()
            }
        }

        if (item.cardSpoiler != null) {
            item.cardSpoiler!!.add(item.card!!)
        } else if (spoiler != null) {
            spoiler!!.add(item.card!!)
        } else {
            if (item.preferred) {
                if (prefCount == 0) myAdapter.add(0, CardDivider())
                myAdapter.add(prefCount, item.card!!)
                prefCount++
            } else {
                myAdapter.add(item.card!!)
            }
        }


    }

    fun group(@StringRes title: Int): SplashMenu {
        return group(ToolsResources.s(title))
    }

    fun group(@StringRes title: Int, divider: Boolean): SplashMenu {
        return group(ToolsResources.s(title), divider)
    }

    @JvmOverloads
    fun group(title: String?, divider: Boolean = false): SplashMenu {
        finishItemBuilding()
        myAdapter.add(CardDividerTitleMini().setText(title).setDividerBottom(divider))
        return this
    }

    fun setOnGlobalSelected(onGlobalSelected: (SplashMenu, String?) -> Unit) {
        this.onGlobalSelected = onGlobalSelected
    }

    fun finishItemBuilding() {
        if (buildItem != null) {
            val i = buildItem
            buildItem = null
            if (!skipThisItem && !skipGroup) add(i!!)
            skipThisItem = false
        }
    }

    fun add(@StringRes text: Int): SplashMenu {
        return add(ToolsResources.s(text))
    }

    fun add(@StringRes text: Int, onClick: (ClickEvent) -> Unit = { }): SplashMenu {
        return add(ToolsResources.s(text), onClick)
    }

    fun add(text: String, onClick: (ClickEvent) -> Unit = { }): SplashMenu {
        finishItemBuilding()
        buildItem = Item()
        buildItem!!.text = text
        buildItem!!.onClick = onClick
        return this
    }

    fun text(@StringRes text: Int): SplashMenu {
        return text(ToolsResources.s(text))
    }

    fun text(text: String): SplashMenu {
        buildItem!!.text = text
        return this
    }

    fun description(description: String): SplashMenu {
        buildItem!!.description = description
        return this
    }

    fun chipText(@StringRes chipText: Int): SplashMenu {
        return chipText(ToolsResources.s(chipText))
    }

    fun chipText(chipText: String): SplashMenu {
        buildItem!!.chipText = chipText
        return this
    }

    fun icon(icon: Int): SplashMenu {
        buildItem!!.icon = icon
        return this
    }

    fun icon(icon: Drawable): SplashMenu {
        buildItem!!.iconDrawable = icon
        return this
    }

    fun iconFilter(iconFilter: Int): SplashMenu {
        buildItem!!.iconFilter = iconFilter
        return this
    }

    fun backgroundRes(@ColorRes color: Int): SplashMenu {
        return background(ToolsResources.getColor(color))
    }

    fun backgroundRes(@ColorRes color: Int, condition: () -> Boolean): SplashMenu {
        return if (condition.invoke())
            background(ToolsResources.getColor(color))
        else
            this
    }

    fun background(@ColorInt color: Int): SplashMenu {
        buildItem!!.bg = color
        return this
    }

    fun toSpoiler(cardSpoiler: CardSpoiler): SplashMenu {
        buildItem!!.cardSpoiler = cardSpoiler
        return this
    }

    fun textColorRes(@ColorRes color: Int): SplashMenu {
        return textColor(ToolsResources.getColor(color))
    }

    fun textColorRes(@ColorRes color: Int, condition: () -> Boolean): SplashMenu {
        return if (condition.invoke())
            textColor(ToolsResources.getColor(color))
        else
            this
    }

    fun textColor(@ColorInt color: Int): SplashMenu {
        buildItem!!.textColor = color
        return this
    }

    fun textSize(textSize: Float): SplashMenu {
        buildItem!!.textSize = textSize
        return this
    }

    fun preferred(b: Boolean): SplashMenu {
        buildItem!!.preferred = b
        return this
    }

    fun onClick(onClick: (ClickEvent) -> Unit): SplashMenu {
        buildItem!!.onClick = onClick
        return this
    }

    fun onLongClick(onLongClick: (ClickEvent) -> Unit): SplashMenu {
        buildItem!!.onLongClick = onLongClick
        return this
    }

    fun condition(b: Boolean): SplashMenu {
        skipThisItem = !b
        return this
    }

    fun groupCondition(b: Boolean): SplashMenu {
        finishItemBuilding()
        skipGroup = !b
        return this
    }

    fun reverseGroupCondition(): SplashMenu {
        finishItemBuilding()
        skipGroup = !skipGroup
        return this
    }

    fun clearGroupCondition(): SplashMenu {
        finishItemBuilding()
        skipGroup = false
        return this
    }

    fun spoiler(name: String, backgroundColor: Int? = null, textColor: Int? = null, condition: Boolean = true, filler: ((SplashMenu, CardSpoiler) -> Unit)? = null): SplashMenu {
        if (!condition) return this
        finishItemBuilding()
        condition(true)
        val card = CardSpoiler()
        this.spoiler = card
        card.setRecyclerView(vRecycler)
        card.setTitle(name)
        card.setDividerTopVisible(false)
        card.setDividerVisible(false)
        if (backgroundColor != null) card.setBackgroundColor(backgroundColor)
        if (textColor != null) card.setTextColor(textColor)
        if (filler != null) {
            card.addOnExpandChanged {
                clearGroupCondition()
                if (card.isExpanded() && card.cards.isEmpty()) filler.invoke(this, card)
            }
        }
        myAdapter.add(card)
        return this
    }

    fun spoiler(cardSpoiler: CardSpoiler): SplashMenu {
        this.spoiler = cardSpoiler
        return this
    }

    fun stopSpoiler(): SplashMenu {
        spoiler = null
        return this
    }

    private inner class Item {

        var card: CardMenu? = null
        var cardSpoiler: CardSpoiler? = null
        var onClick: (ClickEvent) -> Unit = { }
        var onLongClick: ((ClickEvent) -> Unit)? = null
        var text = ""
        var description = ""
        var textSize: Float? = null
        var chipText = ""
        var icon = 0
        var iconFilter: Int? = null
        var iconDrawable: Drawable? = null
        var bg = 0
        var textColor: Int? = null
        var preferred = false

    }

    public class ClickEvent(
            val splash: SplashMenu,
            val card: CardMenu
    ) {}

    //
    //  Icon
    //

    private var iconBuilder: IconBuilder? = null

    fun iconBuilder(): IconBuilder {
        if (iconBuilder == null) iconBuilder = IconBuilder()
        return iconBuilder!!
    }

    inner class IconBuilder {

        val cardContainer = CardIconsContainer()
        private var buildItem: Icon? = null
        private var skipThisItem = false
        private var skipGroup = false

        init {
            myAdapter.add(0, cardContainer)
        }

        fun finishItemBuilding() {
            if (buildItem != null) {
                val i = buildItem
                buildItem = null
                if (!skipThisItem && !skipGroup) add(i!!)
                skipThisItem = false
            }
        }

        fun add(drawable: Drawable, onClick: (ClickEventIocn) -> Unit): IconBuilder {
            finishItemBuilding()
            buildItem = Icon()
            buildItem!!.iconDrawable = drawable
            buildItem!!.onClick = onClick
            return this
        }

        fun condition(b: Boolean): IconBuilder {
            skipThisItem = !b
            return this
        }

        private fun add(item: Icon) {
            item.vIcon = ToolsView.inflate(R.layout.z_icon)
            item.vIcon?.setImageDrawable(item.iconDrawable)
            if (item.iconFilter != null) item.vIcon?.setFilter(item.iconFilter!!)
            item.vIcon?.setOnClickListener {
                if (isHided()) return@setOnClickListener
                item.onClick.invoke(ClickEventIocn(this@SplashMenu, item))
                if (autoHide) hide()
            }
            if (item.onLongClick != null) {
                item.vIcon?.setOnLongClickListener {
                    item.onLongClick!!.invoke(ClickEventIocn(this@SplashMenu, item))
                    if (autoHide) hide()
                    return@setOnLongClickListener true
                }
            }

            cardContainer.vLinear.addView(item.vIcon)

        }


    }

    inner class Icon {

        var vIcon: ViewIcon? = null
        var onClick: (ClickEventIocn) -> Unit = { }
        var onLongClick: ((ClickEventIocn) -> Unit)? = null
        var iconDrawable: Drawable? = null
        var iconFilter: Int? = null

    }

    public class ClickEventIocn(
            val splash: SplashMenu,
            val icon: Icon
    ) {}

    class CardIconsContainer : Card(0) {

        val vFrame = FrameLayout(SupAndroid.activity!!)
        val vLinear = LinearLayout(SupAndroid.activity!!)

        init {
            vFrame.addView(vLinear, 0)
            vLinear.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            vLinear.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            (vLinear.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT
        }

        override fun instanceView(): View {
            return vFrame
        }

    }

}
