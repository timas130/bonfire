package com.sayzen.campfiresdk.screens.post.create.creators

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageTable
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageTable
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.splash.*
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.table.ViewTable
import com.sup.dev.android.views.views.table.ViewTableCell
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads

class SCreatePageTable(
        private val requestPutPage: (page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page) -> Unit) -> Unit,
        private var card: CardPage?,
        private var oldPage: PageTable?
) : Screen(R.layout.screen_post_create_table) {

    private val vCreate: Button = findViewById(R.id.vCreate)
    private val vTable: ViewTable = findViewById(R.id.vTable)
    private val vRemoveMode: ViewIcon = findViewById(R.id.vRemoveMode)
    private val vBorderLeft: ViewIcon = findViewById(R.id.vBorderLeft)
    private val vBorderTop: ViewIcon = findViewById(R.id.vBorderTop)
    private val vBorderRight: ViewIcon = findViewById(R.id.vBorderRight)
    private val vBorderBottom: ViewIcon = findViewById(R.id.vBorderBottom)
    private var removeMode = false

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.post_page_table))
        vCreate.text = t(API_TRANSLATE.app_create)

        vTable.textProcessor = { _, _, vText -> ControllerLinks.makeLinkable(vText) }

        vBorderLeft.setOnClickListener {
            if (removeMode)
                checkAndConfirmRemoveColumn(0) {
                    sendChangesMove(-1, 0)
                    vTable.setColumnsCount(vTable.getColumnsCount() - 1, false)
                }
            else {
                sendChangesMove(1, 0)
                vTable.setColumnsCount(vTable.getColumnsCount() + 1, false)
            }
            update()
        }
        vBorderRight.setOnClickListener {
            if (removeMode)
                checkAndConfirmRemoveColumn(vTable.getColumnsCount() - 1) {
                    sendChangesMove(0, 0)
                    vTable.setColumnsCount(vTable.getColumnsCount() - 1, true)
                }
            else {
                sendChangesMove(0, 0)
                vTable.setColumnsCount(vTable.getColumnsCount() + 1, true)
            }

            update()
        }
        vBorderTop.setOnClickListener {
            if (removeMode) checkAndConfirmRemoveRow(0) {
                sendChangesMove(0, -1)
                vTable.removeRow(0)
            }
            else {
                sendChangesMove(0, 1)
                vTable.createRow(false)
            }
            update()
        }
        vBorderBottom.setOnClickListener {
            if (removeMode) checkAndConfirmRemoveRow(vTable.getRowsCount() - 1) {
                sendChangesMove(0, 0)
                vTable.removeRow(vTable.getRowsCount() - 1)
            }
            else {
                sendChangesMove(0, 0)
                vTable.createRow(true)
            }
            update()
        }
        vRemoveMode.setOnClickListener {
            removeMode = !removeMode
            vRemoveMode.setIconBackgroundColor(if (removeMode) ToolsResources.getColor(R.color.red_700) else 0x00000000)
            update()
        }

        vTable.setOnCellClicked { v, x, y ->
            SplashMenu()
                    .add(t(API_TRANSLATE.app_clear)) { clear(v) }.condition(v.hasContent())
                    .add(t(API_TRANSLATE.app_text)) { createText(v) }
                    .add(t(API_TRANSLATE.app_image)) { createImage(v) }
                    .asPopupShow(v, x, y)
        }

        if (oldPage != null) {
            vTable.setColumnsCount(oldPage!!.columnsCount, true)
            vTable.createRows(oldPage!!.rowsCount, true)
            for (c in oldPage!!.cells) {
                if (c.type == PageTable.CELL_TYPE_TEXT) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentText(c.text)
                if (c.type == PageTable.CELL_TYPE_IMAGE) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentImageId(c.imageId)
            }
            vCreate.setText(t(API_TRANSLATE.app_save))
        } else {
            vTable.setColumnsCount(3, true)
            vTable.createRows(3, true)
        }

        vCreate.setOnClickListener { _ -> onEnter() }
        update()
    }

    override fun onBackPressed(): Boolean {
        onEnter()
        return true
    }

    private fun update() {
        if (removeMode) {
            vBorderLeft.isEnabled = vTable.getColumnsCount() > 1
            vBorderRight.isEnabled = vTable.getColumnsCount() > 1
            vBorderTop.isEnabled = vTable.getRowsCount() > 1
            vBorderBottom.isEnabled = vTable.getRowsCount() > 1
        } else {
            vBorderLeft.isEnabled = vTable.getColumnsCount() < API.PAGE_TABLE_MAX_COLUMNS
            vBorderRight.isEnabled = vTable.getColumnsCount() < API.PAGE_TABLE_MAX_COLUMNS
            vBorderTop.isEnabled = vTable.getRowsCount() < API.PAGE_TABLE_MAX_ROWS
            vBorderBottom.isEnabled = vTable.getRowsCount() < API.PAGE_TABLE_MAX_ROWS
        }
    }

    private fun onEnter() {
        if (oldPage == null) {
            Navigator.back()
            return
        }

        oldPage!!.title = ""
        oldPage!!.columnsCount = vTable.getColumnsCount()
        oldPage!!.rowsCount = vTable.getRowsCount()

        requestChangePage.invoke(oldPage!!, card!!, this, ToolsView.showProgressDialog()) { page ->
            this.oldPage = page as PageTable
        }
    }


    private fun sendChangesMove(addHorizontal: Int, addVertical: Int) {
        if (oldPage == null) return
        val dialog = ToolsView.showProgressDialog()

        oldPage!!.title = ""
        oldPage!!.columnsCount = vTable.getColumnsCount() + addHorizontal
        oldPage!!.rowsCount = vTable.getRowsCount() + addVertical

        for(cell in oldPage!!.cells){
            cell.columnIndex += addHorizontal
            cell.rowIndex += addVertical
        }

        requestChangePage.invoke(oldPage!!, card!!, null, dialog) { page ->
            this.oldPage = page as PageTable
        }
    }

    private fun sendChanges(cell: PageTable.Cell?, vCell: ViewTableCell, dialog: Splash, onCreated: () -> Unit) {

        cell?.rowIndex = vCell.getRowIndex()
        cell?.columnIndex = vCell.getColumnIndex()
        val page = if (oldPage == null) PageTable() else oldPage!!

        if (cell != null) {
            page.cells = page.cells.filterNot {
                it.rowIndex == vCell.getRowIndex() && it.columnIndex == vCell.getColumnIndex()
            }.toTypedArray()
            page.cells = ToolsCollections.add(cell, page.cells)
        } else {
            val list = ArrayList<PageTable.Cell>()
            for (c in page.cells) if (c.rowIndex != vCell.getRowIndex() || c.columnIndex != vCell.getColumnIndex()) list.add(c)
            page.cells = list.toTypedArray()
        }

        page.columnsCount = vTable.getColumnsCount()
        page.rowsCount = vTable.getRowsCount()

        if (oldPage == null) {
            requestPutPage.invoke(page, null, dialog, { CardPageTable(null, it as PageTable) }) { card ->
                this.card = card
                this.oldPage = card.page as PageTable
                onCreated.invoke()
            }
        } else {
            requestChangePage.invoke(page, card!!, null, dialog) { page ->
                this.oldPage = page as PageTable
                onCreated.invoke()
            }
        }
    }

    private fun checkAndConfirmRemoveRow(index: Int, onConfirm: () -> Unit) {
        val row = vTable.getRow(index)
        if (row != null) {
            for (c in row.getCells()) {
                if (c.hasContent()) {
                    SplashAlert()
                            .setText(t(API_TRANSLATE.post_page_tale_remove_confirm))
                            .setOnCancel(t(API_TRANSLATE.app_cancel))
                            .setOnEnter(t(API_TRANSLATE.app_remove)) { onConfirm.invoke() }
                            .asSheetShow()
                    return
                }
            }
        }
        onConfirm.invoke()
    }

    private fun checkAndConfirmRemoveColumn(index: Int, onConfirm: () -> Unit) {
        for (c in vTable.getColumnCells(index)) {
            if (c.hasContent()) {
                SplashAlert()
                        .setText(t(API_TRANSLATE.post_page_tale_remove_confirm))
                        .setOnCancel(t(API_TRANSLATE.app_cancel))
                        .setOnEnter(t(API_TRANSLATE.app_remove)) { onConfirm.invoke() }
                        .asSheetShow()
                return
            }
        }
        onConfirm.invoke()
    }

    //
    //  Creation
    //

    private fun clear(vCell: ViewTableCell) {
        SplashAlert()
                .setText(t(API_TRANSLATE.post_page_tale_clear_confirm))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_clear)) { sendChanges(null, vCell, ToolsView.showProgressDialog()) { vCell.clear() } }
                .asSheetShow()
    }

    private fun createText(vCell: ViewTableCell) {
        SplashField()
                .setMin(1)
                .setHint(t(API_TRANSLATE.app_text))
                .setText(vCell.getText())
                .setMax(API.PAGE_TABLE_MAX_TEXT_SIZE)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_add)) { _, text ->
                    val cell = PageTable.Cell()
                    cell.type = PageTable.CELL_TYPE_TEXT
                    cell.text = text
                    sendChanges(cell, vCell, ToolsView.showProgressDialog()) {
                        vCell.setContentText(text)
                    }
                }
                .asSheetShow()
    }

    private fun createImage(vCell: ViewTableCell) {
        SplashChooseImage()
                .setCallbackInWorkerThread(true)
                .setMaxSelectCount(API.PAGE_IMAGES_MAX_COUNT)
                .setOnSelected { _, bytes, _ ->
                    val dialog = ToolsView.showProgressDialog()
                    ToolsThreads.thread { createImageStep2(vCell, bytes, dialog) }
                }
                .asSheetShow()
    }

    private fun createImageStep2(vCell: ViewTableCell, bytes: ByteArray?, dialog: Splash) {

        if (bytes == null) {
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            dialog.hide()
            return
        }
        val img = if (ToolsBytes.isGif(bytes)) ToolsGif.resize(bytes, API.PAGE_TABLE_MAX_IMAGE_SIDE_GIF, API.PAGE_TABLE_MAX_IMAGE_SIDE_GIF, null, null, null, null, true)
        else {
            val decode = ToolsBitmap.decode(bytes)
            if (decode == null) {
                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                dialog.hide()
                return
            }
            ControllerApi.toBytesNow(ToolsBitmap.keepMaxSides(decode, API.PAGE_TABLE_MAX_IMAGE_SIDE), API.PAGE_TABLE_MAX_IMAGE_WEIGHT)
        }

        if (img == null) {
            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
            dialog.hide()
            return
        }

        if (ToolsBytes.isGif(img) && img.size > API.PAGE_IMAGE_GIF_WEIGHT) {
            ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
            dialog.hide()
            return
        }

        val cell = PageTable.Cell()
        cell.type = PageTable.CELL_TYPE_IMAGE
        cell.insertImage = img
        ToolsThreads.main {
            sendChanges(cell, vCell, dialog) {
                vCell.setContentImageId(oldPage!!.getCell(vCell.getRowIndex(), vCell.getColumnIndex())!!.imageId)
            }
        }
    }

}
