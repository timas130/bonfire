package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.requests.rubrics.RRubricsGetAll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import kotlin.reflect.KClass

class SRubricsList constructor(
        private val fandomId: Long,
        private val languageId: Long,
        private val ownerId: Long,
        private val canCreatePost: Boolean,
        private val onSelected: ((Rubric) -> Unit)? = null
) : SLoadingRecycler<CardRubric, Rubric>(R.layout.screen_fandoms_search) {

    val eventBus = EventBus.subscribe(EventRubricCreate::class) {
        if (it.rubric.fandom.id == fandomId && it.rubric.fandom.languageId == languageId) reload()
    }

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_rubrics))
        if (ownerId == 0L) setTextEmpty(t(API_TRANSLATE.rubric_empty))
        else if (ControllerApi.isCurrentAccount(ownerId)) setTextEmpty(t(API_TRANSLATE.rubric_empty_my))
        else setTextEmpty(t(API_TRANSLATE.rubric_empty_other))
        setTextProgress(t(API_TRANSLATE.rubric_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_28)

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_RUBRIC)) (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { Navigator.to(SRubricsCreate(fandomId, languageId)) }

        if (ownerId > 0) (vFab as View).visibility = View.GONE

        adapter.setBottomLoader { onLoad, cards ->
            RRubricsGetAll(fandomId, languageId, ownerId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.rubrics) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
                .addOnLoadedPack {
                    adapter.remove(CardSpace::class)
                    adapter.add(CardSpace(72))
                }
    }

    override fun classOfCard() = CardRubric::class

    override fun map(item: Rubric): CardRubric {
        val card = CardRubric(item)
        card.canCreatePost = canCreatePost
        if (ownerId > 0) card.showFandom = true
        if (onSelected != null) card.onClick = {
            onSelected.invoke(it)
            Navigator.remove(this)
        }
        return card
    }

}
