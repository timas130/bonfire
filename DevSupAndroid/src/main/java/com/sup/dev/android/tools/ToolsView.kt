@file:Suppress("UNCHECKED_CAST")

package com.sup.dev.android.tools

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.view.TintableBackgroundView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.magic_box.AndroidBug5497Workaround
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.android.views.splash.SplashProgressWithTitle
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Pattern

object ToolsView {

    val ANIMATION_TIME = 300
    val ANIMATION_TIME_FASE = 200

    fun disableScrollViewJump(vScroll:ViewGroup){//    Убирает автоскролл к выделенному полю в ScrollView при клике на другие виджеты.
        vScroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        vScroll.isFocusable = true
        vScroll.isFocusableInTouchMode = true
        vScroll.setOnTouchListener { v, event ->
            v.requestFocus(View.FOCUS_DOWN)
            false
        }
    }

    fun getSelectionPosition(vFiled: EditText): Point {
        val pos = vFiled.selectionStart
        val layout = vFiled.layout
        val line = layout.getLineForOffset(pos)
        val baseline = layout.getLineBaseline(line)
        val ascent = layout.getLineAscent(line)

        val point = Point()
        point.x = layout.getPrimaryHorizontal(pos).toInt() - vFiled.scrollX
        point.y = baseline + ascent - vFiled.scrollY

        return point
    }

    fun onSelectionChanged(vFiled: EditText,onChanged: () -> Unit) {
        onSelectionChanged(WeakReference(vFiled), onChanged)
    }

    private fun onSelectionChanged(vFiled: WeakReference<EditText>, onChanged: () -> Unit) {

        val item = Item(vFiled.get()?.selectionEnd?:0)
        vFiled.get()?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val get = vFiled.get()?:return
                if(item.a != get.selectionEnd){
                    item.a = get.selectionEnd
                    onChanged.invoke()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        ToolsThreads.timerMain(200){
            val get = vFiled.get()
            if(get == null){
                it.unsubscribe()
                return@timerMain
            }
            if(item.a != get.selectionEnd){
                item.a = get.selectionEnd
                onChanged.invoke()
            }
        }

    }

    fun jumpToWithAnimation(vRecycler: RecyclerView, position: Int, animationArg: Float = 5f) {
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(vRecycler.context) {

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return super.calculateSpeedPerPixel(displayMetrics) * animationArg
            }

            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = position
        vRecycler.layoutManager?.startSmoothScroll(smoothScroller)
    }

    fun scrollRecyclerSmooth(vRecycler: RecyclerView, position: Int) {
        if (vRecycler.adapter == null || vRecycler.adapter!!.itemCount == 0) return
        if (position >= vRecycler.adapter!!.itemCount) vRecycler.smoothScrollToPosition(vRecycler.adapter!!.itemCount - 1)
        else if (position < 0) vRecycler.smoothScrollToPosition(0)
        else vRecycler.smoothScrollToPosition(position)
    }

    fun scrollRecycler(vRecycler: RecyclerView, position: Int) {
        if (vRecycler.adapter == null || vRecycler.adapter!!.itemCount == 0) return
        if (position >= vRecycler.adapter!!.itemCount) vRecycler.scrollToPosition(vRecycler.adapter!!.itemCount - 1)
        else if (position < 0) vRecycler.scrollToPosition(0)
        else vRecycler.scrollToPosition(position)
    }

    fun addLink(vText: ViewText, link: String, color:Int?=null, onClick: () -> Unit) {
        val text = vText.text.toString()
        val index = text.indexOf(link)

        if (index == -1) return

        val span = if (vText.text is Spannable) vText.text as Spannable else Spannable.Factory.getInstance().newSpannable(text)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                onClick.invoke()
            }
        }, index, index + link.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        if(color != null) span.setSpan(ForegroundColorSpan(color), index, index + link.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


        vText.text = span
        makeLinksClickable(vText)
    }

    @Suppress("DEPRECATION")
    fun makeTextHtml(vText: TextView) {
        vText.text = Html.fromHtml(vText.text.toString())
    }

