package com.sup.dev.android.views.cards

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatarTitle

open class CardAvatar(
    layout: Int = 0
) : Card(if (layout > 0) layout else R.layout.card_avatar) {

    protected var onClick: () -> Unit = {}
    protected var onLongClick: (CardAvatar, View, Float, Float) -> Unit = { _, _, _, _ -> }
    private var dividerVisible = false
    private var enabled = true
    private var background = 0
    private var title: String? = null
    private var subtitle: String? = null
    private var chipText: String? = null
    private var image: Bitmap? = null
    private var imageRes: Int? = null
    private var avatarSize:Int? = null
    @DrawableRes
    private var chipIcon = 0
    private var chipIconPadding = 0
    @ColorInt
    private var chipBackground = 0
    private var chipUseIconBackground = false
    private var onClickAvatar: (() -> Unit)? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val vTouch: View = view.findViewById(R.id.vTouch)
        val vDivider: View? = view.findViewById(R.id.vDivider)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        if(vDivider != null) vDivider.visibility = if (dividerVisible) View.VISIBLE else View.GONE
        vTouch.isFocusable = true && enabled
        vTouch.isClickable = true && enabled
        vTouch.isEnabled = true && enabled
        if (enabled) vTouch.setOnClickListener { onClick.invoke() }
        else vTouch.setOnClickListener(null)
        if (enabled) ToolsView.setOnLongClickCoordinates(vTouch) { v, x, y -> onLongClick.invoke(this, v, x, y) }
        else vTouch.setOnLongClickListener(null)
        if (background != 0) view.setBackgroundColor(background)
        vAvatar.isEnabled = isEnabled()
        vAvatar.setTitle(title)
        vAvatar.setSubtitle(subtitle)
        vAvatar.isClickable = false
        if (onClickAvatar != null) vAvatar.vAvatar.setOnClickListener { onClickAvatar!!.invoke() }
        else vAvatar.vAvatar.setOnClickListener(null)
        if (imageRes != null) vAvatar.vAvatar.setImage(imageRes!!)
        else vAvatar.vAvatar.setImage(image)
        vAvatar.vAvatar.setChipIcon(chipIcon)
        vAvatar.vAvatar.setChipIconPadding(chipIconPadding)
        vAvatar.vAvatar.setChipText(chipText)
        vAvatar.vAvatar.setChipBackground(chipBackground)

        if(avatarSize != null) {
            vAvatar.vAvatar.layoutParams.width = avatarSize!!
            vAvatar.vAvatar.layoutParams.height = avatarSize!!
        }

        onBind(vAvatar)
    }

    protected open fun onBind(vAvatar: ViewAvatarTitle) {

    }

    //
    //  Setters
    //

    fun setOnCLickAvatar(onClickAvatar: (() -> Unit)?): CardAvatar {
        this.onClickAvatar = onClickAvatar
        update()
        return this
    }

    fun setTitle(title: Int) = setTitle(com.sup.dev.android.tools.ToolsResources.s(title))

    fun setTitle(title: String): CardAvatar {
        this.title = title
        update()
        return this
    }

    fun setSubtitle(subtitle: String): CardAvatar {
        this.subtitle = subtitle
        update()
        return this
    }

    fun setImage(image: Bitmap): CardAvatar {
        this.image = image
        update()
        return this
    }

    fun setImage(image: Int): CardAvatar {
        this.imageRes = image
        update()
        return this
    }

    fun setAvatarSize(avatarSize: Int): CardAvatar {
        this.avatarSize = avatarSize
        update()
        return this
    }

    fun setChipIcon(@DrawableRes icon: Int): CardAvatar {
        this.chipIcon = icon
        update()
        return this
    }

    fun setChipIconPadding(p: Int): CardAvatar {
        chipIconPadding = p
        update()
        return this
    }

    fun setChipBackground(@ColorInt chipBackground: Int): CardAvatar {
        this.chipBackground = chipBackground
        update()
        return this
    }

    fun setChipText(avatarText: String): CardAvatar {
        this.chipText = avatarText
        update()
        return this
    }

    fun setEnabled(enabled: Boolean): CardAvatar {
        this.enabled = enabled
        update()
        return this
    }

    open fun setOnClick(onClick: () -> Unit): CardAvatar {
        this.onClick = onClick
        update()
        return this
    }

    fun setOnLongClick(onLongClick: (CardAvatar, View, Float, Float) -> Unit): CardAvatar {
        this.onLongClick = onLongClick
        update()
        return this
    }

    fun setDividerVisible(dividerVisible: Boolean): CardAvatar {
        this.dividerVisible = dividerVisible
        update()
        return this
    }

    fun setBackground(background: Int): CardAvatar {
        this.background = background
        update()
        return this
    }

    fun setChipUseIconBackground(chipUseIconBackground: Boolean): CardAvatar {
        this.chipUseIconBackground = chipUseIconBackground
        update()
        return this
    }

    //
    //  Getters
    //

    fun isEnabled(): Boolean {
        return enabled
    }

}
