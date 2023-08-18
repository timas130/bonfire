package com.sup.dev.android.views.cards

import android.graphics.PorterDuff
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.CardAdapter
import com.sup.dev.java.classes.callbacks.CallbacksList1
import java.util.*

open class CardSpoiler : Card(R.layout.card_spoiler) {

    val cards = ArrayList<Card>()

    private val onExpandChanged = CallbacksList1<Boolean>()

    private var titleGravity = Gravity.LEFT
    private var title: String? = null
    private var titleExpanded: String? = null
    private var text: String? = null
    private var textSize: Float? = null
    private var rightText: String? = null
    private var titleColor = 0
    private var rightTextColor = 0
    private var textColor = 0
    private var backgroundColor: Int? = null
    private var originalSeted = false
    private var dividerVisible = true
    private var dividerTopVisible = false
    private var titleColorOriginal = 0
    private var rightTextColorOriginal = 0
    private var textColorOriginal = 0
    private var iconColor = 0
    private var useExpandedArrow = true
    private var useExpandedTitleArrow = false
    private var animation = false
    private var isProgress = false
    private var vRecycler: RecyclerView? = null
    private var autoHideBottomDividerOnExpand = false
    private var autoHideTopDividerOnExpand = false
    private var autoHideTopDividerIfTopCardIsSpoilerWithBottomDivider = true

    internal var expanded = false
    internal var enabled = true

    @Suppress("DEPRECATION")
    override fun bindView(view: View) {
        super.bindView(view)
        val vIcon: ImageView = view.findViewById(R.id.vIcon)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vText: TextView = view.findViewById(R.id.vText)
        val vRightText: TextView = view.findViewById(R.id.vRightText)
        val vTouch: View = view.findViewById(R.id.vTouch)
        val vDivider: View = view.findViewById(R.id.vDivider)
        val vDividerTop: View = view.findViewById(R.id.vDividerTop)
        val vRoot: View = view.findViewById(R.id.vRoot)
        val vProgress: View = view.findViewById(R.id.vProgress)

        val iconDown = R.drawable.ic_keyboard_arrow_down_white_24dp
        val iconUp = R.drawable.ic_keyboard_arrow_up_white_24dp

        if (!originalSeted) {
            originalSeted = true
            titleColorOriginal = vTitle.currentTextColor
            textColorOriginal = vText.currentTextColor
            rightTextColorOriginal = vRightText.currentTextColor
        }

        vProgress.visibility = if (isProgress) View.VISIBLE else View.GONE
        vIcon.visibility = if (!isProgress && useExpandedArrow) View.VISIBLE else View.GONE

        vText.text = if (text == null) null else Html.fromHtml(text)
        if (textSize != null) vText.setTextSize(textSize!!)
        vRightText.text = if (rightText == null) null else Html.fromHtml(rightText)

        if (expanded && titleExpanded != null)
            vTitle.text = Html.fromHtml(titleExpanded)
        else
            vTitle.text = if (title == null) null else Html.fromHtml(title)

        vText.visibility = if (text == null) View.GONE else View.VISIBLE
        vRightText.visibility = if (rightText == null) View.GONE else View.VISIBLE
        vTitle.visibility = if (title == null) View.GONE else View.VISIBLE

        if (useExpandedTitleArrow) vTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (expanded) iconUp else iconDown, 0)
        else vTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        vText.isEnabled = enabled
        vRightText.isEnabled = enabled
        vTitle.isEnabled = enabled

        (vTitle.layoutParams as LinearLayout.LayoutParams).gravity = titleGravity

        vDivider.visibility = if (willShowBottomDivider()) View.VISIBLE else View.GONE
        vDividerTop.visibility = if (willShowTopDivider()) View.VISIBLE else View.GONE
        vText.setTextColor(if (textColor != 0) textColor else textColorOriginal)
        vRightText.setTextColor(if (rightTextColor != 0) rightTextColor else rightTextColorOriginal)
        vTitle.setTextColor(if (titleColor != 0) titleColor else titleColorOriginal)

        vIcon.setImageResource(if (expanded) iconUp else iconDown)
        vIcon.setAlpha(if (enabled) 255 else 106)
        if (iconColor != 0) vIcon.setColorFilter(iconColor, PorterDuff.Mode.SRC_ATOP)
        if (enabled) vTouch.setOnClickListener { setExpanded(!expanded) }
        else vTouch.setOnClickListener(null)

        if (backgroundColor != null) vRoot.setBackgroundColor(backgroundColor!!)

