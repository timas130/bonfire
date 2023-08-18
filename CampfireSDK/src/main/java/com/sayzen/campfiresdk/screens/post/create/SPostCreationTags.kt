package com.sayzen.campfiresdk.screens.post.create

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.requests.post.RPostPublication
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPostTagsChanged
import com.sayzen.campfiresdk.screens.other.rules.SGoogleRules
import com.sayzen.campfiresdk.screens.post.pending.SPending
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads
import java.util.*

class SPostCreationTags private constructor(
        private val publicationId: Long,
        private val fandomId: Long,
        private val language: Long,
        private val postParams: SPostCreate.PostParams,
        private val isAlreadyNotifyFollowers: Long,
        private val isMyPublication: Boolean,
        private val isEditTagsMode: Boolean,
        private val fandomTags: Array<PublicationTag>
) : Screen(R.layout.screen_post_create_tags) {

    companion object {

        fun instance(publicationId: Long, isMyPublication: Boolean, isEditTagsMode:Boolean, action: NavigationAction) {
            ApiRequestsSupporter.executeProgressDialog(RPostGet(publicationId)) { r ->
                instance(r.publication.id, SPostCreate.PostParams().setClosed(r.publication.closed).setTags(ControllerPublications.tagsAsLongArray(r.tags)), r.publication.tag_3, isMyPublication, r.publication.fandom.id, r.publication.fandom.languageId, isEditTagsMode, action)
            }
        }

        fun instance(publicationId: Long, postParams: SPostCreate.PostParams, publicationTag3: Long, isMyPublication: Boolean, fandomId: Long, languageId: Long, isEditTagsMode:Boolean, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RTagsGetAll(fandomId, languageId)) { r -> SPostCreationTags(publicationId, fandomId, languageId, postParams, publicationTag3, isMyPublication, isEditTagsMode, r.tags) }
        }

        fun create(publicationId: Long, postParams: SPostCreate.PostParams, onCreate: () -> Unit) {
            SGoogleRules.acceptRulesDialog {
                ApiRequestsSupporter.executeProgressDialog(RPostPublication(publicationId, postParams.tags, "", postParams.notifyFollowers, postParams.pendingTime, postParams.closed, postParams.multilingual, postParams.rubric?.id?:0, postParams.activity?.id ?: 0, postParams.nextUserId)) { _ ->
                    onCreate.invoke()
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_POST)
                    ControllerActivities.reloadActivities()
                }
            }
        }

    }


    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vLine: View = findViewById(R.id.vLine)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vContainer: ViewGroup = findViewById(R.id.vTagsContainer)
    private val vMenuContainer: ViewGroup = findViewById(R.id.vMenuContainer)
    private val vParams: Settings = findViewById(R.id.vParams)
    private val vParamsText: TextView = findViewById(R.id.vParamsText)
    private val vTextEmpty: TextView = findViewById(R.id.vTextEmpty)
    private val vImage: ImageView = findViewById(R.id.vImage)

    private val widgetTagsAdditional = SplashTagsAdditional(fandomId, language, postParams, isAlreadyNotifyFollowers, vParamsText)
    private val chips = ArrayList<ViewChip>()

    init {
        isNavigationShadowAvailable = false
        SActivityTypeBottomNavigation.setShadow(vLine)
        vTextEmpty.text = t(API_TRANSLATE.post_create_tags_empty)
        setTitle(t(API_TRANSLATE.app_tags))

        vParams.setTitle(t(API_TRANSLATE.app_additional))

        isSingleInstanceInBackStack = true

        if (fandomTags.isNotEmpty()) vMessageContainer.visibility = View.GONE
        else {
            vMessageContainer.visibility = View.VISIBLE
            ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_11).noHolder().into(vImage)
        }
        if (isEditTagsMode) {
            vMenuContainer.visibility = View.GONE
            vFab.setImageResource(R.drawable.ic_done_white_24dp)
        }

        vFab.setOnClickListener { sendPublication() }
        vParams.setOnClickListener { widgetTagsAdditional.asSheetShow() }


        setTags()
    }

    override fun onResume() {
        super.onResume()
        if (widgetTagsAdditional.needReShow)
            ToolsThreads.main(100) { widgetTagsAdditional.asSheetShow() }
    }


    private fun sendPublication() {

        val selectedTags = ArrayList<PublicationTag>()
        for (v in chips) if (v.isChecked) selectedTags.add(v.tag as PublicationTag)


        postParams.tags = Array(selectedTags.size) { selectedTags[it].id }

        val selectedTagsWithCategories = ArrayList<PublicationTag>()
        for(t in fandomTags){
            for(tt in selectedTags){
                if(tt.parentPublicationId == t.id){
                    selectedTagsWithCategories.add(t)
                    break
                }
            }
        }
        for(t in selectedTags) selectedTagsWithCategories.add(t)

        if (isMyPublication) {
            create(publicationId, postParams) {
                if(isEditTagsMode){
                    Navigator.remove(this)
                }else {
                    Navigator.removeAll(SPostCreate::class)
                    if (postParams.pendingTime > 0) Navigator.replace(SPending())
                    else SPost.instance(publicationId, 0, NavigationAction.replace())
                    EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
                }
                EventBus.post(EventPostTagsChanged(publicationId, selectedTagsWithCategories.toTypedArray()))
            }
        } else {
            SplashField()
                    .setHint(t(API_TRANSLATE.moderation_widget_comment))
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(t(API_TRANSLATE.app_change)) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(w, RPostPublication(publicationId, postParams.tags, comment, false, 0, false, false, 0, 0, 0)) {
                            Navigator.removeAll(SPostCreate::class)
                            EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
                            EventBus.post(EventPostTagsChanged(publicationId, selectedTagsWithCategories.toTypedArray()))
                            SPost.instance(publicationId, 0, NavigationAction.replace())
                        }
                    }.asSheetShow()
        }

    }


    fun setTags() {

        vContainer.removeAllViews()

        val tags = ControllerPublications.parseTags(fandomTags)

        for (tag in tags) {

            val vText: TextView = ToolsView.inflate(context, R.layout.z_text_subhead)
            vText.setPadding(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt())
            vText.text = tag.tag.name

            vContainer.addView(vText)


            val vFlow = LayoutFlow(context)

            for (t in tag.tags) {
                val v = ViewChip.instanceChooseOutline(context, t.name, t)
                for (tt in postParams.tags) if (t.id == tt) v.isChecked = true
                vFlow.addView(v)
                chips.add(v)
                if (t.imageId != 0L) {
                    v.setIcon(R.color.focus)
                    ImageLoader.load(t.imageId).size(ToolsView.dpToPx(24).toInt(), ToolsView.dpToPx(24).toInt()).intoBitmap { v.setIcon(it) }
                } else {
                    v.setIcon(null)
                }
            }

            vContainer.addView(vFlow, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            vFlow.setHorizontalSpacing(ToolsView.dpToPx(8).toInt())
            vFlow.setVerticalSpacing(ToolsView.dpToPx(8).toInt())

            (vFlow.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
            (vFlow.layoutParams as MarginLayoutParams).bottomMargin = ToolsView.dpToPx(8).toInt()

        }
    }


}
