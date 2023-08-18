package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.animation.AnimationFocus

open class ViewAvatar constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val paint: Paint
    private val animationFocus: AnimationFocus

    val vImageView: ViewCircleImage
    val vChip: ViewChipMini
    val vChipIcon: ViewCircleImage
    private val vTouch: ViewDraw

    init {

        SupAndroid.initEditMode(this)
        val focusColor = ToolsResources.getColor(R.color.focus)

        paint = Paint()
        paint.isAntiAlias = true

        val view: View = ToolsView.inflate(context, R.layout.view_avatar)
        vImageView = view.findViewById(R.id.vDevSupImage)
        vChip = view.findViewById(R.id.vDevSupChip)
        vChipIcon = view.findViewById(R.id.vDevSupChipIcon)
        vTouch = view.findViewById(R.id.vDevSupAvatarTouch)

        isEnabled = false

        addView(view)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewAvatar, 0, 0)
        val src = a.getResourceId(R.styleable.ViewAvatar_android_src, 0)
        val text = a.getString(R.styleable.ViewAvatar_ViewAvatar_chipText)
        val chipBackground = a.getColor(R.styleable.ViewAvatar_ViewAvatar_chipBackground, 0)
        val srcIcon = a.getResourceId(R.styleable.ViewAvatar_ViewAvatar_chipIcon, 0)
        val iconPadding = a.getDimension(R.styleable.ViewAvatar_ViewAvatar_chipIconPadding, 0f)
        val chipSize = a.getDimension(R.styleable.ViewAvatar_ViewAvatar_chipSize, ToolsView.dpToPx(18))
        val roundBackgroundColor = a.getColor(R.styleable.ViewAvatar_ViewAvatar_avatarBackground, 0x00000000)
        a.recycle()

        animationFocus = AnimationFocus(vTouch, focusColor)

        vImageView.setBackgroundColorCircle(roundBackgroundColor)
        setImage(src)
        setChipSize(chipSize.toInt())
        setChipIconPadding(iconPadding.toInt())
        setChipText(text)
        setChipIcon(srcIcon)
        setChipBackground(chipBackground)

        vTouch.setOnDraw { canvas ->
            paint.color = animationFocus.update()
            canvas.drawCircle(vTouch.width / 2f, vTouch.height / 2f, vTouch.height / 2f, paint)
        }
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        if (params.width == LayoutParams.WRAP_CONTENT && params.height == LayoutParams.WRAP_CONTENT) {
            params.width = ToolsView.dpToPx(48).toInt()
            params.height = ToolsView.dpToPx(48).toInt()
        }

        if (params.width > 0 && params.height == LayoutParams.WRAP_CONTENT)
            params.height = params.width
        if (params.height > 0 && params.width == LayoutParams.WRAP_CONTENT) params.width = params.height

        super.setLayoutParams(params)

    }

    //
    //  Setters
    //

    fun setChipSize(size: Int) {
        vChipIcon.layoutParams.width = size
        vChipIcon.layoutParams.height = size
        vChip.layoutParams.height = size
    }

    fun setChipText(t: String?) {
        vChip.setText(t)
    }

    fun setChipIcon(icon: Int) {
        vChipIcon.setImageResource(icon)
        vChipIcon.visibility = if (icon == 0) View.GONE else View.VISIBLE
    }

    fun setChipIconPadding(p: Int) {
        vChipIcon.setPadding(p, p, p, p)
    }

    fun setChipBackground(color: Int) {
        vChip.setBackgroundColor(color)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        vTouch.setOnClickListener(l)
        vTouch.isClickable = l != null
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        vTouch.setOnLongClickListener(l)
        vTouch.isLongClickable = l != null
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vImageView.isEnabled = enabled
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        vImageView.isClickable = clickable
    }

    fun setImage(@DrawableRes image: Int) {
        if (image != 0)
            vImageView.setImageResource(image)
        else
            vImageView.setImageBitmap(null)

    }

    fun setImage(bitmap: Bitmap?) {
        vImageView.setImageBitmap(bitmap)
    }

    //
    //  Getters
    //

    fun getText() = vChip.getText()


}
