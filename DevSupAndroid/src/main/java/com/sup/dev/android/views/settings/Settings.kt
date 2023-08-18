package com.sup.dev.android.views.settings

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewCircleImage
import java.util.ArrayList

open class Settings @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @LayoutRes layoutRes: Int = R.layout.settings_action
) : FrameLayout(context, attrs) {


    val view: View
    private var isSubSettingsEnabled = true
    private var reversSubSettingsEnabled = false
    private val line: View
    val vIcon: ViewCircleImage?
    val vTitle: TextView?
    val vSubtitle: TextView?
    val vSubViewContainer: ViewGroup?

    private var subSettings: ArrayList<Settings>? = null

    init {

        SupAndroid.initEditMode(this)

        view = ToolsView.inflate(this, layoutRes)
        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        vIcon = findViewById(R.id.vDevSupIcon)
        vTitle = findViewById(R.id.vDevSupTitle)
        vSubtitle = findViewById(R.id.vDevSupSubtitle)
        vSubViewContainer = findViewById(R.id.vDevSupContainer)

        line = View(context)
        addView(line, ViewGroup.LayoutParams.MATCH_PARENT, 1)
        line.setBackgroundColor(ToolsResources.getColor(R.color.grey_600))
        (line.layoutParams as MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), 0, ToolsView.dpToPx(8).toInt(), 0)
        (line.layoutParams as LayoutParams).gravity = Gravity.BOTTOM

        val a = context.obtainStyledAttributes(attrs, R.styleable.Settings, 0, 0)
        isFocusable = a.getBoolean(R.styleable.Settings_android_focusable, true)
        val lineVisible = a.getBoolean(R.styleable.Settings_Settings_lineVisible, true)
        val title = a.getString(R.styleable.Settings_Settings_title)
        val subtitle = a.getString(R.styleable.Settings_Settings_subtitle)
        val icon = a.getResourceId(R.styleable.Settings_Settings_icon, 0)
        val iconBackground = a.getColor(R.styleable.Settings_Settings_icon_background, 0)
        val iconFilter = a.getColor(R.styleable.Settings_Settings_icon_filter, 0)
        val iconPadding = a.getDimension(R.styleable.Settings_Settings_icon_padding, ToolsView.dpToPx(6))
        a.recycle()

        setLineVisible(lineVisible)
        setTitle(title)
        setSubtitle(subtitle)
        setIcon(icon)
        setIconBackground(iconBackground)
        setIconPaddingPx(iconPadding)
        setIconFilter(iconFilter)
    }

    //
    //  Setters
    //

    fun setSubView(view: View?) {
        vSubViewContainer?.removeAllViews()
        if (view != null)
            vSubViewContainer?.addView(view)
    }

    open fun setTitle(@StringRes titleRes: Int) {
        setTitle(ToolsResources.s(titleRes))
    }

    open fun setTitle(title: String?) {
        vTitle?.text = title
        vTitle?.visibility = if (title != null && title.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setSubtitle(@StringRes subtitleRes: Int) {
        setSubtitle(ToolsResources.s(subtitleRes))
    }

    fun setSubtitle(subtitle: String?) {
        vSubtitle?.text = subtitle
        vSubtitle?.visibility = if (subtitle != null && subtitle.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setSubtitleColor(color: Int) {
        vSubtitle?.setTextColor(color)
    }

    fun setIcon(@DrawableRes icon: Int) {
        if (icon == 0)
            vIcon?.setImageBitmap(null)
        else
            vIcon?.setImageResource(icon)
        vIcon?.visibility = if (icon == 0) View.GONE else View.VISIBLE
    }

    fun setIconPadding(dp: Int) {
        setIconPaddingPx(ToolsView.dpToPx(dp))
    }

    fun setIconPaddingPx(px: Float) {
        vIcon?.setPadding(px.toInt(), px.toInt(), px.toInt(), px.toInt())
    }

    fun setIconBackground(color: Int) {
        vIcon?.setBackgroundColor(color)
    }

    fun setIconFilter(color: Int) {
        vIcon?.setColorFilter(color)
    }

    fun addSubSettings(settings: Settings) {
        if (subSettings == null) subSettings = ArrayList()
        subSettings!!.add(settings)
        settings.isEnabled = isSubSettingsEnabled && isEnabled
    }

    fun setReversSubSettingsEnabled(reversSubSettingsEnabled: Boolean) {
        this.reversSubSettingsEnabled = reversSubSettingsEnabled
        setEnabledSubSettings(isEnabled)
    }

    fun setEnabledSubSettings(enabled: Boolean) {
        isSubSettingsEnabled = enabled
        if (subSettings != null) for (settings in subSettings!!) settings.isEnabled = if (reversSubSettingsEnabled) !(isSubSettingsEnabled && isEnabled) else (isSubSettingsEnabled && isEnabled)
    }

    fun setLineVisible(b: Boolean) {
        line.visibility = if (b) View.VISIBLE else View.GONE
    }

    fun setSubSettingsEnabled(isSubSettingsEnabled: Boolean) {
        this.isSubSettingsEnabled = isSubSettingsEnabled
    }

    @CallSuper
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        view.isEnabled = enabled
        vTitle?.isEnabled = enabled
        vSubtitle?.isEnabled = enabled
        setEnabledSubSettings(enabled)
        if (subSettings != null) for (settings in subSettings!!) settings.isEnabled = isSubSettingsEnabled && isEnabled
    }

    override fun setOnClickListener(l: OnClickListener?) {
        view.setOnClickListener(l)
        view.isFocusable = l != null
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        view.setOnLongClickListener(l)
        view.isFocusable = l != null
    }


    //
    //  Getters
    //

    fun getTitle() = vTitle?.text ?: ""

    fun isSubSettingsEnabled() = isSubSettingsEnabled

}
