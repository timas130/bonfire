package com.sayzen.campfiresdk.screens.fandoms.rubrics

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.post.RPostGetAllByRubric
import com.dzen.campfire.api.requests.rubrics.RRubricGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerRubrics
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeName
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeOwner
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SRubricPosts(
        val rubric: Rubric
) : SLoadingRecycler<CardPublication, Publication>() {

    companion object{

        fun instance(rubricId:Long, action: NavigationAction){
            ApiRequestsSupporter.executeInterstitial(action, RRubricGet(rubricId)) { r ->
                SRubricPosts(r.rubric)
            }
        }

    }

    val eventBus = EventBus
            .subscribe(EventRubricChangeName::class){ if(rubric.id == it.rubricId) rubric.name = it.rubricName }
            .subscribe(EventRubricChangeOwner::class){ if(rubric.id == it.rubricId) rubric.owner = it.owner }
            .subscribe(EventRubricRemove::class){ if(rubric.id == it.rubricId) Navigator.remove(this) }
            .subscribe(EventRubricChangeName::class){
                if(rubric.id == it.rubricId){
                    rubric.name = it.rubricName
                    setTitle(rubric.name)
                }
            }

    init {
        disableNavigation()
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(rubric.name)
        setTextEmpty(t(API_TRANSLATE.rubric_posts_empty))
        addToolbarIcon(R.drawable.ic_more_vert_white_24dp){
            ControllerRubrics.instanceMenu(rubric).asSheetShow()
        }

        adapter.setBottomLoader { onLoad, cards ->
            RPostGetAllByRubric(rubric.id, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, vRecycler)

}
