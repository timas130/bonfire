package com.sayzen.campfiresdk.screens.post.create.creators

import android.app.Activity
import android.content.Intent
import com.dzen.campfire.api.models.publications.post.Page
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.libs.debug.err

object VideoAttach {
    fun pickAndUpload(
        requestPutPage: (Page, Screen?, Splash?, ((CardPage) -> Unit)) -> Unit,
        requestChangePage: (Page, CardPage, Screen?, Splash?, (Page) -> Unit) -> Unit,
        card: CardPage?,
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            ToolsIntent.startIntentForResult(Intent.createChooser(intent, null)) { resultCode, resultIntent ->
                val uri = resultIntent?.data
                if (resultCode != Activity.RESULT_OK || uri == null) return@startIntentForResult
                VideoUploadSplash(uri, requestPutPage, requestChangePage, card).asSheetShow()
            }
        } catch (e: Exception) {
            err(e)
            ToolsToast.show(R.string.app_error)
        }
    }
}
