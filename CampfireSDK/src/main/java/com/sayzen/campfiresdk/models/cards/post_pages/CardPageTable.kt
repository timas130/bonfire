package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageTable
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.table.ViewTable

class CardPageTable(
        pagesContainer: PagesContainer?,
        page: PageTable
) : CardPage(R.layout.card_page_table, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)

        val page = page as PageTable

        val vTable:ViewTable = view.findViewById(R.id.vTable)

        vTable.clear()
        vTable.textProcessor = { _, _, vText -> ControllerLinks.makeLinkable(vText) }
        vTable.setColumnsCount(page.columnsCount, true)
        vTable.createRows(page.rowsCount, true)
        for(c in page.cells) {
            if(c.type == PageTable.CELL_TYPE_TEXT) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentText(c.text)
            if(c.type == PageTable.CELL_TYPE_IMAGE) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentImageId(c.imageId) {
                if (pagesContainer != null) {
                    ControllerPost.toImagesScreen(pagesContainer, c.imageId)
                } else {
                    Navigator.to(SImageView(ImageLoader.load(c.imageId)))
                }
            }
        }

    }

    override fun notifyItem() {}
}