        vTouch.isClickable = enabled
    }

    private fun willShowBottomDivider():Boolean{
        return dividerVisible && (!autoHideBottomDividerOnExpand || !expanded)
    }

    private fun willShowTopDivider():Boolean{
        val b = !isHideDisableTopDividerByTopCard()
        return dividerTopVisible && (!autoHideTopDividerOnExpand || !expanded) && !isHideDisableTopDividerByTopCard()
    }

    private fun isHideDisableTopDividerByTopCard():Boolean{
        if(!autoHideTopDividerIfTopCardIsSpoilerWithBottomDivider) return false
        if(!dividerTopVisible) return false
        val card = getTopSpoiler() ?: return false
        return !card.expanded && card.willShowBottomDivider()
    }

    private fun getBottomSpoiler():CardSpoiler?{
        for (i in adapter.indexOf(this) + 1 until adapter.size()){
            val card = adapter[i]
            if(card is CardSpoiler) return card
        }
        return null
    }

    private fun getTopSpoiler():CardSpoiler?{
        for (i in adapter.indexOf(this) - 1 downTo 0){
            val card = adapter[i]
            if(card is CardSpoiler) return card
        }
        return null
    }

    //
    //  Setters
    //

    fun setProgress(isProgress: Boolean): CardSpoiler {
        this.isProgress = isProgress
        update()
        return this
    }

    fun setUseExpandedArrow(useExpandedArrow: Boolean): CardSpoiler {
        this.useExpandedArrow = useExpandedArrow
        update()
        return this
    }

    fun setUseExpandedTitleArrow(useExpandedArrow: Boolean): CardSpoiler {
        this.useExpandedTitleArrow = useExpandedArrow
        update()
        return this
    }

    fun setAutoHideTopDividerIfTopCardIsSpoilerWithBottomDivider(autoHideTopDividerIfTopCardIsSpoilerWithBottomDivider: Boolean): CardSpoiler {
        this.autoHideTopDividerIfTopCardIsSpoilerWithBottomDivider = autoHideTopDividerIfTopCardIsSpoilerWithBottomDivider
        update()
        return this
    }

    fun add(card: Card): CardSpoiler {
        cards.add(card)
        setExpanded(expanded)
        return this
    }

    fun remove(card: Card): CardSpoiler {
        cards.remove(card)
        adapter.remove(card)
        return this
    }

    open fun setTitle(@StringRes title: Int): CardSpoiler {
        return setTitle(ToolsResources.s(title))
    }

    fun setTitle(title: String?): CardSpoiler {
        this.title = title
        update()
        return this
    }

    fun setAutoHideBottomDividerOnExpand(b: Boolean): CardSpoiler {
        this.autoHideBottomDividerOnExpand = b
        update()
        return this
    }
    fun setAutoHideTopDividerOnExpand(b: Boolean): CardSpoiler {
        this.autoHideTopDividerOnExpand = b
        update()
        return this
    }

    open fun setTitleExpanded(@StringRes title: Int): CardSpoiler {
        return setTitleExpanded(ToolsResources.s(title))
    }

    fun setTitleExpanded(title: String?): CardSpoiler {
        this.titleExpanded = title
        update()
        return this
    }

    fun setTitleGravity(gravity: Int): CardSpoiler {
        this.titleGravity = gravity
        update()
        return this
    }

    override fun setCardAdapter(adapter: CardAdapter?) {
        super.setCardAdapter(adapter)
        setExpanded(expanded)
    }

    open fun onExpandedClicked(expanded: Boolean) {

    }

    fun setExpanded(expanded: Boolean): CardSpoiler {
        this.expanded = expanded
        onExpandedClicked(expanded)
        update()

        if (expanded) {
            var myIndex = adapter.indexOf(this)
            for (c in cards)
                if (myIndex != -1) {
                    ++myIndex
                    if (!adapter.contains(c)) adapter.add(myIndex, c)
                }
            if (cards.isNotEmpty()) {
                //   if (!animation) adapter.notifyUpdate()
                if (vRecycler != null) ToolsView.jumpToWithAnimation(vRecycler!!, adapter.indexOf(this))
            }
        } else {
            for (c in cards) adapter.remove(c)
            // if (!animation) adapter.notifyUpdate()
        }

        onExpandChanged.invoke(expanded)

        getBottomSpoiler()?.update()
        getTopSpoiler()?.update()

        return this
    }

    fun setEnabled(enabled: Boolean): CardSpoiler {
        this.enabled = enabled
        update()
        return this
    }

    fun setText(text: String): CardSpoiler {
        this.text = text
        update()
        return this
    }

    fun setTextSize(textSize: Float): CardSpoiler {
        this.textSize = textSize
        update()
        return this
    }

    fun setRightText(rightText: String): CardSpoiler {
        this.rightText = rightText
        update()
        return this
    }

    fun setIconColor(iconColor: Int): CardSpoiler {
        this.iconColor = iconColor
        update()
        return this
    }

    fun setTextColor(textColor: Int): CardSpoiler {
        this.textColor = textColor
        update()
        return this
    }

    fun setBackgroundColor(backgroundColor: Int): CardSpoiler {
        this.backgroundColor = backgroundColor
        update()
        return this
    }

    fun setTitleColor(titleColor: Int): CardSpoiler {
        this.titleColor = titleColor
        update()
        return this
    }

    fun setAnimation(animation: Boolean): CardSpoiler {
        this.animation = animation
        return this
    }

    fun setRightTextColor(rightTextColor: Int): CardSpoiler {
        this.rightTextColor = rightTextColor
        update()
        return this
    }

    fun setDividerVisible(dividerVisible: Boolean): CardSpoiler {
        this.dividerVisible = dividerVisible
        update()
        return this
    }

    fun setRecyclerView(vRecycler: RecyclerView?): CardSpoiler {
        this.vRecycler = vRecycler
        return this
    }

    fun setDividerTopVisible(dividerTopVisible: Boolean): CardSpoiler {
        this.dividerTopVisible = dividerTopVisible
        update()
        return this
    }

    fun addOnExpandChanged(onExpandChanged: (Boolean) -> Unit): CardSpoiler {
        this.onExpandChanged.add(onExpandChanged)
        return this
    }

    fun isHasOnExpandChangedCallback() = onExpandChanged.isNotEmpty()

    fun isExpanded() = expanded
}
