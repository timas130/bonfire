package com.sup.dev.android.views.screens

import android.graphics.drawable.ColorDrawable
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.drawable.DrawableGif
import com.sup.dev.android.views.support.adapters.pager.PagerCardAdapter
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.layouts.LayoutZoom
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorImages
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsThreads

class SImageView private constructor()
    : Screen(R.layout.screen_image_view) {

    private val vRoot: View = findViewById(R.id.vRoot)
    private val vCounterContainer: View = findViewById(R.id.vCounterContainer)
    private val vCounter: TextView = findViewById(R.id.vCounter)
    private val vPager: ViewPager = findViewById(R.id.vPager)
    private val vShare: ViewIcon = findViewById(R.id.vShare)
    private val vDownload: ViewIcon = findViewById(R.id.vDownload)
    private val vBack: ViewIcon = findViewById(R.id.vBack)
    private val vIndicator: ViewPagerIndicatorImages = findViewById(R.id.vIndicator)
    private val adapterIn: PagerCardAdapter = PagerCardAdapter()


    constructor(scrollTo: Int, imageLoaders: Array<ImageLink>) : this() {
        for (loader in imageLoaders) adapterIn.add(Page(loader.getFullImageLoader()?:loader))
        vPager.setCurrentItem(scrollTo, false)
        vCounterContainer.visibility = if (adapterIn.size() > 1) View.VISIBLE else View.GONE
        vIndicator.visibility = if (adapterIn.size() > 1) View.VISIBLE else View.GONE
        vIndicator.imageProvider = { index, v -> vIndicator.setImage(v, imageLoaders[index].getPreviewImageLoader()?.copy()?:imageLoaders[index].copy()) }
        vIndicator.setPagerView(vPager)
    }

    constructor(imageLoader: ImageLink) : this() {
        adapterIn.add(Page(imageLoader))
        vCounterContainer.visibility = if (adapterIn.size() > 1) View.VISIBLE else View.GONE
        vIndicator.visibility = View.GONE
    }

    init {

disableNavigation()
        statusBarColor = 0xFF000000.toInt()
        navigationBarColor = 0xFF000000.toInt()
        statusBarIsLight = statusBarColor > 0xFF70FFFF.toInt()

        val color = ToolsColor.setAlpha(70, (vRoot.background as ColorDrawable).color)
        vDownload.setIconBackgroundColor(color)
        vShare.setIconBackgroundColor(color)
        vBack.setIconBackgroundColor(color)
        vCounterContainer.setBackgroundColor(color)

        vDownload.setOnClickListener { download() }
        vShare.setOnClickListener { share() }
        vPager.adapter = adapterIn
        vPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                updateTitle()
            }

            override fun onPageSelected(p0: Int) {
                for (i in Math.max(0, p0 - 1) until Math.min(adapterIn.size(), p0 + 1)) {
                    (adapterIn[i] as Page).resetZoom()
                }
            }

        })

        updateTitle()
        ToolsThreads.main(true) { updateTitle() }   //  Костыль. Иногда счетчик не обновляется сразу
    }

    private fun toggleInterface() {
        val toAlpha = vBack.visibility == View.VISIBLE

        ToolsView.alpha(vBack, toAlpha)
        ToolsView.alpha(vDownload, toAlpha)
        ToolsView.alpha(vShare, toAlpha)
        ToolsView.alpha(vCounterContainer, toAlpha)
        ToolsView.alpha(vIndicator, toAlpha || adapterIn.size() < 2)
    }

    private fun updateTitle() {
        vCounter.text = "${vPager.currentItem + 1} / ${adapterIn.size()}"
    }

    private fun download() {
        (adapterIn[vPager.currentItem] as Page).download()
    }

    private fun share() {
        (adapterIn[vPager.currentItem] as Page).share()
    }

    private inner class Page constructor(
            private val imageLoader: ImageLink
    ) : Card(R.layout.screen_image_view_page) {

        override fun bindView(view: View) {
            super.bindView(view)
            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vZoom: LayoutZoom = view.findViewById(R.id.vZoom)

            vImage.setImageBitmap(null)
            vImage.isClickable = false
            vZoom.setOnClickListener {
                toggleInterface()
            }

            imageLoader.intoBytes { bytes ->
                if (bytes != null) {
                    if (ToolsBytes.isGif(bytes)) DrawableGif(bytes, vImage) { vImage.setImageDrawable(it) }
                    else vImage.setImageBitmap(ToolsBitmap.decode(bytes))
                }
            }
        }

        fun resetZoom() {
            if (getView() == null) return
            val vZoom: LayoutZoom = getView()!!.findViewById(R.id.vZoom)
            vZoom.reset()
        }

        fun download() {
            val dialog = ToolsView.showProgressDialog(SupAndroid.TEXT_APP_DOWNLOADING)
            ToolsThreads.thread {
                imageLoader.intoBytes { bytes ->
                    if (!ToolsBytes.isGif(bytes))
                        ToolsStorage.saveImageInDownloadFolder(ToolsBitmap.decode(bytes)!!) { }
                    else
                        ToolsStorage.saveFileInDownloadFolder(bytes!!, "gif", { }, { ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES) })

                }
                dialog.hide()
                ToolsToast.show(SupAndroid.TEXT_APP_DOWNLOADED)
            }

        }

        fun share() {

            SplashField()
                    .setHint(SupAndroid.TEXT_APP_SHARE_MESSAGE_HINT)
                    .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                    .setOnEnter(SupAndroid.TEXT_APP_SHARE) { _, text ->
                        ToolsThreads.thread {
                            val dialog = ToolsView.showProgressDialog()
                            imageLoader.intoBitmap { bm ->
                                dialog.hide()
                                if (bm == null) {
                                    ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
                                    return@intoBitmap
                                }
                                ToolsIntent.shareImage(bm, text)
                            }
                        }
                    }
                    .asSheetShow()


        }
    }

}
