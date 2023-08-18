package com.sup.dev.android.views.splash

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewCircleImage
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsText

class SplashAlert : Splash(R.layout.splash_alert) {

    private val vCheck: CheckBox = findViewById(R.id.vCheckBox)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vInfo: Button = findViewById(R.id.vInfo)
    private val vText: ViewText = findViewById(R.id.vText)
    private val vTopContainer: ViewGroup = findViewById(R.id.vTopContainer)
    private val vTopImage: ViewCircleImage = findViewById(R.id.vTopImage)
    private val vTopTitle: TextView = findViewById(R.id.vTopTitle)

    private var key: String? = null
    private var lockUntilAccept: Boolean = false
    private var autoHideOnEnter = true
    private var autoHideOnInfo = true
    private var onChecker:(Boolean)->Unit = {}
    private var isEnterClicked = false

    init {

        vText.text = ""

        vText.visibility = View.GONE
        vCancel.visibility = View.GONE
        vEnter.visibility = View.GONE
        vCheck.visibility = View.GONE
        vTopContainer.visibility = View.GONE
        vTopImage.visibility = View.GONE
        vTopTitle.visibility = View.GONE
        vInfo.visibility = View.GONE

        vCheck.setOnCheckedChangeListener { _, _ -> updateLock(vEnter, vCheck) }
    }

    private fun updateLock(vEnter: Button, vCheck: CheckBox) {
        if (lockUntilAccept) vEnter.isEnabled = vCheck.isChecked
    }

    fun getCheckboxCondition() = vCheck.isChecked

    fun isEnterClicked() = isEnterClicked

    //
    //  Setters
    //

    override fun setCancelable(cancelable: Boolean): SplashAlert {
        super.setCancelable(cancelable)
        return this
    }

    override fun setEnabled(enabled: Boolean): SplashAlert {
        super.setEnabled(enabled)
        vCheck.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        vText.isEnabled = enabled
        vTopContainer.isEnabled = enabled
        vTopImage.isEnabled = enabled
        vTopTitle.isEnabled = enabled
        return this
    }

    override fun setTitle(title: Int): SplashAlert {
        super.setTitle(title)
        return this
    }

    override fun setTitle(title: String?): SplashAlert {
        super.setTitle(title)
        return this
    }

    fun setLockUntilAccept(lockUntilAccept: Boolean): SplashAlert {
        this.lockUntilAccept = lockUntilAccept
        return this
    }


    fun setChecker(@StringRes text: Int): SplashAlert {
        return setChecker(null, ToolsResources.s(text))
    }

    fun setChecker(key: String? = null, @StringRes text: Int): SplashAlert {
        return setChecker(key, ToolsResources.s(text))
    }

    @JvmOverloads
    fun setChecker(key: String?=null, text: String? = SupAndroid.TEXT_APP_DONT_SHOW_AGAIN): SplashAlert {
        this.key = key
        vCheck.text = text
        vCheck.visibility = View.VISIBLE
        return this
    }


    fun setTitleImageBackgroundRes(@ColorRes res: Int): SplashAlert {
        return setImageBackground(ToolsResources.getColor(res))
    }

    fun setImageBackground(@ColorInt color: Int): SplashAlert {
        vTopContainer.setBackgroundColor(color)
        return this
    }

    fun setTitleImage(image: Int): SplashAlert {
        vTopContainer.visibility = if (image > 0) View.VISIBLE else View.GONE
        ToolsView.setImageOrGone(vTopImage, image)
        return this
    }

    fun setTitleImage(image: Bitmap?): SplashAlert {
        vTopContainer.visibility = if (image != null) View.VISIBLE else View.GONE
        ToolsView.setImageOrGone(vTopImage, image)
        return this
    }

    fun setTitleImageTopPadding(padding: Float): SplashAlert {
        vTopImage.setPadding(vTopImage.paddingLeft, padding.toInt(), vTopImage.paddingRight, vTopImage.paddingBottom)
        return this
    }

    fun setTitleImage(setter:(ViewCircleImage)->Unit): SplashAlert {
        vTopContainer.visibility = View.VISIBLE
        vTopImage.visibility = View.VISIBLE
        setter.invoke(vTopImage)
        return this
    }

    fun setTopTitleText(@StringRes topTitle: Int): SplashAlert {
        return setTopTitleText(ToolsResources.s(topTitle))
    }

    fun setTopTitleText(topTitle: String?): SplashAlert {
        vTopContainer.visibility = if (ToolsText.empty(topTitle)) View.GONE else View.VISIBLE
        ToolsView.setTextOrGone(vTopTitle, topTitle)
        return this
    }

    fun addLine(text: Int) = addLine(ToolsResources.s(text))

    fun addLine(text: String): SplashAlert {
        vText.text = vText.text.toString() + "\n" + text
        vText.visibility = View.VISIBLE
        ToolsView.makeLinksClickable(vText)
        vText.isFocusable = false
        return this
    }

    fun setText(@StringRes text: Int): SplashAlert {
        return setText(ToolsResources.s(text))
    }

    fun setText(text: CharSequence?): SplashAlert {
        ToolsView.setTextOrGone(vText, text)
        ToolsView.makeLinksClickable(vText)
        vText.isFocusable = false
        return this
    }

    fun setTextGravity(gravity: Int): SplashAlert {
        vText.gravity = gravity
        return this
    }

    fun setOnEnter(@StringRes s: Int): SplashAlert {
        return setOnEnter(ToolsResources.s(s))
    }

    fun setOnChecker(onChecker:(Boolean)->Unit): SplashAlert {
        this.onChecker = onChecker
        return this
    }

    fun setOnEnter(@StringRes s: Int, onEnter: (SplashAlert) -> Unit): SplashAlert {
        return setOnEnter(ToolsResources.s(s), onEnter)
    }

    @JvmOverloads
    fun setOnEnter(s: String?, onEnter: (SplashAlert) -> Unit = {}): SplashAlert {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            isEnterClicked = true
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            if (key != null) ToolsStorage.put(key!!, vCheck.isChecked)
            onEnter.invoke(this)
            onChecker.invoke(vCheck.isChecked)
        }
        return this
    }

    fun setOnInfo(@StringRes s: Int, onInfo: (SplashAlert) -> Unit = {}): SplashAlert {
        return setOnInfo(ToolsResources.s(s), onInfo)
    }

    @JvmOverloads
    fun setOnInfo(s: String?, onInfo: (SplashAlert) -> Unit = {}): SplashAlert {
        ToolsView.setTextOrGone(vInfo, s)
        vInfo.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            onInfo.invoke(this)
        }
        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashAlert {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setAutoHideOnInfo(autoHideOnInfo: Boolean): SplashAlert {
        this.autoHideOnInfo = autoHideOnInfo
        return this
    }

    fun setOnCancel(@StringRes s: Int): SplashAlert {
        return setOnCancel(ToolsResources.s(s), {})
    }

    fun setOnCancel(onCancel: (SplashAlert) -> Unit): SplashAlert {
        return setOnCancel(null, onCancel)
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashAlert) -> Unit = {}): SplashAlert {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashAlert) -> Unit = {}): SplashAlert {
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }

        return this
    }

    override fun setOnHide(onHide: (Splash) -> Unit): SplashAlert {
        super.setOnHide{onHide.invoke(this)}
        return this
    }

    companion object {

        fun check(key: String): Boolean {
            return ToolsStorage.getBoolean(key, false)
        }

        fun clear(key: String) {
            ToolsStorage.clear(key)
        }
    }

}
