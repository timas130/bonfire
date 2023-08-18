package com.sup.dev.android.views.screens

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.SeekBar
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.view.SplashViewDialog
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.cropper.ViewCropImage
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.java.tools.ToolsColor

class SCrop(
        bitmap: Bitmap,
        aw: Int,
        ah: Int,
        private val onCrop: ((SCrop, Bitmap, Int, Int, Int, Int) -> Unit)?
) : Screen(R.layout.screen_image_crop) {

    private val vRoot: View = findViewById(R.id.vRoot)
    private val vCropImageView: ViewCropImage = findViewById(R.id.vCrop)
    private val vFinish: View = findViewById(R.id.vFab)
    private val vRotate: View = findViewById(R.id.vRotate)
    private val vAll: View = findViewById(R.id.vAll)
    private val vBack: ViewIcon = findViewById(R.id.vBack)
    private val vBackground: View = findViewById(R.id.vBackground)
    private val vBackgroundPanel: View = findViewById(R.id.vBackgroundPanel)
    private val vBackgroundPanelColor: ViewIcon = findViewById(R.id.vBackgroundPanelColor)
    private val vBackgroundPanelSeek: SeekBar = findViewById(R.id.vBackgroundPanelSeek)
    private val vBackgroundPanelCancel: View = findViewById(R.id.vBackgroundPanelCancel)
    private val vBackgroundPanelEnter: View = findViewById(R.id.vBackgroundPanelEnter)

    private var autoBackOnCrop = true
    private var locked: Boolean = false

    private var dialogProgress: SplashViewDialog? = null

    constructor(bitmap: Bitmap, onCrop: ((SCrop, Bitmap, Int, Int, Int, Int) -> Unit)) : this(bitmap, 0, 0, onCrop) {}

    init {


        disableNavigation()
        statusBarColor = ToolsResources.getColor(R.color.black)
        navigationBarColor = ToolsResources.getColorAttr(android.R.attr.windowBackground)

        val color = ToolsColor.setAlpha(70, (vRoot.background as ColorDrawable).color)
        vBack.setIconBackgroundColor(color)

        if (aw > 0 && ah > 0) vCropImageView.setAspectRatio(aw, ah)
        vCropImageView.setImageBitmap(bitmap)

        vAll.setOnClickListener { vCropImageView.cropRect = Rect(0, 0, bitmap.width, bitmap.height) }

        vRotate.setOnClickListener { vCropImageView.rotatedDegrees = (vCropImageView.rotatedDegrees + 90) % 360 }


        vFinish.setOnClickListener {
            if (onCrop != null) {
                if (autoBackOnCrop)
                    Navigator.back()
                else
                    setLock(false)

                val cropPoints = vCropImageView.cropPoints
                val croppedImage = vCropImageView.croppedImage
                if (croppedImage != null)
                    onCrop.invoke(this, croppedImage, cropPoints[0].toInt(), cropPoints[1].toInt(), (cropPoints[2] - cropPoints[0]).toInt(), (cropPoints[5] - cropPoints[1]).toInt())
            }
        }

        vBackground.visibility = View.GONE

        vBackgroundPanelSeek.max = 100
        vBackgroundPanel.visibility = View.GONE
        vBackgroundPanelColor.setOnClickListener {

        }
        vBackground.setOnClickListener {
            vBackgroundPanel.visibility = View.VISIBLE
            vBackground.visibility = View.GONE
            vAll.visibility = View.GONE
            vFinish.visibility = View.GONE
        }
        vBackgroundPanelCancel.setOnClickListener {
            vBackgroundPanel.visibility = View.GONE
            vBackground.visibility = View.VISIBLE
            vAll.visibility = View.VISIBLE
            vFinish.visibility = View.VISIBLE
        }
        vBackgroundPanelEnter.setOnClickListener {
            vBackgroundPanel.visibility = View.GONE
            vBackground.visibility = View.VISIBLE
            vAll.visibility = View.VISIBLE
            vFinish.visibility = View.VISIBLE
        }
    }

    fun setLock(b: Boolean): SCrop {
        locked = b
        vFinish.isEnabled = !b
        vAll.isEnabled = !b
        if (b) {
            if (locked) dialogProgress = SplashProgressTransparent().asDialogShow()
        } else {
            if (dialogProgress != null) {
                dialogProgress!!.hide()
                dialogProgress = null
            }
        }
        return this
    }

    fun setAutoBackOnCrop(autoBackOnCrop: Boolean): SCrop {
        this.autoBackOnCrop = autoBackOnCrop
        return this
    }

    override fun onBackPressed(): Boolean {
        return locked
    }

    fun back() {
        Navigator.back()
    }

}
