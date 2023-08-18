package com.sayzen.campfiresdk.screens.post.create.creators

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageImages
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads

class SplashPageImages(
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page) -> Unit) -> Unit,
        private val card: CardPage,
        private var oldPage: PageImages
) : Splash(R.layout.screen_post_create_images) {

    private val vPageTitle: SettingsField = findViewById(R.id.vPgeTitle)
    private val vAdd: ViewButton = findViewById(R.id.vAdd)
    private val vAttachRecycler: RecyclerView = findViewById(R.id.vAttachRecycler)
    private val vTextEmpty: TextView = findViewById(R.id.vTextEmpty)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val adapter: RecyclerCardAdapter = RecyclerCardAdapter()

    init {

        vEnter.isEnabled = false

        vAdd.text = t(API_TRANSLATE.app_add)
        vEnter.text = t(API_TRANSLATE.app_to_finish)

        vTextEmpty.text = t(API_TRANSLATE.post_create_images_empty)
        vPageTitle.setHint(t(API_TRANSLATE.app_name_s))
        vPageTitle.setHint("${t(API_TRANSLATE.app_naming)} (${t(API_TRANSLATE.app_not_required)})")
        vPageTitle.addOnTextChanged { update() }
        vEnter.setOnClickListener { onEnter() }
        vAdd.setOnClickListener { addImage() }

        vAttachRecycler.layoutManager = LinearLayoutManager(vAttachRecycler.context, LinearLayoutManager.HORIZONTAL, false)
        vAttachRecycler.adapter = adapter
        adapter.setCardW(ViewGroup.LayoutParams.WRAP_CONTENT)

        vPageTitle.setText(oldPage!!.title)
        if (oldPage.imagesIds.isNotEmpty()) vTextEmpty.visibility = View.GONE
        for (element in oldPage.imagesIds) addBytes(null, element)

        update()
    }

    private fun update() {
        vAdd.isEnabled = adapter.size() < API.PAGE_IMAGES_MAX_COUNT
        vEnter.isEnabled =  vPageTitle.getText().length <= API.PAGE_IMAGES_TITLE_MAX
    }

    private fun onEnter() {
        oldPage.title = vPageTitle.getText()
        requestChangePage.invoke(oldPage, card, null, this) { page ->
            oldPage = page as PageImages
        }
    }

    private fun removeImage(index: Int) {
        val pageNew = PageImages()
        pageNew.insertImages = emptyArray()
        pageNew.insertImagesMini = emptyArray()
        pageNew.title = oldPage.title
        pageNew.imagesIds = oldPage.imagesIds
        pageNew.imagesMiniIds = oldPage.imagesMiniIds
        pageNew.removePageIndex = index

        SplashAlert()
                .setText(t(API_TRANSLATE.post_page_images_remove_confirm))
                .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                .setAutoHideOnEnter(false)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { ww ->
                    requestChangePage.invoke(pageNew, card, null, ww) { page ->
                        oldPage = page as PageImages
                        vTextEmpty.visibility = if (oldPage.imagesIds.isNotEmpty()) View.GONE else View.VISIBLE
                        adapter.remove(index)
                        update()
                    }
                }
                .asSheetShow()

    }

    private fun addImage() {
        SplashChooseImage()
                .setCallbackInWorkerThread(true)
                .setMaxSelectCount(API.PAGE_IMAGES_MAX_COUNT)
                .setOnSelected { _, bytes, _ ->
                    sendImage(ToolsBitmap.decode(bytes), bytes, null)
                }
                .asSheetShow()
    }

    private fun sendImage(bitmap: Bitmap?, bytes: ByteArray?, dialog: Splash?, replaceIndex: Int = -1) {
        if (bitmap == null || bytes == null) {
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            return
        }
        val img = if (ToolsBytes.isGif(bytes)) ToolsGif.resize(bytes, API.PAGE_IMAGES_SIDE_GIF, API.PAGE_IMAGES_SIDE_GIF, null, null, null, null, true)
        else ControllerApi.toBytesNow(ToolsBitmap.keepMaxSides(bitmap, API.PAGE_IMAGES_SIDE), API.PAGE_IMAGES_WEIGHT)

        val imgMini = if (ToolsBytes.isGif(bytes)) ToolsGif.resize(bytes, API.PAGE_IMAGES_SIDE_GIF, API.PAGE_IMAGES_SIDE_GIF, null, null, null, null, true)
        else ControllerApi.toBytesNow(ToolsBitmap.keepMaxSides(bitmap, API.PAGE_IMAGES_MINI_SIDE), API.PAGE_IMAGES_MINI_WEIGHT)

        if (img == null || imgMini == null) {
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            return
        }

        if ((ToolsBytes.isGif(img) && img.size > API.PAGE_IMAGE_GIF_WEIGHT) || ToolsBytes.isGif(imgMini) && imgMini.size > API.PAGE_IMAGE_GIF_WEIGHT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
            dialog?.hide()
            return
        }

        sendImageNow(img, imgMini, bytes, dialog, replaceIndex)
    }

    private fun sendImageNow(img: ByteArray, imgMini: ByteArray, rawBytes: ByteArray, dialog: Splash?, replaceIndex: Int = -1) {
        val pageNew = PageImages()
        pageNew.insertImages = Array(1) { img }
        pageNew.insertImagesMini = Array(1) { imgMini }
        pageNew.replacePageIndex = replaceIndex
        pageNew.title = oldPage.title
        pageNew.imagesIds = oldPage.imagesIds
        pageNew.imagesMiniIds = oldPage.imagesMiniIds

        requestChangePage.invoke(pageNew, card, null, dialog) { page ->
            oldPage = page as PageImages
            ToolsThreads.main {
                vTextEmpty.visibility = View.GONE
                addBytes(rawBytes, oldPage.imagesMiniIds[oldPage.imagesMiniIds.size - 1], replaceIndex)
                update()
            }
        }
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onEnter()
        return false
    }

    //
    //  Card
    //

    private fun onSupportScreenHide() {
        ToolsThreads.main(100) { asSheetShow() }
    }

    private fun addBytes(bytes: ByteArray?, id: Long, replaceIndex: Int = -1) {
        val card = ItemCard(bytes, id)
        if (replaceIndex == -1) {
            adapter.add(card)
            vAttachRecycler.scrollToPosition(adapter.size() - 1)
        } else {
            adapter.replace(replaceIndex, card)
            vAttachRecycler.scrollToPosition(replaceIndex)
        }
        update()
    }

    private inner class ItemCard(
            var bytes: ByteArray?,
            var id: Long
    ) : Card(R.layout.view_attach_image) {

        override fun bindView(view: View) {
            super.bindView(view)

            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vCrop: View = view.findViewById(R.id.vCrop)
            val vRemove: View = view.findViewById(R.id.vRemove)
            setImage(vImage)

            vImage.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(id)).setOnHide { onSupportScreenHide() }) }
            vCrop.setOnClickListener { crop() }
            vRemove.setOnClickListener {
                removeImage(adapter!!.indexOf(this))
            }
        }

        private fun setImage(vImage: ImageView) {
            val dp = ToolsView.dpToPx(128).toInt()
            if (bytes != null) {
                vImage.setImageBitmap(ToolsBitmap.decode(bytes, dp, dp, null, dp, dp))
            } else {
                ImageLoader.load(id).intoBytes { bytes -> vImage.setImageBitmap(ToolsBitmap.decode(bytes, dp, dp, null, dp, dp)) }
            }
        }

        private fun crop() {
            val dialog = ToolsView.showProgressDialog()

            if (bytes != null) {
                crop(bytes!!, dialog)
            } else {
                ImageLoader.load(id).intoBytes { bytes ->
                    crop(bytes!!, dialog)
                }
            }
        }

        private fun crop(bytes: ByteArray, dialog: Splash) {
            ToolsThreads.thread {
                val decoded = ToolsBitmap.decode(bytes)
                dialog.hide()
                if (decoded == null) {
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@thread
                }
                ToolsThreads.main {
                    Navigator.to(SCrop(decoded) { _, bitmap, x, y, w, h ->
                        ToolsThreads.main(150) {
                            val dialogV = ToolsView.showProgressDialog()
                            ToolsThreads.thread {
                                if (ToolsBytes.isGif(bytes)) {
                                    val resizedBytes = ToolsGif.resize(bytes, API.PAGE_IMAGES_SIDE_GIF, API.PAGE_IMAGES_SIDE_GIF, x, y, w, h, true)
                                    sendImage(bitmap, resizedBytes, dialogV, adapter!!.indexOf(this))
                                } else {
                                    sendImage(bitmap, ControllerApi.toBytesNow(bitmap, API.PAGE_IMAGES_WEIGHT), dialogV, adapter!!.indexOf(this))
                                }
                            }
                        }
                    }.setOnHide { onSupportScreenHide() })
                }
            }
        }

    }


}
