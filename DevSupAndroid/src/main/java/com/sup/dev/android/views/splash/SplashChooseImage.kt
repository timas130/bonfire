package com.sup.dev.android.views.splash

import android.graphics.Bitmap
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.GridLayoutManager
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.splash.view.SplashViewDialog
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsThreads
import java.io.File
import java.io.IOException

open class SplashChooseImage : SplashRecycler(R.layout.splash_choose_image) {

    private val DP = ToolsView.dpToPx(1).toInt()
    private val myAdapter: RecyclerCardAdapter = RecyclerCardAdapter()
    private val vEmptyText: TextView = findViewById(R.id.vEmptyText)
    private val fabs = ArrayList<View>()

    private var onSelected: (SplashChooseImage, ByteArray, Int) -> Unit = { _, _, _ -> }
    private var imagesLoaded: Boolean = false
    private var spanCount = 3
    private var maxSelectCount = 1
    private var selectedList = ArrayList<File>()
    private var callbackInWorkerThread = false
    private val addedHash = SparseArray<Boolean>()
    private val vFabDoneContainer: View
    private var inited = false

    init {
        vEmptyText.text = SupAndroid.TEXT_ERROR_CANT_FIND_IMAGES

        spanCount = if (ToolsAndroid.isScreenPortrait()) 3 else 6
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)


        setAdapter<SplashRecycler>(myAdapter)

        ToolsThreads.timerMain(4000) {
            if (!imagesLoaded) return@timerMain
            if (isHided()) it.unsubscribe()
            else loadImagesNow()
        }

        addFab(R.drawable.ic_done_white_24dp) { sendAll() }
        vFabDoneContainer = getLastAddedFabContainer()
        ToolsView.setFabColorR(getLastAddedFab(), R.color.green_700)
        addFab(R.drawable.ic_landscape_white_24dp) { openGallery() }
        addFab(R.drawable.ic_insert_link_white_24dp) { showLink() }
        addFab(R.drawable.ic_camera_alt_white_24dp) { openCamera() }

