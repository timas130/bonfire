package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageDownload
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.apiMedia
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class CardPageDownload(
        pagesContainer: PagesContainer?,
        page: PageDownload
) : CardPage(R.layout.card_page_download, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageDownload
        val vSubTitle: TextView = view.findViewById(R.id.vSubTitle)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vDownload: Button = view.findViewById(R.id.vDownload)

        vDownload.text = t(API_TRANSLATE.app_download)

        vTitle.text = t(API_TRANSLATE.app_file) + ": " + ToolsText.numToBytesString(page.size)

        vSubTitle.text = page.title
        if (page.patch.isNotEmpty()) {
            vSubTitle.text = vSubTitle.text.toString() + if (vSubTitle.text.isNotEmpty()) "\n" else ""
            vSubTitle.text = vSubTitle.text.toString() + page.patch
        }
        if (page.autoUnzip) {
            vSubTitle.text = vSubTitle.text.toString() + if (vSubTitle.text.isNotEmpty()) "\n" else ""
            vSubTitle.text = vSubTitle.text.toString() + t(API_TRANSLATE.post_page_download_unzip)
        }
        vSubTitle.visibility = if (vSubTitle.text.isEmpty()) View.GONE else View.VISIBLE

        if (clickable) vDownload.setOnClickListener { download() }
        else vDownload.setOnClickListener(null)

    }

    protected fun download() {
        val page = this.page as PageDownload
        SplashAlert()
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setText(t(API_TRANSLATE.post_page_download_alert))
                .setTextGravity(Gravity.CENTER)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setOnEnter(t(API_TRANSLATE.app_download)) {

                    val dProgress = ToolsView.showProgressDialog(t(API_TRANSLATE.app_downloading))
                    RResourcesGet(page.resourceId)
                            .onComplete { r ->
                                ToolsPermission.requestWritePermission {
                                    val dProgress2 = ToolsView.showProgressDialog(t(API_TRANSLATE.app_downloading))
                                    ToolsThreads.thread {
                                        ToolsFilesAndroid.unpackZip(page.patch, r.bytes)
                                        ToolsThreads.main {
                                            dProgress2.hide()
                                            dProgress.hide()    //  Почему-то не скрывается в onFinish
                                            ToolsToast.show(t(API_TRANSLATE.app_done))
                                        }
                                    }
                                }
                            }
                            .onFinish { dProgress.hide() }
                            .send(apiMedia)
                }
                .asSheetShow()


    }

    override fun notifyItem() {

    }

}
