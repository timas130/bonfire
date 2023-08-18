package com.sayzen.campfiresdk.screens.activities.administration.reports

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsReportedGetAll
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads
import kotlin.reflect.KClass

class SAdministrationReports : SLoadingRecycler<CardPublication, Publication>() {

    private var languages = arrayListOf(*ControllerSettings.adminReportsLanguages)

    private val eventBus = EventBus.subscribe(EventPublicationReportsClear::class) {
        for (c in adapter.get(CardPublication::class)) if (c.xPublication.publication.id == it.publicationId) adapter.remove(c)
    }

    init {
        disableNavigation()

        vScreenRoot?.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_15)
        setTitle(t(API_TRANSLATE.moderation_screen_reports))
        setTextEmpty(t(API_TRANSLATE.moderation_screen_reports_empty))
        addToolbarIcon(R.drawable.ic_translate_white_24dp){
            ControllerCampfireSDK.createLanguageCheckMenu(languages)
                    .setOnEnter(t(API_TRANSLATE.app_save))
                    .setOnHide {
                        ControllerSettings.adminReportsLanguages = languages.toTypedArray()
                        reload()
                    }
                    .asSheetShow()
        }

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsReportedGetAll(0, languages.toTypedArray(), cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.publications)
                        ToolsThreads.main { afterPackLoaded() }
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, null, false, false, isShowReports= true)


    override fun reload() {
        adapter.clear()
        super.reload()
    }

    private fun afterPackLoaded() {
        var i = 0
        while (i < adapter.size()) {
            if (adapter[i] is CardPublication) {
                if (i != adapter.size() - 1) {
                    if (adapter[i + 1] is CardPublication) {
                        adapter.add(i + 1, CardUnitReport(adapter[i] as CardPublication))
                        i++
                    }
                } else {
                    adapter.add(i + 1, CardUnitReport((adapter[i] as CardPublication)))
                    i++
                }
            }
            i++
        }
    }


}