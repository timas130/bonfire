package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.FandomLink
import com.dzen.campfire.api.requests.fandoms.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.notifications.ControllerApp
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.*
import com.sayzen.campfiresdk.models.objects.FandomParam
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashFieldTwo
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads


class CardFandomInfo(
        private val xFandom: XFandom,
        var karmaCof: Long
) : Card(R.layout.screen_fandom_card_info) {

    private var loaded = false
    private var loading = false
    private var gallery = emptyArray<Long>()
    private var links = emptyArray<FandomLink>()
    private var description: String = ""
    private var names = emptyArray<String>()
    private var params1 = emptyArray<Long>()
    private var params2 = emptyArray<Long>()
    private var params3 = emptyArray<Long>()
    private var params4 = emptyArray<Long>()
    var categoryId = 0L

    private val eventBus = EventBus
            .subscribe(EventFandomInfGalleryChanged::class) { this.onEventFandomInfoGalleryChanged(it) }
            .subscribe(EventFandomInfoLinksChanged::class) { this.onEventFandomInfoLinksChanged(it) }
            .subscribe(EventFandomKarmaCofChanged::class) { if (it.fandomId == xFandom.getId()) karmaCof = it.cof; updateKarmaCof(); }
            .subscribe(EventFandomDescriptionChanged::class) { this.onEventFandomDescriptionChanged(it) }
            .subscribe(EventFandomNamesChanged::class) { this.onEventFandomNamesChanged(it) }
            .subscribe(EventFandomParamsChanged::class) { onEventFandomParamsChanged(it) }
            .subscribe(EventFandomCategoryChanged::class) { if (it.fandomId == xFandom.getId()) categoryId = it.newCategory }

    private var expanded = false

    override fun bindView(view: View) {
        super.bindView(view)

        updateSpoiler()
        updateKarmaCof()
        updateDescription()
        updateGallery()
        updateLinks()
        updateParams_1()
        updateParams_2()
        updateParams_3()
        updateParams_4()
    }

    private fun onEventFandomInfoGalleryChanged(e: EventFandomInfGalleryChanged) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId()) {
            if (e.gallery.isNotEmpty()) {
                this.gallery = e.gallery
                updateGallery()
            }
        }
    }

    private fun onEventFandomInfoLinksChanged(e: EventFandomInfoLinksChanged) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId()) {
            if (e.links.isNotEmpty()) {
                this.links = e.links
                updateLinks()
            }
        }
    }

    //
    //  Spoiler
    //

    fun updateSpoiler() {
        val view = getView() ?: return
        val vShowMore: TextView = view.findViewById(R.id.vShowMore)
        val vShowMoreTouch: View = view.findViewById(R.id.vShowMoreTouch)
        val vContainerInfo: View = view.findViewById(R.id.vContainerInfo)

        vShowMoreTouch.setOnClickListener {
            expanded = !expanded
            updateSpoiler()
        }
        vShowMore.setText(if (expanded) t(API_TRANSLATE.fandom_hide_details) else t(API_TRANSLATE.fandom_show_details))
        vContainerInfo.visibility = if (expanded && loaded) View.VISIBLE else View.GONE

        if (expanded && !loaded && !loading) {
            loading = true
            vShowMore.text = "${t(API_TRANSLATE.app_loading)}..."
            RFandomsGetInfo(xFandom.getId(), xFandom.getLanguageId())
                    .onComplete { r ->
                        loading = false
                        loaded = true
                        gallery = r.gallery
                        links = r.links
                        description = r.description
                        names = r.names
                        params1 = r.params1
                        params2 = r.params2
                        params3 = r.params3
                        params4 = r.params4
                        categoryId = r.categoryId
                        update()
                    }
                    .onError {
                        loading = false
                        updateSpoiler()
                    }
                    .send(api)
        }
    }

    //
    //  Karma Cof
    //

    fun updateKarmaCof() {
        val view = getView() ?: return
        val vTitle: TextView = view.findViewById(R.id.vKarmaCof_Title)
        val vTouch: View = view.findViewById(R.id.vKarmaCof_Touch)

        var text = ToolsText.numToStringRound(karmaCof / 100.0, 2)
        if (karmaCof > 100) text = ToolsHTML.font_color(text, ToolsHTML.color_green)
        if (karmaCof < 100) text = ToolsHTML.font_color(text, ToolsHTML.color_red)
        vTitle.text = t(API_TRANSLATE.app_coefficient_karma, text)
        ToolsView.makeTextHtml(vTitle)

        if (ControllerApi.can(API.LVL_ADMIN_FANDOM_SET_COF))
            vTouch.setOnClickListener { setCof() }
    }

    private fun setCof() {
        SplashFieldTwo()
                .setTitle(t(API_TRANSLATE.fandoms_menu_set_cof_hint, API.FANDOM_KARMA_COF_MIN / 100, API.FANDOM_KARMA_COF_MAX / 100))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setText_1(ToolsText.numToStringRound(karmaCof / 100.0, 2))
                .setHint_1(t(API_TRANSLATE.app_coefficient))
                .setLinesCount_1(1)
                .addChecker_1(t(API_TRANSLATE.error_incorrect_value)) {
                    if (it.length > 4) {
                        return@addChecker_1 false
                    }
                    try {
                        val v = (it.toDouble() * 100).toLong()
                        if (v < API.FANDOM_KARMA_COF_MIN || v > API.FANDOM_KARMA_COF_MAX) {
                            return@addChecker_1 false
                        }
                    } catch (e: Exception) {
                        return@addChecker_1 false
                    }

                    return@addChecker_1 true
                }
                .addChecker_1("") {
                    try {
                        val v = (it.toDouble() * 100).toLong()
                        if (v == karmaCof) {
                            return@addChecker_1 false
                        }
                    } catch (e: Exception) {
                        return@addChecker_1 false
                    }

                    return@addChecker_1 true
                }
                .setMin_1(1)
                .setMax_1(4)
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_2(t(API_TRANSLATE.comments_hint))
                .addChecker_2(t(API_TRANSLATE.error_use_english)) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(t(API_TRANSLATE.app_change)) { w, cof, comment ->
                    val v = (cof.toDouble() * 100).toLong()

                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminSetCof(xFandom.getId(), v, comment)) {
                        EventBus.post(EventFandomKarmaCofChanged(xFandom.getId(), v))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    //
    //  Params
    //

    fun updateParams_1() {
        val view = getView() ?: return
        view.findViewById<View>(R.id.vParams1_Root).visibility = if (CampfireConstants.getParamTitle(categoryId, 1) != null) View.VISIBLE else View.GONE
        val vContainer: ViewGroup = view.findViewById(R.id.vParams1_vContainer)
        val vAdd: ViewIcon = view.findViewById(R.id.vParams1_vAdd)
        val vEmptyText: TextView = view.findViewById(R.id.vParams1_vEmptyText)

        vContainer.removeAllViews()

        vAdd.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_PARAMS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { change(1, params1) }

        vEmptyText.text = if (params1.isEmpty() && CampfireConstants.getParamTitle(categoryId, 1) != null) CampfireConstants.getParamTitle(categoryId, 1)!! else ""

        for (i in params1) {
            val v = ViewChip.instance(view.context)
            v.text = getParam(1, i).name
            v.setOnClickListener { onClick(1, i) }
            vContainer.addView(v)
        }
    }

    fun updateParams_2() {
        val view = getView() ?: return
        view.findViewById<View>(R.id.vParams2_Root).visibility = if (CampfireConstants.getParamTitle(categoryId, 2) != null) View.VISIBLE else View.GONE
        val vContainer: ViewGroup = view.findViewById(R.id.vParams2_vContainer)
        val vAdd: ViewIcon = view.findViewById(R.id.vParams2_vAdd)
        val vEmptyText: TextView = view.findViewById(R.id.vParams2_vEmptyText)

        vContainer.removeAllViews()

        vAdd.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_PARAMS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { change(2, params2) }

        vEmptyText.text = if (params2.isEmpty() && CampfireConstants.getParamTitle(categoryId, 2) != null) CampfireConstants.getParamTitle(categoryId, 2)!! else ""

        for (i in params2) {
            val v = ViewChip.instance(view.context)
            v.text = getParam(2, i).name
            v.setOnClickListener { onClick(2, i) }
            vContainer.addView(v)
        }
    }

    fun updateParams_3() {
        val view = getView() ?: return
        view.findViewById<View>(R.id.vParams3_Root).visibility = if (CampfireConstants.getParamTitle(categoryId, 3) != null) View.VISIBLE else View.GONE
        val vContainer: ViewGroup = view.findViewById(R.id.vParams3_vContainer)
        val vAdd: ViewIcon = view.findViewById(R.id.vParams3_vAdd)
        val vEmptyText: TextView = view.findViewById(R.id.vParams3_vEmptyText)

        vContainer.removeAllViews()

        vAdd.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_PARAMS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { change(3, params3) }

        vEmptyText.text = if (params3.isEmpty() && CampfireConstants.getParamTitle(categoryId, 3) != null) CampfireConstants.getParamTitle(categoryId, 3)!! else ""

        for (i in params3) {
            val v = ViewChip.instance(view.context)
            v.text = getParam(3, i).name
            v.setOnClickListener { onClick(3, i) }
            vContainer.addView(v)
        }

    }

    fun updateParams_4() {
        val view = getView() ?: return
        view.findViewById<View>(R.id.vParams4_Root).visibility = if (CampfireConstants.getParamTitle(categoryId, 4) != null) View.VISIBLE else View.GONE
        val vContainer: ViewGroup = view.findViewById(R.id.vParams4_vContainer)
        val vAdd: ViewIcon = view.findViewById(R.id.vParams4_vAdd)
        val vEmptyText: TextView = view.findViewById(R.id.vParams4_vEmptyText)

        vContainer.removeAllViews()

        vAdd.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_PARAMS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { change(4, params4) }

        vEmptyText.text = if (params4.isEmpty() && CampfireConstants.getParamTitle(categoryId, 4) != null) CampfireConstants.getParamTitle(categoryId, 4)!! else ""

        for (i in params4) {
            val v = ViewChip.instance(view.context)
            v.text = getParam(4, i).name
            v.setOnClickListener { onClick(4, i) }
            vContainer.addView(v)
        }
    }

    private fun change(paramsPosition: Int, params: Array<Long>) {
        SplashParams(CampfireConstants.getParamTitle(categoryId, paramsPosition)!!, CampfireConstants.getParams(categoryId, paramsPosition)!!, params) { newParams, comment ->
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_menu_change_params_confirms), t(API_TRANSLATE.app_change), RFandomsAdminChangeParams(xFandom.getId(), categoryId, paramsPosition, newParams, comment)) {
                EventBus.post(EventFandomParamsChanged(xFandom.getId(), xFandom.getLanguageId(), categoryId, paramsPosition, newParams))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }.asSheetShow()
    }

    fun resetParams_1(params: Array<Long>) {
        this.params1 = params
        updateParams_1()
    }

    fun resetParams_2(params: Array<Long>) {
        this.params2 = params
        updateParams_2()
    }

    fun resetParams_3(params: Array<Long>) {
        this.params3 = params
        updateParams_3()
    }

    fun resetParams_4(params: Array<Long>) {
        this.params4 = params
        updateParams_4()
    }

    fun getParam(paramsPosition: Int, index: Long): FandomParam {
        try {
            for (a in CampfireConstants.getParams(categoryId, paramsPosition)!!)
                if (a.index == index) return a
        }catch (e:Exception){
            err(e)
        }
        return FandomParam(100, API_TRANSLATE.error_unknown)
    }

    private fun onEventFandomParamsChanged(e: EventFandomParamsChanged) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId() && e.categoryId == categoryId) {
            if (e.paramsPosition == 1) resetParams_1(e.params)
            if (e.paramsPosition == 2) resetParams_2(e.params)
            if (e.paramsPosition == 3) resetParams_3(e.params)
            if (e.paramsPosition == 4) resetParams_4(e.params)
        }
    }

    private fun onClick(paramsPosition: Int, paramIndex: Long) {
        when (paramsPosition) {
            1 -> SFandomsSearch.instance("", categoryId, arrayOf(paramIndex), emptyArray(), emptyArray(), emptyArray(), Navigator.TO)
            2 -> SFandomsSearch.instance("", categoryId, emptyArray(), arrayOf(paramIndex), emptyArray(), emptyArray(), Navigator.TO)
            3 -> SFandomsSearch.instance("", categoryId, emptyArray(), emptyArray(), arrayOf(paramIndex), emptyArray(), Navigator.TO)
            4 -> SFandomsSearch.instance("", categoryId, emptyArray(), emptyArray(), emptyArray(), arrayOf(paramIndex), Navigator.TO)
            else -> throw java.lang.RuntimeException("Unknown paramsPosition $paramsPosition")
        }
    }

    //
    //  Description
    //

    fun updateDescription() {
        val view = getView() ?: return
        val vDescription: TextView = view.findViewById(R.id.vDescription_Text)
        val vDescriptionChange: View = view.findViewById(R.id.vDescription_Change)
        val vNames: TextView = view.findViewById(R.id.vDescription_Names)

        vDescription.text = if (description.isEmpty()) t(API_TRANSLATE.fandom_info_description_empty) else description
        vDescriptionChange.visibility = if (ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_DESCRIPTION)) View.VISIBLE else View.GONE
        vDescriptionChange.setOnClickListener { changeDescription() }

        var namesS = if (names.isNotEmpty()) names[0] else ""
        for (i in 1 until names.size) namesS += ", ${names[i]}"
        vNames.text = if (namesS.isEmpty()) t(API_TRANSLATE.fandom_info_names_empty) else namesS
        if (ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_NAMES)) vNames.setOnClickListener { SplashNames(xFandom.getId(), xFandom.getLanguageId(), names).asSheetShow() }
    }

    private fun changeDescription() {
        SplashDescription(description) { description ->
            ControllerApi.moderation(t(API_TRANSLATE.app_moderation), t(API_TRANSLATE.app_confirm), { RFandomsModerationDescriptionChange(xFandom.getId(), xFandom.getLanguageId(), description, it) }) {
                EventBus.post(EventFandomDescriptionChanged(xFandom.getId(), xFandom.getLanguageId(), description))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }.asSheetShow()
    }

    private fun onEventFandomDescriptionChanged(e: EventFandomDescriptionChanged) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId()) {
            this.description = e.description
            updateDescription()
        }
    }

    private fun onEventFandomNamesChanged(e: EventFandomNamesChanged) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId()) {
            this.names = e.names
            updateDescription()
        }
    }

    //
    //  Gallery
    //

    fun updateGallery() {
        val view = getView() ?: return
        val vImages: ViewImagesSwipe = view.findViewById(R.id.vGallery_Images)
        val vText: TextView = view.findViewById(R.id.vGallery_Text)
        val vAdd: ViewIcon = view.findViewById(R.id.vGallery_Add)

        vText.text = t(API_TRANSLATE.fandom_info_gallery_empty)

        vAdd.visibility = if (ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_FANDOM_IMAGE)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { addGallery() }

        vImages.clear()
        for (i in gallery) vImages.add(ImageLoader.load(i), onClick = null) { onGalleryImageClicked(i) }

        vText.visibility = if (gallery.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    private fun onGalleryImageClicked(i: Long) {
        if (!ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_GALLERY)) {
            ToolsToast.show(t(API_TRANSLATE.error_low_lvl))
            return
        }
        SplashField()
                .setTitle(t(API_TRANSLATE.app_remove_image))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationGalleryRemove(xFandom.getId(), xFandom.getLanguageId(), i, comment)) {
                        val list = ArrayList<Long>()
                        for (id in gallery) if (id != i) list.add(id)
                        EventBus.post(EventFandomInfGalleryChanged(xFandom.getId(), xFandom.getLanguageId(), list.toTypedArray()))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    private fun addGallery() {
        if (gallery.size >= API.FANDOM_GALLERY_MAX) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        SplashChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b) { _, bitmap, _, _, _, _ ->
                        SplashField()
                                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                                .setOnCancel(t(API_TRANSLATE.app_cancel))
                                .setMin(API.MODERATION_COMMENT_MIN_L)
                                .setMax(API.MODERATION_COMMENT_MAX_L)
                                .setOnEnter(t(API_TRANSLATE.app_add)) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(bitmap, API.FANDOM_GALLERY_MAX_SIDE), API.FANDOM_GALLERY_MAX_WEIGHT)
                                        ToolsThreads.main {
                                            ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsModerationGalleryAdd(xFandom.getId(), xFandom.getLanguageId(), image, comment)) { r ->
                                                val array = Array(gallery.size + 1) {
                                                    if (gallery.size == it) r.imageId
                                                    else gallery[it]
                                                }
                                                EventBus.post(EventFandomInfGalleryChanged(xFandom.getId(), xFandom.getLanguageId(), array))
                                                ToolsToast.show(t(API_TRANSLATE.app_done))
                                            }
                                        }
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    //
    //  Links
    //

    fun updateLinks() {
        val view = getView() ?: return
        val vLinksContainer: ViewGroup = view.findViewById(R.id.vLinks_vLinksContainer)
        val vEmptyText: TextView = view.findViewById(R.id.vLinks_vEmptyText)
        val vAdd: ViewIcon = view.findViewById(R.id.vLinks_vAdd)

        vEmptyText.text = t(API_TRANSLATE.fandom_info_links_empty)

        vLinksContainer.visibility = if (links.isEmpty()) View.GONE else View.VISIBLE

        vAdd.visibility = if (ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_LINKS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { addLink() }

        vEmptyText.visibility = if (links.isEmpty()) View.VISIBLE else View.GONE

        vLinksContainer.removeAllViews()
        for (link in links) {
            val vLink: View = ToolsView.inflate(R.layout.screen_fandom_card_info_link)
            val vLinkTitle: TextView = vLink.findViewById(R.id.vLinkTitle)
            val vLinkSubtitle: TextView = vLink.findViewById(R.id.vLinkUrl)
            val vLinkImage: ImageView = vLink.findViewById(R.id.vLinkImage)
            vLinkTitle.text = link.title
            vLinkSubtitle.text = link.url

            val w = SplashMenu()
                    .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(link.url);ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                    .add(t(API_TRANSLATE.app_change)) { changeLink(link.index, link.url, link.title, link.imageIndex) }
                    .add(t(API_TRANSLATE.app_remove)) { removeLink(link) }.condition(ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_LINKS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)

            vLink.setOnLongClickListener {
                w.asSheetShow()
                true
            }
            vLink.setOnClickListener { ControllerLinks.openLink(link.url) }

            when (link.imageIndex) {
                1L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_YOUTUBE_WHITE else API_RESOURCES.ICON_YOUTUBE_BLACK).into(vLinkImage)
                2L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_DISCORD_WHITE else API_RESOURCES.ICON_DISCORD_BLACK).into(vLinkImage)
                3L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_WIKI_WHITE else API_RESOURCES.ICON_WIKI_BLACK).into(vLinkImage)
                4L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_TWITTER_WHITE else API_RESOURCES.ICON_TWITTER_BLACK).into(vLinkImage)
                5L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_STEAM_WHITE else API_RESOURCES.ICON_STEAM_BLACK).into(vLinkImage)
                6L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_GOOGLE_PLAY_WHITE else API_RESOURCES.ICON_GOOGLE_PLAY_BLACK).into(vLinkImage)
                7L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_APPSTORE_WHITE else API_RESOURCES.ICON_APPSTORE_BLACK).into(vLinkImage)
                8L -> ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_CAMPFIRE else API_RESOURCES.ICON_CAMPFIRE).into(vLinkImage)
                else -> vLinkImage.setImageResource(R.drawable.ic_insert_link_white_24dp)
            }

            vLinksContainer.addView(vLink)
        }
    }

    private fun removeLink(link: FandomLink) {
        SplashField()
                .setTitle(t(API_TRANSLATE.app_remove_link))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setAutoHideOnEnter(false)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationLinkRemove(link.index, comment)) {
                        val list = ArrayList<FandomLink>()
                        for (l in links) if (l.index != link.index) list.add(l)
                        EventBus.post(EventFandomInfoLinksChanged(xFandom.getId(), xFandom.getLanguageId(), list.toTypedArray()))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    private fun changeLink(linkId: Long, link: String, description: String, icon: Long) {
        SplashLink(link, description, icon) { w, url, title, comment, imageIndex ->
            ApiRequestsSupporter.executeEnabled(w, RFandomsModerationLinkChange(xFandom.getId(), xFandom.getLanguageId(), linkId, title, url, imageIndex, comment)) { r ->
                val array = Array(links.size) {
                    if (links[it].index == linkId) {
                        val l = FandomLink()
                        l.index = linkId
                        l.imageIndex = imageIndex
                        l.url = url
                        l.title = title
                        l
                    } else links[it]
                }
                EventBus.post(EventFandomInfoLinksChanged(xFandom.getId(), xFandom.getLanguageId(), array))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }.asSheetShow()
    }

    private fun addLink() {
        if (links.size >= API.FANDOM_GALLERY_MAX) {
            ToolsToast.show(t(API_TRANSLATE.error_too_many_items))
            return
        }
        SplashLink("", "", 0) { w, url, title, comment, imageIndex ->
            ApiRequestsSupporter.executeEnabled(w, RFandomsModerationLinkAdd(xFandom.getId(), xFandom.getLanguageId(), title, url, imageIndex, comment)) { r ->
                val array = Array(links.size + 1) {
                    if (links.size == it) {
                        val link = FandomLink()
                        link.index = r.linkIndex
                        link.imageIndex = imageIndex
                        link.url = url
                        link.title = title
                        link
                    } else links[it]
                }
                EventBus.post(EventFandomInfoLinksChanged(xFandom.getId(), xFandom.getLanguageId(),  array))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }.asSheetShow()
    }


}
