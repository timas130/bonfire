package com.sayzen.campfiresdk.screens.wiki

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.Language
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemChange
import com.dzen.campfire.api.requests.wiki.RWikiItemCreate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.wiki.EventWikiChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiCreated
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.lang.ref.WeakReference

class SWikiItemCreate(
        val fandomId: Long,
        val parentItemId: Long,
        val item: WikiTitle = WikiTitle()
) : Screen(R.layout.screen_wiki_item_create) {

    private val vNameEnglish: EditText = findViewById(R.id.vNameEnglish)
    private val vNameMyLanguage: EditText = findViewById(R.id.vNameMyLanguage)
    private val vNamesContainer: ViewGroup = findViewById(R.id.vNamesContainer)
    private val vAddTranslate: ViewButton = findViewById(R.id.vAddTranslate)
    private val vFinish: FloatingActionButton = findViewById(R.id.vFinish)
    private val vShowLanguages: TextView = findViewById(R.id.vShowLanguages)
    private val vImageBig: ImageView = findViewById(R.id.vImageBig)
    private val vImageBigPlus: View = findViewById(R.id.vImageBigPlus)
    private val vImageMini: ImageView = findViewById(R.id.vImage)
    private val vImageMiniPlus: View = findViewById(R.id.vImagePlus)
    private val vTypeArticle: ViewChip = findViewById(R.id.vTypeArticle)
    private val vTypeSection: ViewChip = findViewById(R.id.vTypeSection)

    private var image: ByteArray? = null
    private var imageMini: ByteArray? = null

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.wiki_item_create_title))
        vTypeArticle.text = t(API_TRANSLATE.app_article)
        vTypeSection.text = t(API_TRANSLATE.app_section)
        vShowLanguages.text = t(API_TRANSLATE.app_add)
        vAddTranslate.text = t(API_TRANSLATE.app_add)

        vTypeArticle.setOnClickListener {
            vTypeSection.isChecked = !vTypeArticle.isChecked
            updateFinishEnabled()
        }
        vTypeSection.setOnClickListener {
            vTypeArticle.isChecked = !vTypeSection.isChecked
            updateFinishEnabled()
        }

        vFinish.setOnClickListener { create() }

        vNameEnglish.hint = t(API_TRANSLATE.wiki_item_create_name, "English")
        vNameEnglish.setText(item.name)

        vNameEnglish.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vNameMyLanguage.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        vNamesContainer.visibility = View.GONE

        vImageBig.setOnClickListener { selectImageBig() }
        vImageMini.setOnClickListener { selectImageMini() }

        vAddTranslate.setOnClickListener { showTranslateDialog() }
        vShowLanguages.setOnClickListener {
            vNamesContainer.visibility = if (vNamesContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            vShowLanguages.setText(if (vNamesContainer.visibility == View.VISIBLE) t(API_TRANSLATE.app_hide) else t(API_TRANSLATE.app_show_all))
        }

        val code = ControllerApi.getLanguageCode()
        if (code == "en") {
            vNameMyLanguage.visibility = View.GONE
        } else {
            vNameMyLanguage.tag = code
            vNameMyLanguage.hint = t(API_TRANSLATE.wiki_item_create_name, ControllerApi.getLanguage(code).name)
            addLanguageToItemIfNeed(code)
            vNameMyLanguage.setText(item.getName(code))
        }

        for (i in item.translates) {
            if (i.languageCode == code) continue
            addLanguage(ControllerApi.getLanguage(i.languageCode), i.name)
        }

        if (item.imageId > 0) {
            vImageMiniPlus.visibility = View.GONE
            ImageLoader.loadGif(item.imageId, 0, vImageMini)
        }
        if (item.imageBigId > 0) {
            vImageBigPlus.visibility = View.GONE
            ImageLoader.loadGif(item.imageBigId, 0, vImageBig)
        }

        if (item.itemId > 0) {
            vTypeSection.visibility = View.GONE
            vTypeArticle.visibility = View.GONE
        }

        updateFinishEnabled()

    }

    private fun updateFinishEnabled() {
        var textCheck = vNameEnglish.text.isNotBlank()
        if (textCheck) {
            textCheck = ToolsText.isOnly(vNameEnglish.text.toString(), API.ENGLISH)
            vNameEnglish.error = if (textCheck) null else t(API_TRANSLATE.error_use_english)
        }
        if (textCheck) {
            textCheck = vNameEnglish.text.isNotEmpty() && vNameEnglish.text.length <= API.WIKI_NAME_MAX
            vNameEnglish.error = if (textCheck) null else t(API_TRANSLATE.error_too_long_text)
        }

        var textCheckMy = true
        if (vNameMyLanguage.visibility == View.VISIBLE) {
            textCheckMy = vNameMyLanguage.text.isNotEmpty() && vNameMyLanguage.text.length <= API.WIKI_NAME_MAX
            if (vNameMyLanguage.text.length > API.WIKI_NAME_MAX) vNameMyLanguage.error = if (textCheckMy) null else t(API_TRANSLATE.error_too_long_text)
            else vNameMyLanguage.error = null
        }

        var textCheOther = true
        for (i in 0 until vNamesContainer.childCount) {
            val v = vNamesContainer.getChildAt(i)
            val vFiled: EditText? = v.findViewById(R.id.vField)
            if (vFiled != null) {
                textCheOther = vFiled.text.isNotEmpty() && vFiled.text.length <= API.WIKI_NAME_MAX
                vFiled.error = if (textCheckMy) null else t(API_TRANSLATE.error_too_long_text)
            }
        }

        var check = textCheck && textCheckMy && textCheOther
        if (vTypeSection.visibility == View.VISIBLE) check = check && (vTypeArticle.isChecked || vTypeSection.isChecked)

        if (item.itemId == 0L) {
            ToolsView.setFabEnabledR(vFinish, check && image != null && imageMini != null, R.color.green_700)
        } else {
            ToolsView.setFabEnabledR(vFinish, check, R.color.green_700)
        }
    }

    private fun showTranslateDialog() {
        val w = SplashMenu()

        val existed = ArrayList<String>()
        existed.add("en")
        for (i in item.translates) existed.add(i.languageCode)

        for (i in API.LANGUAGES) if (!existed.contains(i.code)) w.add(i.name) { addLanguage(i, "") }

        w.asSheetShow()
    }

    private fun addLanguage(language: Language, text: String) {
        addLanguageToItemIfNeed(language.code)
        val v: View = ToolsView.inflate(R.layout.view_wiki_item_create_field)
        val vField: EditText = v.findViewById(R.id.vField)
        vField.tag = language.code
        vField.setText(text)
        vField.hint = t(API_TRANSLATE.wiki_item_create_name, language.name)
        vNamesContainer.addView(v, vNamesContainer.childCount - 1)
    }

    private fun addLanguageToItemIfNeed(code: String) {
        for (i in item.translates) if (i.languageCode == code) return
        val wikiTranslation = WikiTitle.Translate()
        wikiTranslation.languageCode = code
        item.translates = ToolsCollections.add(wikiTranslation, item.translates)
    }

    private fun create() {
        item.name = vNameEnglish.text.toString()

        if (vNameMyLanguage.visibility == View.VISIBLE) {
            val code = vNameMyLanguage.tag.toString()
            for (n in item.translates) if (n.languageCode == code) n.name = vNameMyLanguage.text.toString()
        }

        for (i in 0 until vNamesContainer.childCount) {
            val v = vNamesContainer.getChildAt(i)
            val vFiled: EditText? = v.findViewById(R.id.vField)
            if (vFiled != null) {
                val code = vFiled.tag.toString()
                for (n in item.translates) if (n.languageCode == code) n.name = vFiled.text.toString()
            }
        }

        if (item.itemId == 0L) {
            item.itemType = if (vTypeArticle.isChecked) API.WIKI_TYPE_ARTICLE else API.WIKI_TYPE_SECION
            ApiRequestsSupporter.executeProgressDialog(RWikiItemCreate(fandomId, parentItemId, item, imageMini, image)) { _, r ->
                ToolsToast.show(t(API_TRANSLATE.app_done))
                Navigator.remove(this)
                EventBus.post(EventWikiCreated(r.item))
            }
        } else {
            ApiRequestsSupporter.executeProgressDialog(RWikiItemChange(item, parentItemId, imageMini, image)) { _, r ->
                ToolsToast.show(t(API_TRANSLATE.app_done))
                Navigator.remove(this)
                EventBus.post(EventWikiChanged(r.item))
            }
        }
    }

    private fun setImageBig(bitmap: Bitmap, bytes: ByteArray) {
        image = bytes
        updateFinishEnabled()
        vImageBigPlus.visibility = View.GONE
        if (ToolsBytes.isGif(bytes)) {
            ToolsGif.iterator(bytes, WeakReference(vImageBig))
        } else {
            vImageBig.setImageBitmap(bitmap)
        }
    }

    private fun setImageMini(bitmap: Bitmap, bytes: ByteArray) {
        imageMini = bytes
        updateFinishEnabled()
        vImageMiniPlus.visibility = View.GONE
        if (ToolsBytes.isGif(bytes)) {
            ToolsGif.iterator(bytes, WeakReference(vImageMini))
        } else {
            vImageMini.setImageBitmap(bitmap)
        }
    }

    private fun selectImageBig() {
        ToolsView.hideKeyboard()
        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {


                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSizeW = if (isGif) API.WIKI_TITLE_IMG_GIF_W else API.WIKI_TITLE_IMG_W
                            val cropSizeH = if (isGif) API.WIKI_TITLE_IMG_GIF_H else API.WIKI_TITLE_IMG_H

                            Navigator.to(SCrop(bitmap, cropSizeW, cropSizeH) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.WIKI_TITLE_IMG_GIF_W, API.WIKI_TITLE_IMG_GIF_H, x, y, w, h, true)
                                        ToolsThreads.main {
                                            d.hide()
                                            if (bytesSized.size > API.WIKI_TITLE_IMG_GIF_WEIGHT) {
                                                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                            } else {
                                                setImageBig(b2, bytesSized)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.WIKI_TITLE_IMG_WEIGHT, API.WIKI_TITLE_IMG_W, API.WIKI_TITLE_IMG_H, true) {
                                        ToolsThreads.main {
                                            d.hide()
                                            if (it == null) {
                                                ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                                            } else {
                                                setImageBig(b2, it)
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()

    }

    private fun selectImageMini() {
        ToolsView.hideKeyboard()
        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.WIKI_IMG_SIDE_GIF else API.WIKI_IMG_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.WIKI_IMG_SIDE_GIF, API.WIKI_IMG_SIDE_GIF, x, y, w, h, true)
                                        ToolsThreads.main {
                                            d.hide()
                                            if (bytesSized.size > API.WIKI_IMG_WEIGHT_GIF) {
                                                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                            } else {
                                                setImageMini(bitmap, bytesSized)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.WIKI_IMG_WEIGHT, API.WIKI_IMG_SIDE, API.WIKI_IMG_SIDE, true) {
                                        ToolsThreads.main {
                                            d.hide()
                                            if (it == null) ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                                            else setImageMini(b2, it)
                                        }
                                    }
                                }
                            })

                        }


                    }


                }
                .asSheetShow()

    }

}