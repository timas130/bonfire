package com.sayzen.campfiresdk.screens.post.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageImage
import com.dzen.campfire.api.models.publications.post.PageText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sayzen.campfiresdk.screens.post.create.creators.CardMove
import com.sayzen.campfiresdk.screens.post.create.creators.SplashAdd
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class PostCreator(
        val oldPages: Array<Page>,
        val vRecycler: RecyclerView,
        val vAdd: FloatingActionButton,
        val vFinish: FloatingActionButton,
        val onBackEmptyAndNewerAdd: () -> Unit,
        val requestPutPage: (Splash?, Array<Page>, (Array<Page>) -> Unit, () -> Unit) -> Unit,
        val requestRemovePage: (Array<Int>, () -> Unit) -> Unit,
        val requestChangePage: (Splash?, Page, Int, (Page) -> Unit) -> Unit,
        val requestMovePage: (Int, Int, () -> Unit) -> Unit
):PagesContainer {

    enum class ActionType {
        STOP, ADD
    }

    private val adapter: RecyclerCardAdapter
    private var actionType: ActionType? = null
    private val widgetAdd: SplashAdd
    private var newerAdd = true

    val pages: Array<Page>
        get() {
            val cards = adapter.get(CardPage::class)
            val pages = arrayOfNulls<Page>(cards.size)
            for (i in cards.indices) pages[i] = cards[i].page
            return ToolsMapper.asNonNull(pages)
        }

    init {

        widgetAdd = SplashAdd(
                { page, screen, widget, mapper, onFinish -> putPage(page, screen, widget, mapper, onFinish) },
                { page, card, screen, widget, onFinish -> changePage(page, card, screen, widget, onFinish) }
                , onBackEmptyAndNewerAdd)
        adapter = RecyclerCardAdapter()
        adapter.addItemsChangeListener { updateFinishEnabled() }
        adapter.add(CardSpace(72))

        vRecycler.layoutManager = LinearLayoutManager(vRecycler.context)
        vRecycler.adapter = adapter
        vRecycler.scrollToPosition(adapter.size() - 1)

        for (p in oldPages) addPage(CardPage.instance(this, p))

        vAdd.setOnClickListener { onFabClicked() }

        setActionType(ActionType.ADD)

        updateFinishEnabled()
    }


    //
    //  View
    //

    private fun updateFinishEnabled() {
        ToolsView.setFabEnabledR(vFinish, adapter.size() > 1, R.color.green_700)
    }

    fun setActionType(actionType: ActionType) {
        this.actionType = actionType
        if (actionType == ActionType.STOP)
            vAdd.setImageResource(R.drawable.ic_clear_white_24dp)
        else
            vAdd.setImageResource(R.drawable.ic_add_white_24dp)
    }

    //
    //  Actions
    //

    private fun addPage(c: CardPage) {
        adapter.add(adapter.size() - 1, c.setEditMod(true, { c1: CardPage -> this.startMove(c1) }, { c2: CardPage -> widgetAdd.changePage(c2) }, { c3: CardPage -> this.removePage(c3) }))
        ControllerPost.openAllSpoilers(adapter)
    }

    private fun startMove(c: CardPage) {
        stopMove()
        setActionType(ActionType.STOP)
        val startPosition = adapter.indexOf(c)
        for (i in adapter.size() - 1 downTo 0) {
            if (i == startPosition || i == startPosition + 1) continue
            adapter.add(i, CardMove { movePage(c, i) })
        }
        for(page in adapter.get(CardPage::class)){
            page.editMode = false
            page.update()
        }
    }

    private fun stopMove() {
        ControllerPost.openAllSpoilers(adapter)
        setActionType(ActionType.ADD)
        var i = 0
        while (i < adapter.size()) {
            if (adapter[i] is CardMove)
                adapter.remove(i--)
            i++
        }
        for(page in adapter.get(CardPage::class)){
            page.editMode = true
            page.update()
        }
    }

    fun hideMenu(){
        widgetAdd.hide()
    }

    fun isMove() = actionType == ActionType.STOP

    private fun onFabClicked() {
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        if (actionType == ActionType.STOP)
            stopMove()
        else
            widgetAdd.asSheetShow()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K : Page, N : CardPage> onPageAdd(screen: Screen?, pages: Array<Page>, mapper: (K) -> N): CardPage {
        if (screen != null) Navigator.remove(screen)
        val card = mapper.invoke(pages[0] as K)
        addPage(card)
        ToolsThreads.main(200) { vRecycler.scrollToPosition((vRecycler.adapter as RecyclerCardAdapter).indexOf(card) + 1) }
        return card
    }

    private fun onChangePage(card: CardPage, screen: Screen?, page: Page) {
        if (screen != null) Navigator.remove(screen)
        card.page = page
        card.update()
    }


    //
    //  Requests
    //

    private fun putPage(page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) {
        newerAdd = false
        screen?.isEnabled = false

        requestPutPage.invoke(splash, arrayOf(page), { pages ->
            val card = onPageAdd(screen, pages, mapper)
            onFinish.invoke(card)
        }, {
            screen?.isEnabled = true
        })
    }

    private fun removePage(c: CardPage) {
        requestRemovePage.invoke(arrayOf(adapter.indexOf(c))) {
            adapter.remove(c)
            ControllerPost.openAllSpoilers(adapter)
        }
    }

    private fun changePage(page: Page, card: CardPage, screen: Screen?, splash: Splash?, onFinish: (Page) -> Unit) {
        requestChangePage.invoke(splash, page, adapter.indexOf(card)) { page ->
            onFinish.invoke(page)
            onChangePage(card, screen, page)
        }
    }

    private fun movePage(c: CardPage, index: Int) {
        val currentIndex = adapter.get(CardPage::class).indexOf(c)
        val targetIndex = if (currentIndex > index) index else index - 1
        requestMovePage(currentIndex, targetIndex) {
            stopMove()
            adapter.remove(c)
            adapter.add(targetIndex, c)
            ControllerPost.openAllSpoilers(adapter)
        }
    }

    //
    //  Share
    //

    fun addText(text: String, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        val page = PageText()
        page.text = text
        page.size = PageText.SIZE_0
        putPage(page, null, null, { CardPageText(this, it as PageText) }, {
            onAdd.invoke()
        })
    }

    fun addImage(image: Uri, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            ToolsBitmap.getFromUri(image, {
                if (it == null) {
                    w.hide()
                    ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                    return@getFromUri
                }
                page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(it, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
                ToolsThreads.main {
                    putPage(page, null, w, { CardPageImage(this, it as PageImage) }, {
                        onAdd.invoke()
                    })
                }
            }, {
                w.hide()
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            })
        }
    }

    fun addImage(image: Bitmap, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(image, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
            ToolsThreads.main {
                putPage(page, null, w, { CardPageImage(this, it as PageImage) }, {
                    onAdd.invoke()
                })
            }
        }
    }

    //
    //  Getters
    //

    fun isNewerAdd() = newerAdd

    //
    //  Pages Container
    //

    override fun getPagesArray() = pages
    override fun getSourceType() = API.PAGES_SOURCE_TYPE_POST
    override fun getSourceId() = 0L
    override fun getSourceIdSub() = 0L


}