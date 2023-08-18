package com.sayzen.campfiresdk.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.LayoutRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerKarma
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon

abstract class ViewKarma(
        @LayoutRes res: Int,
        context: Context,
        attrs: AttributeSet?
) : FrameLayout(context, attrs) {

    private val paint = Paint()
    private val green = ToolsResources.getColor(R.color.green_700)
    private val red = ToolsResources.getColor(R.color.red_700)

    private val vDown: ViewIcon
    private val vUp: ViewIcon
    private val vText: TextView
    private val vTouch: View

    private val textColor: Int
    private var rateStartTime = 0L
    private var rateIsUp = false
    private var clickDrawable: Drawable? = null

    init {

        SupAndroid.initEditMode(this)

        addView(ToolsView.inflate(context, res))
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ToolsView.dpToPx(3)

        vDown = findViewById(R.id.view_karma_down)
        vUp = findViewById(R.id.view_karma_up)
        vText = findViewById(R.id.vViewKarmaText)
        vTouch = findViewById(R.id.vTouch)

        clickDrawable = vTouch.background
        textColor = vText.currentTextColor
        setWillNotDraw(false)

    }

    fun clear(){
        update(0,0,0,0,0)
    }

    fun update(publicationId: Long,
               karmaCount: Long,
               myKarma: Long,
               creatorId: Long,
               publicationStatus: Long,
               onRate: ((Boolean) -> Unit) = {},
               onRateClicked: (() -> Unit) = {}
    ) {
        this.rateStartTime = ControllerKarma.getStartTime(publicationId)
        this.rateIsUp = ControllerKarma.getIsUp(publicationId)

        vDown.setFilter(if (myKarma < 0 || (rateStartTime != 0L && !rateIsUp)) red else ToolsResources.getColorAttr(R.attr.colorRevers))
        vUp.setFilter(if (myKarma > 0 || (rateStartTime != 0L && rateIsUp)) green else ToolsResources.getColorAttr(R.attr.colorRevers))

        vDown.isEnabled = myKarma <= 0 && !ControllerApi.isCurrentAccount(creatorId) && publicationStatus == API.STATUS_PUBLIC
        vUp.isEnabled = myKarma >= 0 && !ControllerApi.isCurrentAccount(creatorId) && publicationStatus == API.STATUS_PUBLIC
        vText.text = (karmaCount / 100).toString()

        vDown.setOnClickListener {
            if (myKarma == 0L) onRate.invoke(false)
            else onRateClicked.invoke()
        }
        vUp.setOnClickListener {
            if (myKarma == 0L) onRate.invoke(true)
            else onRateClicked.invoke()
        }
        vTouch.setOnClickListener {
            onRateClicked.invoke()
        }

        vDown.setOnLongClickListener {
            onRateClicked.invoke()
            true
        }
        vUp.setOnLongClickListener {
            onRateClicked.invoke()
            true
        }

        //  Порядок важен (после setOnClickListener)
        vDown.isClickable = myKarma == 0L && vDown.isEnabled && publicationStatus == API.STATUS_PUBLIC
        vDown.isLongClickable =  vDown.isClickable
        vUp.isClickable = myKarma == 0L && vUp.isEnabled && publicationStatus == API.STATUS_PUBLIC
        vUp.isLongClickable =  vUp.isClickable
        vTouch.isClickable = (myKarma != 0L || ControllerApi.isCurrentAccount(creatorId)) && publicationStatus == API.STATUS_PUBLIC

        vTouch.background = if (vTouch.isClickable) clickDrawable else null

        vText.setTextColor(if ((karmaCount / 100) == 0L) textColor else if (karmaCount < 0) red else green)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (rateStartTime != 0L && canvas != null) {

            val x: Float
            val y: Float
            val w: Float
            val h: Float
            if (rateIsUp) {
                paint.color = green
                x = vUp.x + vUp.width / 4
                y = vUp.y + vUp.height / 4
                w = vUp.width + x - ((vUp.width / 4) * 2)
                h = vUp.height + y - ((vUp.height / 4) * 2)
            } else {
                paint.color = red
                x = vDown.x + vDown.width / 4
                y = vDown.y + vDown.height / 4
                w = vDown.width + x - ((vDown.width / 4) * 2)
                h = vDown.height + y - ((vDown.height / 4) * 2)
            }
            val start = getStart(rateIsUp)
            val r = 360f * ((System.currentTimeMillis() - rateStartTime).toFloat() / CampfireConstants.RATE_TIME) * getDuration(rateIsUp)

            canvas.drawArc(x, y, w, h, start, r, false, paint)
            invalidate()
        }
    }

    abstract fun getStart(up: Boolean): Float

    abstract fun getDuration(up: Boolean): Float


}
