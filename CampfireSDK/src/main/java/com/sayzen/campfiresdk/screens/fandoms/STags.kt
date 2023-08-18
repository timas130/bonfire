package com.sayzen.campfiresdk.screens.fandoms

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.sayzen.campfiresdk.screens.fandoms.tags.SplashTagCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomTagMove
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.objects.TagParent
import com.sayzen.campfiresdk.models.splashs.SplashCategoryCreate
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

class STags private constructor(
        private val fandomId: Long,
        private val languageId: Long,
        tagsOriginal: Array<PublicationTag>
) : Screen(R.layout.screen_tags) {


    companion object {

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RTagsGetAll(fandomId, languageId)) { r ->
                STags(fandomId, languageId, r.tags)
            }
        }
    }

    private val eventBus = EventBus.subscribe(EventFandomTagMove::class){
        Navigator.back()
        instance(fandomId, languageId, Navigator.TO)
    }


    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vContainer: ViewGroup = findViewById(R.id.vTagsContainer)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vImage: ImageView= findViewById(R.id.vImage)
    private val vTextEmpty: TextView = findViewById(R.id.vTextEmpty)
    private val tags: Array<TagParent>

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_tags))

        vTextEmpty.text = t(API_TRANSLATE.post_create_tags_empty)

        vFab.setOnClickListener { onActionClicked() }
        (vFab as View).visibility = if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_TAGS)) View.VISIBLE else View.GONE

        tags = ControllerPublications.parseTags(tagsOriginal)

        if (tags.isNotEmpty()) vMessageContainer.visibility = View.GONE
        else {
            vMessageContainer.visibility = View.VISIBLE
            ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_11).noHolder().into(vImage)
        }

        for (tag in tags) {

            val vText: TextView = ToolsView.inflate(context, R.layout.z_text_subhead_touch)
            vText.text = tag.tag.name
            vText.setPadding(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt())
            vText.setOnClickListener { SPostsSearch.instance(tag.tag, Navigator.TO) }

            vContainer.addView(vText, vContainer.childCount - 1)
            ControllerPublications.createTagMenu(vText, tag.tag, true, tags)

            val vFlow = LayoutFlow(context)

            for (t in tag.tags) {
                val v = ViewChip.instanceOutline(context)
                v.text = t.name
                v.setOnClickListener { SPostsSearch.instance(t, Navigator.TO) }
                v.tag = t.id
                vFlow.addView(v)
                ControllerPublications.createTagMenu(v, t, true, tags)
                if (t.imageId != 0L) {
                    v.setIcon(R.color.focus)
                    ImageLoader.load(t.imageId).size(ToolsView.dpToPx(24).toInt(),ToolsView.dpToPx(24).toInt()).intoBitmap { v.setIcon(it) }
                }else{
                    v.setIcon(null)
                }
            }
            vContainer.addView(vFlow, vContainer.childCount - 1, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

            vFlow.setHorizontalSpacing(ToolsView.dpToPx(8).toInt())
            vFlow.setVerticalSpacing(ToolsView.dpToPx(8).toInt())

            (vFlow.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
            (vFlow.layoutParams as MarginLayoutParams).bottomMargin = ToolsView.dpToPx(8).toInt()

        }
    }

    override fun onFirstShow() {
        super.onFirstShow()

        for(s in Navigator.currentStack.stackCopy())
            if(s != this && s is STags && s.fandomId == fandomId && s.languageId == languageId)
                Navigator.remove(s)
    }

    private fun onActionClicked() {

        SplashMenu()
                .add(t(API_TRANSLATE.app_tag)) {  createTag() }
                .add(t(API_TRANSLATE.app_category)) {  SplashCategoryCreate(fandomId, languageId) }
                .asSheetShow()
    }

    private fun createTag() {

        if(tags.isEmpty()){
            ToolsToast.show(t(API_TRANSLATE.error_cant_create_tag_without_category))
            return
        }

        val menu = SplashMenu()
        for (tag in tags)
            menu.add(tag.tag.name) {  SplashTagCreate(tag.tag.id, fandomId, languageId) }
        menu.asSheetShow()
    }

}
