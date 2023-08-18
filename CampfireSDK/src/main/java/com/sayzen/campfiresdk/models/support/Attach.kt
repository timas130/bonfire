package com.sayzen.campfiresdk.models.support

import android.graphics.Bitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.views.SplashStickers
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsGif
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsThreads

class Attach(
        val vAttach: ViewIcon,
        val vAttachRecycler: RecyclerView,
        val onUpdate: () -> Unit = {},
        val onSupportScreenHide: () -> Unit = {},
        val onStickerSelected: (PublicationSticker) -> Unit = {}
) {

    private var adapter = RecyclerCardAdapter()
    private var enabled = true
    private var inited = false

    init {
        vAttach.setOnClickListener { onAttachClicked() }
        vAttach.setOnLongClickListener { onStickersClicked(null); true }
        vAttachRecycler.layoutManager = LinearLayoutManager(vAttachRecycler.context, LinearLayoutManager.HORIZONTAL, false)
        clear()
        inited = true
    }

    fun clear() {
        adapter.clear()
        adapter = RecyclerCardAdapter()
        adapter.setCardW(RecyclerView.LayoutParams.WRAP_CONTENT)
        vAttachRecycler.adapter = adapter
        updateAttach()
    }

    private fun updateAttach() {
        vAttachRecycler.visibility = if (isHasContent()) View.VISIBLE else View.GONE
        vAttach.isEnabled = adapter.size() < API.CHAT_MESSAGE_MAX_IMAGES_COUNT && enabled
        if (inited) onUpdate.invoke()
    }

    private fun addBytes(bytes: ByteArray) {
        adapter.add(ItemCard(bytes))
        vAttachRecycler.scrollToPosition(adapter.size() - 1)
        updateAttach()
    }

    fun setImageBitmapNow(it: Bitmap, dialog: Splash) {

        val bitmap = ToolsBitmap.keepMaxSides(it, API.CHAT_MESSAGE_IMAGE_SIDE)
        val bytes = ControllerApi.toBytesNow(bitmap, API.CHAT_MESSAGE_IMAGE_WEIGHT)

        ToolsThreads.main {
            dialog.hide()
            if (bytes == null) {
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            } else {
                addBytes(bytes)
            }
        }


    }

    fun attachUrl(url: String, dialog: Splash, onError: () -> Unit) {
        ToolsNetwork.getBytesFromURL(url){ bytes->
            try {
                ToolsThreads.thread {
                    attachBytes_inWorker(bytes, dialog)
                }
            } catch (e: Exception) {
                ToolsBitmap.getFromURL(url) { bitmap ->
                    try {
                        ToolsThreads.thread {
                            attachBytes_inWorker(ToolsBitmap.toPNGBytes(bitmap!!), dialog)
                        }
                    } catch (e: Exception) {
                        err(e)
                        dialog.hide()
                        onError.invoke()
                    }
                }
            }
        }
    }

    private fun onAttachClicked() {
        val widget = SplashChooseImage()
        widget.setMaxSelectCount(API.CHAT_MESSAGE_MAX_IMAGES_COUNT)
                .setCallbackInWorkerThread(true)
                .setOnSelected { _, bytes, _ ->
                    attachBytes_inWorker(bytes, null)
                    onSupportScreenHide.invoke()
                }
                .addFab(R.drawable.ic_mood_white_24dp) { onStickersClicked(widget) }
                .asSheetShow()
    }

    private fun onStickersClicked(widget: SplashChooseImage?) {
        SplashStickers()
                .onSelected { onStickerSelected.invoke(it) }
                .asSheetShow()
        widget?.hide()
    }

    fun attachBytes_inWorker(bytes: ByteArray?, dialog: Splash?) {
        if (bytes == null) {
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            ToolsThreads.main{dialog?.hide()}
            return
        }
        if (ToolsBytes.isGif(bytes)) {
            val bytesScaled = ToolsGif.resize(bytes, API.CHAT_MESSAGE_IMAGE_SIDE_GIF, API.CHAT_MESSAGE_IMAGE_SIDE_GIF, null, null, null, null, true)
            if (bytesScaled.size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) {
                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                dialog?.hide()
                return
            }
            ToolsThreads.main {
                dialog?.hide()
                addBytes(bytesScaled)
            }
        } else {
            val bytes2 = ToolsBitmap.decode(bytes)
            if (bytes2 == null) {
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                return
            }

            val bytesScaled = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(bytes2, API.CHAT_MESSAGE_IMAGE_SIDE), API.CHAT_MESSAGE_IMAGE_WEIGHT)

            ToolsThreads.main {
                dialog?.hide()
                if (bytesScaled == null) {
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@main
                }
                addBytes(bytesScaled)
            }
        }
    }

    //
    //  Setters
    //

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        updateAttach()
    }

    //
    //  Getters
    //

    fun getBytes() = Array(adapter.size()) { (adapter[it] as ItemCard).bytes }

    fun isHasContent() = !adapter.isEmpty

    //
    //  Card
    //

    private inner class ItemCard(
            var bytes: ByteArray
    ) : Card(R.layout.view_attach_image) {

        override fun bindView(view: View) {
            super.bindView(view)

            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vCrop: View = view.findViewById(R.id.vCrop)
            val vRemove: View = view.findViewById(R.id.vRemove)
            setImage(vImage)

            vImage.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(bytes)).setOnHide(onSupportScreenHide)) }
            vCrop.setOnClickListener { crop(vImage) }
            vRemove.setOnClickListener {
                if (adapter != null) adapter!!.remove(this)
                updateAttach()
            }
        }

        private fun setImage(vImage: ImageView) {
            val dp = ToolsView.dpToPx(128).toInt()
            val bm = ToolsBitmap.decode(bytes, dp, dp, null, dp, dp)
            vImage.setImageBitmap(bm)
        }

        private fun crop(vImage: ImageView) {
            val dialog = ToolsView.showProgressDialog()

            ToolsThreads.thread {
                val decoded = ToolsBitmap.decode(bytes)
                dialog.hide()
                if (decoded == null) {
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@thread
                }
                ToolsThreads.main {
                    Navigator.to(SCrop(decoded) { _, bitmap, _, _, _, _ ->
                        val bytesScaled = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(bitmap, API.CHAT_MESSAGE_IMAGE_SIDE), API.CHAT_MESSAGE_IMAGE_WEIGHT)
                        if (bytesScaled == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@SCrop
                        }
                        this.bytes = bytesScaled
                        setImage(vImage)

                    }.setOnHide(onSupportScreenHide))
                }
            }
        }

    }

}