        inited = true
        updateFabs()
    }

    fun updateFabs() {
        if (!inited) return
        if (selectedList.isEmpty()) {
            vContainer.removeView(vFabDoneContainer)
            var offset = 0
            for (i in fabs) {
                if(i == vFabDoneContainer) continue
                if (vContainer.indexOfChild(i) == -1) vContainer.addView(i)
                (i.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = 0
                (i.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(offset).toInt()
                offset += 48
            }
        } else {
            for (i in fabs) vContainer.removeView(i)
            if (vContainer.indexOfChild(vFabDoneContainer) == -1) vContainer.addView(vFabDoneContainer)
        }
    }

    fun addFab(icon: Int, onClick: () -> Unit): SplashChooseImage {
        val vFabContainer: View = ToolsView.inflate(R.layout.z_fab_mini)
        val vFab: ImageView = vFabContainer.findViewById(R.id.vFab)
        fabs.add(vFabContainer)

        vFab.setImageResource(icon)
        vFab.setOnClickListener { onClick.invoke() }
        updateFabs()

        return this
    }

    fun getLastAddedFab() = getLastAddedFabContainer().findViewById(R.id.vFab) as FloatingActionButton
    fun getLastAddedFabContainer() = fabs[fabs.size - 1]

    override fun onShow() {
        super.onShow()
        loadImages()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is SplashViewDialog)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is SplashViewSheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()
    }

    private fun loadImages() {
        if (imagesLoaded) return

        ToolsThreads.main(true) { vRecycler.requestLayout() }   //  Костыль. Иначе улетают кнопки вверх

        ToolsPermission.requestReadPermission({
            imagesLoaded = true
            loadImagesNow()
        }, {
            ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_FILES)
            hide()
        })

    }

    private fun loadImagesNow() {

        val offset = myAdapter.size()
        var addCount = 0
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val cursor = SupAndroid.appContext!!.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC")

        while (cursor != null && cursor.moveToNext()) {
            val file = File(cursor.getString(0))
            val hash = file.hashCode()

            if (addedHash.get(hash) != null) break

            addCount++
            addedHash.put(hash, true)
            myAdapter.add(myAdapter.size() - offset, CardImage(file))
        }

        if (addCount > 0 && offset > 0) {
            vRecycler.scrollToPosition(0)
        }

        vEmptyText.visibility = if (myAdapter.isEmpty) View.VISIBLE else View.GONE
    }

    private fun loadLink(link: String) {
        val progress = ToolsView.showProgressDialog()
        ToolsNetwork.getBytesFromURL(link, onResult = {
            progress.hide()
            if (it == null || !ToolsBytes.isImage(it)) ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
            else {
                if (callbackInWorkerThread) ToolsThreads.thread { onSelected.invoke(this, it, 0) }
                else onSelected.invoke(this, it, 0)
                hide()
            }
        })
    }

    private fun showLink() {
        SplashField()
                .setMediaCallback { w, s ->
                    w.hide()
                    loadLink(s)
                }
                .enableFastCopy()
                .setHint(SupAndroid.TEXT_APP_LINK)
                .setOnEnter(SupAndroid.TEXT_APP_CHOOSE) { _, s -> loadLink(s) }
                .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                .asSheetShow()

    }

    private fun openCamera() {
        ToolsIntent.getCameraImage({
            bytes ->
            try {
                onSelected.invoke(this, bytes, 0)
                hide()
            } catch (e: IOException) {
                err(e)
                ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
            }
        },{
            err(it)
            ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
        })
    }

    private fun openGallery() {
        ToolsBitmap.getFromGallery({ bytes ->
            try {
                onSelected.invoke(this, bytes, 0)
                hide()
            } catch (e: IOException) {
                err(e)
                ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
            }
        }, {
            if(it !is IllegalAccessException) ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
        })
    }

    private fun sendAll() {
        val d = if (selectedList.size > 1 && SupAndroid.TEXT_APP_LOADING != null) ToolsView.showProgressDialog(SupAndroid.TEXT_APP_LOADING + " 1 / " + selectedList.size) else ToolsView.showProgressDialog()
        ToolsThreads.thread {
            for (f in selectedList) {
                if (d is SplashProgressWithTitle) ToolsThreads.main { d.setTitle(SupAndroid.TEXT_APP_LOADING + " ${selectedList.indexOf(f) + 1} / " + selectedList.size) }
                try {

                    val bytes = ToolsFiles.readFile(f)

                    if (callbackInWorkerThread) {
                        onSelected.invoke(this, bytes, selectedList.indexOf(f))
                    } else {
                        val sent = Item(false)
                        ToolsThreads.main {
                            try {
                                onSelected.invoke(this, bytes, selectedList.indexOf(f))
                            } catch (e: Exception) {
                                err(e)
                            }
                            sent.a = true
                        }
                        while (!sent.a) ToolsThreads.sleep(10)
                    }

                } catch (e: Exception) {
                    err(e)
                    ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
                }
            }
            ToolsThreads.main {
                hide()
                d.hide()
            }
        }
    }

    private fun sendWithCrop() {
        val d = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val f = selectedList.get(0)
            try {
                val startBytes = ToolsFiles.readFile(f)
                val startBitmap = ToolsBitmap.decode(startBytes)!!
                ToolsThreads.main {
                    Navigator.to(SCrop(startBitmap) { screen, bitmap, x, y, w, h ->
                        ToolsThreads.thread {
                            try {
                                val bytes = ToolsBitmap.toBytes(bitmap)!!
                                if (callbackInWorkerThread) {
                                    onSelected.invoke(this, bytes, selectedList.indexOf(f))
                                } else {
                                    val sent = Item(false)
                                    ToolsThreads.main {
                                        try {
                                            onSelected.invoke(this, bytes, selectedList.indexOf(f))
                                        } catch (e: Exception) {
                                            err(e)
                                        }
                                        sent.a = true
                                    }
                                    while (!sent.a) ToolsThreads.sleep(10)
                                }
                            } catch (e: Exception) {
                                err(e)
                                ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
                            }
                            ToolsThreads.main {
                                hide()
                                d.hide()
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                err(e)
                ToolsToast.show(SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE)
                ToolsThreads.main {
                    hide()
                    d.hide()
                }
            }
        }
    }

    fun getSelectedCount() = selectedList.size

    //
    //  Setters
    //

    fun setOnSelected(onSelected: (SplashChooseImage, ByteArray, Int) -> Unit): SplashChooseImage {
        this.onSelected = onSelected
        return this
    }

    fun setOnSelectedBitmap(callback: (SplashChooseImage, Bitmap) -> Unit): SplashChooseImage {
        this.onSelected = { _, bytes, _ -> callback.invoke(this, ToolsBitmap.decode(bytes)!!) }
        return this
    }

    fun setMaxSelectCount(maxSelectCount: Int): SplashChooseImage {
        this.maxSelectCount = maxSelectCount
        return this
    }

    fun setCallbackInWorkerThread(callbackInWorkerThread: Boolean): SplashChooseImage {
        this.callbackInWorkerThread = callbackInWorkerThread
        return this
    }

    //
    //  Card
    //

    private inner class CardImage(val file: File) : Card(R.layout.splash_choose_image_card) {

        override fun bindView(view: View) {
            super.bindView(view)
            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vNumContainerTouch: View = view.findViewById(R.id.vNumContainerTouch)

            vImage.setOnClickListener { onClick() }
            vImage.setOnLongClickListener { if (selectedList.isEmpty()) onLongClick() else onClick();true }
            if (maxSelectCount > 1) {
                vNumContainerTouch.setOnClickListener { select() }
            }

            ImageLoader.load(file).size(420, 420).cropSquare().into(vImage)
            val index = adapter.indexOf(this)
            val arg = index % spanCount
            view.setPadding(if (arg == 0) 0 else DP, if (index < spanCount) 0 else DP, if (arg == spanCount - 1) 0 else DP, DP)

            updateIndex()
        }

        fun updateIndex() {
            val view = getView()
            if (view == null) return
            val vNum: TextView = view.findViewById(R.id.vNum)
            val vNumContainer: View = view.findViewById(R.id.vNumContainer)

            val selectIndex = selectedList.indexOf(file)
            vNum.text = if (selectIndex == -1) "   " else " ${selectIndex + 1} "
            vNumContainer.visibility = if (maxSelectCount > 1) View.VISIBLE else View.GONE
            vNumContainer.setBackgroundColor(if (selectIndex == -1) ToolsResources.getColor(R.color.focus_dark) else ToolsResources.getSecondaryColor(view.context))
        }

        fun onClick() {
            if (selectedList.isNotEmpty()) {
                select()
            } else {
                selectedList.add(file)
                sendAll()
            }
        }

        fun onLongClick() {
            selectedList.clear()
            selectedList.add(file)
            sendWithCrop()
        }

        fun select() {
            if (selectedList.contains(file)) {
                selectedList.remove(file)
                updateFabs()
                for (c in myAdapter.get(CardImage::class)) c.updateIndex()
            } else {
                if (selectedList.size >= maxSelectCount) {
                    if (SupAndroid.TEXT_ERROR_MAX_ITEMS_COUNT != null) ToolsToast.show(SupAndroid.TEXT_ERROR_MAX_ITEMS_COUNT)
                    return
                }
                selectedList.add(file)
                updateFabs()
                update()
            }
        }


    }

}