    fun setRecyclerAnimation(vRecycler: RecyclerView) {
        vRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(vRecycler.context, R.anim.layout_animation_slide_from_bottom)
    }

    fun onFieldEnterKey(vFiled: EditText, callback: () -> Unit) {
        vFiled.setOnEditorActionListener { v, actionId, event ->
            if (actionId == 6 || actionId == 5) {
                callback.invoke()
                true
            } else {
                false
            }
        }
    }

    fun setNavigationBarColor(window: Window, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = color
        }
    }

    fun makeNotFullscreen(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    fun makeHalfFullscreen(activity: Activity) {
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    fun makeFullscreen(activity: Activity) {
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun dontAutoShowKeyboard(window: Window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    fun setFabEnabledR(vFab: FloatingActionButton, enabled: Boolean, @ColorRes colorEnabled: Int) {
        setFabEnabled(vFab, enabled, ToolsResources.getColor(colorEnabled))
    }

    @JvmOverloads
    fun setFabEnabled(vFab: FloatingActionButton, enabled: Boolean, @ColorInt colorEnabled: Int = ToolsResources.getSecondaryColor(vFab.context)) {
        vFab.isEnabled = enabled
        setFabColor(vFab, if (enabled) colorEnabled else ToolsResources.getColor(R.color.grey_700))
    }

    fun setFabColorR(vFab: FloatingActionButton, @ColorRes color: Int) {
        setFabColor(vFab, ToolsResources.getColor(color))
    }

    fun setFabColor(vFab: FloatingActionButton, @ColorInt color: Int) {
        vFab.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun makeLinksClickable(vText: ViewText) {
        vText.setLinkTextColor(ToolsResources.getSecondaryColor(vText.context))
        Linkify.addLinks(vText, Pattern.compile("[a-z]+:\\/\\/[^ \\n]*"), "")

        val m = vText.movementMethod
        if (m == null || m !is LinkMovementMethod) if (vText.linksClickable) vText.movementMethod = LinkMovementMethod.getInstance()
    }

    fun recyclerHideFabWhenScrollEnd(vRecycler: RecyclerView, vFab: FloatingActionButton) {
        vRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (vRecycler.computeVerticalScrollOffset() != 0 && vRecycler.computeVerticalScrollOffset() + 50 >= vRecycler.computeVerticalScrollRange() - vRecycler.computeVerticalScrollExtent())
                    vFab.hide()
                else
                    vFab.show()
            }

        })
    }

    fun <K : View> removeFromParent(view: K): K {
        if (view.parent != null) (view.parent as ViewGroup).removeView(view)
        return view
    }

    fun showProgressDialog(): SplashProgressTransparent {
        val widget = SplashProgressTransparent().setCancelable(false)
        ToolsThreads.main { widget.asDialogShow() }
        return widget
    }

    fun showProgressDialog(title: Int): SplashProgressWithTitle {
        return showProgressDialog(ToolsResources.s(title))
    }

    fun showProgressDialog(title: String?): SplashProgressWithTitle {
        val splash: SplashProgressWithTitle = SplashProgressWithTitle().setTitle(title).setCancelable(false) as SplashProgressWithTitle
        ToolsThreads.main { splash.asSheetShow() }
        return splash
    }

    fun setImageOrGone(vImage: ImageView, bitmap: Bitmap?) {
        vImage.setImageBitmap(bitmap)
        if (bitmap != null)
            vImage.visibility = VISIBLE
        else
            vImage.visibility = GONE
    }

    fun setImageOrGone(vImage: ImageView, image: Int) {
        if (image > 0) {
            vImage.setImageResource(image)
            vImage.visibility = VISIBLE
        } else {
            vImage.setImageBitmap(null)
            vImage.visibility = GONE
        }
    }

    fun setTextOrGone(vText: TextView, text: CharSequence?) {
        vText.text = text
        vText.visibility = if (ToolsText.empty(text)) GONE else VISIBLE
    }

    fun setOnClickAndLongClickCoordinates(v: View, onClick: (ClickEvent) -> Unit, onLongClick: (ClickEvent) -> Unit) {

        val clickScreenX = Item(0f)
        val clickScreenY = Item(0f)


        v.setOnTouchListener { _, event ->
            clickScreenX.a = event.x
            clickScreenY.a = event.y
            false
        }

        v.setOnClickListener { onClick.invoke(ClickEvent(v, clickScreenX.a, clickScreenY.a)) }
        v.setOnLongClickListener {
            onLongClick.invoke(ClickEvent(v, clickScreenX.a, clickScreenY.a))
            true
        }

    }

    class ClickEvent(val view:View, val x:Float, val y:Float)

    fun setOnClickCoordinates(v: View, onClick: (View, Int, Int) -> Unit) {

        val clickScreenX = Item(0)
        val clickScreenY = Item(0)


        v.setOnTouchListener { _, event ->
            clickScreenX.a = event.x.toInt()
            clickScreenY.a = event.y.toInt()
            false
        }

        v.setOnClickListener { onClick.invoke(v, clickScreenX.a, clickScreenY.a) }
    }

    fun setOnLongClickCoordinates(v: View, onClick: (View, Float, Float) -> Unit) {

        val clickScreenX = Item(0f)
        val clickScreenY = Item(0f)

        v.setOnTouchListener { _, event ->
            clickScreenX.a = event.x
            clickScreenY.a = event.y
            false
        }

        v.setOnLongClickListener {
            onClick.invoke(v, clickScreenX.a, clickScreenY.a)
            true
        }
    }

    fun viewPointAsScreenPoint(view: View, x: Float, y: Float): Array<Int> {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        location[0] = location[0] + x.toInt()
        location[1] = location[1] + y.toInt()
        return location.toTypedArray()
    }

    fun checkHit(view: View, x: Float, y: Float): Boolean {

        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val l = location[0]
        val t = location[1]
        val r = l + view.width
        val b = t + view.height

        return x >= l && y >= t && x <= r && y <= b
    }

    fun <K : View> inflate(@LayoutRes res: Int): K {
        return LayoutInflater.from(SupAndroid.activity!!).inflate(res, null, false) as K
    }

    fun <K : View> inflate(viewContext: Context, @LayoutRes res: Int): K {
        return LayoutInflater.from(viewContext).inflate(res, null, false) as K
    }

    fun <K : View> inflate(parent: ViewGroup, @LayoutRes res: Int): K {
        return LayoutInflater.from(parent.context).inflate(res, parent, false) as K
    }

    fun getRootParent(v: View): ViewGroup? {
        return if (v.parent == null || v.parent !is View)
            v as? ViewGroup
        else
            getRootParent(v.parent as View)
    }

    fun getRootBackground(v: View): Int {
        if (v.background is ColorDrawable) return (v.background as ColorDrawable).color
        return if (v.parent == null || v.parent !is View)
            0
        else
            getRootBackground(v.parent as View)
    }


    fun <K : View> findViewOnParents(v: View, viewId: Int): K? {

        val fView = v.findViewById<K>(viewId)
        if (fView != null) return fView

        return if (v.parent == null || v.parent !is View)
            null
        else
            findViewOnParents(v.parent as View, viewId)
    }

    fun makeTransparentAppBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            AndroidBug5497Workaround.assistActivity(activity)
        }
    }

    fun pxToDp(px: Int) = pxToDp(px.toFloat())

    fun pxToSp(px: Int) = pxToSp(px.toFloat())

    fun dpToPx(dp: Int) = dpToPx(dp.toFloat())

    fun spToPx(sp: Int) = spToPx(sp.toFloat())

    fun pxToDp(px: Float) = px * (px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, Resources.getSystem().displayMetrics))

    fun pxToSp(px: Float) = px * (px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, Resources.getSystem().displayMetrics))

    fun dpToPx(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)

    fun spToPx(sp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)


    //
    //  Keyboard
    //

    fun hideKeyboard() {
        val currentFocus = SupAndroid.activity?.currentFocus
        if (currentFocus != null) {
            val imm = SupAndroid.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboard(view: View) {
        ToolsThreads.main {
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    @JvmOverloads
    fun setTextAnimate(v: TextView, text: String, onAlpha: () -> Unit = {}) {

        if (v.text == text) {
            fromAlpha(v)
            return
        }

        toAlpha(v) {
            v.text = text
            onAlpha.invoke()
            fromAlpha(v)
        }
    }


    fun crossfade(inp: View, out: View) {
        fromAlpha(inp)
        toAlpha(out)
    }

    fun clearAnimation(v: View) {
        v.animate().setListener(null)
        if (v.animation != null)
            v.animation.cancel()
    }

    @JvmOverloads
    fun alpha(v: View, toAlpha: Boolean, onAlpha: () -> Unit = {}) {
        if (toAlpha)
            toAlpha(v, onAlpha)
        else
            fromAlpha(v, onAlpha)
    }

    fun fromAlpha(v: View, onFinish: () -> Unit = {}) {
        fromAlpha(v, ANIMATION_TIME, onFinish)
    }

    @JvmOverloads
    fun fromAlpha(v: View, time: Int = ANIMATION_TIME, onFinish: () -> Unit = {}) {

        clearAnimation(v)

        if (v.alpha == 1f && (v.visibility == GONE || v.visibility == View.INVISIBLE))
            v.alpha = 0f

        v.visibility = VISIBLE

        v.animate()
                .alpha(1f)
                .setDuration(time.toLong())
                .setInterpolator(FastOutLinearInInterpolator())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        onFinish.invoke()
                    }
                })

    }

    fun toAlpha(v: View, onFinish: () -> Unit = {}) {
        toAlpha(v, ANIMATION_TIME, onFinish)
    }

    @JvmOverloads
    fun toAlpha(v: View, time: Int = ANIMATION_TIME, onFinish: () -> Unit = {}) {

        clearAnimation(v)

        v.animate()
                .alpha(0f)
                .setDuration(time.toLong())
                .setInterpolator(LinearOutSlowInInterpolator())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        v.animate().setListener(null)
                        v.alpha = 1f
                        v.visibility = View.INVISIBLE
                        onFinish.invoke()
                    }
                })
    }

    //
    //  Target alpha
    //

    fun targetAlpha(v: View, alpha: Float) {

        clearAnimation(v)

        val time = (255 / ANIMATION_TIME * Math.abs(v.alpha - alpha)).toLong()

        v.animate()
                .alpha(alpha)
                .setDuration(time).interpolator = LinearOutSlowInInterpolator()
    }

    //
    //  State color
    //

    fun setStateColorForText(view: TintableBackgroundView, states: Array<IntArray>, colors: IntArray) {
        setStateColor(view, states, false, true, colors)
    }

    fun setStateColorForBackground(view: TintableBackgroundView, states: Array<IntArray>, colors: IntArray) {
        setStateColor(view, states, true, false, colors)
    }

    fun setStateColor(view: TintableBackgroundView, states: Array<IntArray>, forBack: Boolean, forText: Boolean, colors: IntArray) {
        var colorsArr: IntArray = colors
        if (states.size > colors.size && colors.size == 1) {
            colorsArr = IntArray(states.size)
            Arrays.fill(colorsArr, colors[0])
        }
        val colorStateList = ColorStateList(states, colorsArr)
        if (forBack) view.supportBackgroundTintList = colorStateList
        if (forText && view is TextView) (view as TextView).setTextColor(colorStateList)
    }

    fun setStateColors(view: TintableBackgroundView, states: Array<IntArray>, colorsBack: IntArray, colorsText: IntArray) {
        setStateColorForBackground(view, states, colorsBack)
        setStateColorForText(view, states, colorsText)
    }

    fun getTextColorForState(view: TintableBackgroundView, state: Int): Int {
        if (view !is TextView) throw IllegalArgumentException("View must be instance of TextView")
        val colorStateList = (view as TextView).textColors
        return colorStateList.getColorForState(intArrayOf(state), colorStateList.defaultColor)
    }

    fun getColorForState(view: TintableBackgroundView, state: Int): Int {
        val colorStateList: ColorStateList? = view.supportBackgroundTintList
        return colorStateList?.getColorForState(intArrayOf(state), colorStateList.defaultColor) ?: 0
    }

}
