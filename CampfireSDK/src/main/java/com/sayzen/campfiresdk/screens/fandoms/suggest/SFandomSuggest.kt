package com.sayzen.campfiresdk.screens.fandoms.suggest

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.*
import com.dzen.campfire.api.tools.client.Request
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.models.objects.FandomParam
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.util.*


class SFandomSuggest(
        val changeFandom: Fandom?,
        val creator: Account,
        val params1: Array<Long>,
        val params2: Array<Long>,
        val params3: Array<Long>,
        val params4: Array<Long>,
        val notes: String,
) : Screen(R.layout.screen_fandom_suggest) {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SFandomSuggest(null, Account(), emptyArray(), emptyArray(), emptyArray(), emptyArray(), ""))
        }

        fun instance(fandomId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsSuggestedGet(fandomId)) { r ->
                SFandomSuggest(r.fandom, r.creator, r.params1, r.params2, r.params3, r.params4, r.notes)
            }
        }
    }

    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImagePlus: View = findViewById(R.id.vImagePlus)
    private val vImageMini: ImageView = findViewById(R.id.vImageMini)
    private val vImageMiniPlus: View = findViewById(R.id.vImageMiniPlus)
    private val vName: SettingsField = findViewById(R.id.vName)
    private val vFandomSuggestionNotes: SettingsField = findViewById(R.id.vFandomSuggestionNotes)
    private val vFandomIsClosed: SettingsCheckBox = findViewById(R.id.vFandomIsClosed)
    private val vCategoriesContainer: ViewGroup = findViewById(R.id.vCategoriesContainer)
    private val vCategoriesTitle: TextView = findViewById(R.id.vCategoriesTitle)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vFab2: FloatingActionButton = findViewById(R.id.vFab2)
    private val vSearchProgress: View = findViewById(R.id.vSearchProgress)
    private val vFandomsContainer: ViewGroup = findViewById(R.id.vFandomsContainer)
    private val vSuggestUser: ViewAvatarTitle = findViewById(R.id.vSuggestUser)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)
    private val vScroll: ScrollView = findViewById(R.id.vScroll)

    private var image: ByteArray? = null
    private var imageMini: ByteArray? = null

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.fandoms_suggest))

        vName.setHint(t(API_TRANSLATE.app_name_s))
        vCategoriesTitle.text = t(API_TRANSLATE.app_category)
        vFandomIsClosed.setTitle(t(API_TRANSLATE.fandoms_suggest_closed_title))
        vFandomIsClosed.setSubtitle(t(API_TRANSLATE.fandoms_suggest_closed_help))

        if (changeFandom != null) vFandomIsClosed.setChecked(changeFandom.closed)

        vSearchProgress.visibility = View.INVISIBLE
        vFandomsContainer.visibility = View.GONE
        vSuggestUser.visibility = if (changeFandom == null) View.GONE else View.VISIBLE

        vImage.setOnClickListener {
            hideKeyboard()
            selectImage()
        }
        vImageMini.setOnClickListener {
            hideKeyboard()
            selectImageMini()
        }
        vName.vField.addTextChangedListener(TextWatcherChanged {
            updateFinishEnabled()
            updateSearch()
        })
        vFab.setOnClickListener {
            hideKeyboard()
            onFabClicked()
        }
        (vFab2 as View).visibility = if (changeFandom != null) View.VISIBLE else View.GONE
        vFab2.setOnClickListener {
            hideKeyboard()
            reject()
        }

        for (g in CampfireConstants.CATEGORIES) {
            if (!ControllerApi.can(API.LVL_PROTOADMIN) && g.index == API.CATEGORY_OTHER) continue
            val v = ViewChip.instanceChoose(context, g.name, g)
            v.setOnClickListener {
                hideKeyboard()
                for (i in 0 until vCategoriesContainer.childCount) {
                    val vv = vCategoriesContainer.getChildAt(i) as ViewChip
                    vv.isChecked = vv == v
                }
                switchParams()
                updateSearch()
                updateFinishEnabled()
            }
            vCategoriesContainer.addView(v)
        }

        if (changeFandom != null) {

            XAccount().setAccount(creator).setView(vSuggestUser)
            vSuggestUser.setSubtitle(ToolsDate.dateToString(changeFandom.dateCreate))

            ToolsView.setFabColorR(vFab2, R.color.red_700)
            vName.setText(changeFandom.name)
            updateSearch()
            vImagePlus.visibility = View.GONE
            vImageMiniPlus.visibility = View.GONE
            ImageLoader.load(changeFandom.imageTitleId).into(vImage)
            ImageLoader.load(changeFandom.imageId).into(vImageMini)

            for (i in 0 until vCategoriesContainer.childCount) {
                val v = vCategoriesContainer.getChildAt(i) as ViewChip
                val g = v.tag as FandomParam
                v.isChecked = changeFandom.category == g.index
            }

        }

        if (SFandomsSearch.ROOT_CATEGORY_ID > 0) {
            vCategoriesContainer.visibility = View.GONE
            vCategoriesTitle.visibility = View.GONE
            (vCategoriesContainer.getChildAt(SFandomsSearch.ROOT_CATEGORY_ID.toInt()) as ViewChip).isChecked = true
        }

        if (getSelectedCategory() == 0L) (vCategoriesContainer.getChildAt(0) as ViewChip).isChecked = true

        switchParams()
        updateFinishEnabled()

        ToolsView.disableScrollViewJump(vScroll)

        vFandomSuggestionNotes.setHint(t(API_TRANSLATE.fandoms_suggest_notes))
        vFandomSuggestionNotes.setText(notes)
        if (changeFandom != null) {
            vFandomSuggestionNotes.isEnabled = false
        }
    }

    private fun isHasChanges(): Boolean {
        if (changeFandom != null) return false
        return image != null || imageMini != null || vName.getText().isNotEmpty()
    }

    override fun onBackPressed(): Boolean {
        if (!isHasChanges()) return false
        SplashAlert()
                .setText(t(API_TRANSLATE.post_create_cancel_alert))
                .setOnEnter(t(API_TRANSLATE.app_yes)) { Navigator.remove(this) }
                .setOnCancel(t(API_TRANSLATE.app_no))
                .asSheetShow()
        return true
    }

    private fun hideKeyboard() {
        ToolsView.hideKeyboard(vName.vField)
    }

    private fun switchParams() {

        val category = getSelectedCategory()
        vContainer.removeAllViews()

        addParams(category, 1, params1)
        addParams(category, 2, params2)
        addParams(category, 3, params3)
        addParams(category, 4, params4)
    }

    private fun onFabClicked() {
        if (changeFandom == null)
            send()
        else
            accept()
    }

    private fun updateFinishEnabled() {
        var textCheck = ToolsText.isOnly(vName.getText(), API.FANDOM_NAME_CHARS)
        vName.setError(if (textCheck) null else t(API_TRANSLATE.error_use_english))
        if (textCheck) {
            textCheck = vName.getText().length <= API.FANDOM_NAME_MAX
            vName.setError(if (textCheck) null else t(API_TRANSLATE.error_too_long_text))
        }
        val cateryId = getSelectedCategory()
        var check = textCheck && vName.getText().isNotEmpty()
                && (cateryId == 0L || CampfireConstants.getParamTitle(cateryId, 1) == null || paramGet(1).isNotEmpty())
                && (cateryId == 0L || CampfireConstants.getParamTitle(cateryId, 2) == null || paramGet(3).isNotEmpty())
                && (cateryId == 0L || CampfireConstants.getParamTitle(cateryId, 3) == null || paramGet(5).isNotEmpty())
                && (cateryId == 0L || CampfireConstants.getParamTitle(cateryId, 4) == null || paramGet(7).isNotEmpty())

        if (changeFandom == null)
            ToolsView.setFabEnabledR(vFab, check && image != null && imageMini != null, R.color.green_700)
        else
            ToolsView.setFabEnabledR(vFab, check, R.color.green_700)
    }

    private fun selectImage() {
        SplashChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H) { _, b, _, _, _, _ ->
                        val w = ToolsView.showProgressDialog()
                        vImage.setImageBitmap(b)
                        vImagePlus.visibility = View.GONE
                        ToolsThreads.thread {
                            image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H), API.FANDOM_TITLE_IMG_WEIGHT)
                            w.hide()
                            ToolsThreads.main { updateFinishEnabled() }
                        }

                    })
                }
                .asSheetShow()
    }

    private fun selectImageMini() {
        SplashChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE) { _, b, _, _, _, _ ->
                        val w = ToolsView.showProgressDialog()
                        vImageMini.setImageBitmap(b)
                        vImageMiniPlus.visibility = View.GONE
                        ToolsThreads.thread {
                            imageMini = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.FANDOM_IMG_SIDE), API.FANDOM_IMG_WEIGHT)
                            w.hide()
                            ToolsThreads.main { updateFinishEnabled() }
                        }

                    })
                }
                .asSheetShow()
    }

    //
    //  Search
    //

    private var request: Request<RFandomsGetAll.Response>? = null
    private var subscription: Subscription? = null

    private fun updateSearch() {
        if (request != null) request!!.unsubscribe()
        if (subscription != null) subscription!!.unsubscribe()
        vFandomsContainer.visibility = View.GONE
        vFandomsContainer.removeAllViews()
        if (vName.getText().isEmpty()) return
        vSearchProgress.visibility = View.VISIBLE
        subscription = ToolsThreads.main(1000) {
            searchNow()
        }
    }

    private fun searchNow() {
        request = RFandomsGetAll(RFandomsGetAll.SUBSCRIBE_NONE, 0, 0, getSelectedCategory(), vName.getText(), emptyArray(), emptyArray(), emptyArray(), emptyArray())
                .onComplete { r ->
                    if (r.fandoms.isEmpty()) vFandomsContainer.visibility = View.GONE
                    else vFandomsContainer.visibility = View.VISIBLE
                    for (fandom in r.fandoms) {
                        val v = ViewChip.instance(context)
                        v.text = fandom.name
                        ImageLoader.load(fandom.imageId).intoBitmap { v.setIcon(it) }
                        v.setOnClickListener { SFandom.instance(fandom.id, Navigator.TO) }
                        v.setChipBackgroundColorResource(R.color.focus)
                        vFandomsContainer.addView(v)
                    }
                    vSearchProgress.visibility = View.INVISIBLE
                }
                .send(api)
    }

    //
    //  Actions
    //

    private fun send() {
        ApiRequestsSupporter.executeProgressDialog(t(API_TRANSLATE.app_uploading), RFandomsSuggest(vName.getText(), getSelectedCategory(), vFandomIsClosed.isChecked(), image, imageMini,
                paramGet(1),
                paramGet(3),
                paramGet(5),
                paramGet(7),
                vFandomSuggestionNotes.getText()
        )) { _ ->
            ToolsToast.show(t(API_TRANSLATE.fandoms_suggest_suggested))
            Navigator.back()
        }
    }

    private fun accept() {
        sendChangeIfNeed {
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_suggest_accept_text), t(API_TRANSLATE.app_accept), RFandomsAccept(changeFandom!!.id, true, "")) {
                ToolsToast.show(t(API_TRANSLATE.app_done))
                EventBus.post(EventFandomAccepted(changeFandom.id, true))
                Navigator.back()
            }
                    .onApiError(RFandomsAccept.E_SELF){ToolsToast.show(t(API_TRANSLATE.fandoms_suggest_accept_error_self))}
        }
    }

    private fun sendChangeIfNeed(onFinish: () -> Unit) {
        val changed = image != null
                || imageMini != null
                || changeFandom!!.name != vName.getText()
                || changeFandom.closed != vFandomIsClosed.isChecked()
                || isParamsChanged(params1, paramGet(1))
                || isParamsChanged(params2, paramGet(3))
                || isParamsChanged(params3, paramGet(5))
                || isParamsChanged(params4, paramGet(7))

        if (!changed) {
            onFinish.invoke()
            return
        }

        ApiRequestsSupporter.executeProgressDialog(t(API_TRANSLATE.app_uploading), RFandomsChange(changeFandom!!.id, vName.getText(), getSelectedCategory(), vFandomIsClosed.isChecked(), image, imageMini,
                paramGet(1),
                paramGet(3),
                paramGet(5),
                paramGet(7)
        )) { _ ->
            onFinish.invoke()
        }
    }

    private fun reject() {
        SplashReject { comment ->
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_suggest_reject_text), t(API_TRANSLATE.app_reject), RFandomsAccept(changeFandom!!.id, false, comment)) {
                ToolsToast.show(t(API_TRANSLATE.app_done))
                EventBus.post(EventFandomAccepted(changeFandom.id, false))
                Navigator.back()
            }
        }.asSheetShow()
    }

    private fun getSelectedCategory(): Long {
        for (i in 0 until vCategoriesContainer.childCount) {
            val v = vCategoriesContainer.getChildAt(i) as ViewChip
            if (v.isChecked) return (v.tag as FandomParam).index
        }
        return 0
    }

    //
    //  Infos
    //


    private fun addParams(categoryId: Long, paramsPosition: Int, selected: Array<Long>) {
        if (CampfireConstants.getParamTitle(categoryId, paramsPosition) == null) return

        val vTitle: TextView = ToolsView.inflate(R.layout.screen_fandoms_search_params_title)
        val vFlow: LayoutFlow = ToolsView.inflate(R.layout.screen_fandoms_search_params_flow)

        vTitle.text = CampfireConstants.getParamTitle(categoryId, paramsPosition)

        for (i in CampfireConstants.getParams(categoryId, paramsPosition)!!) {
            val v = ViewChip.instanceChoose(context, i.name, i)
            for (genre in selected) if (genre == i.index) v.isChecked = true
            v.setOnCheckedChangeListener { _, _ ->
                hideKeyboard()
                updateFinishEnabled()
            }
            vFlow.addView(v)
        }

        vContainer.addView(vTitle)
        vContainer.addView(vFlow)

    }

    private fun paramGet(index: Int): Array<Long> {
        if (vContainer.childCount <= index) return emptyArray()
        val indexes = ArrayList<Long>()
        val container = vContainer.getChildAt(index) as ViewGroup
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i) as ViewChip
            if (v.isChecked) indexes.add((v.tag as FandomParam).index)
        }
        return indexes.toTypedArray()
    }

    private fun isParamsChanged(allParams: Array<Long>, selectedParams: Array<Long>): Boolean {

        if (selectedParams.size != allParams.size) return true
        else for (i in 0 until allParams.size) if (selectedParams[i] != allParams[i]) return true

        return false

    }

}
