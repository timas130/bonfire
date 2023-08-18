package com.sayzen.campfiresdk.screens.comments

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.adapters.AdapterComments
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class SComments constructor(
        val publicationId: Long,
        commentId: Long
) : Screen(R.layout.screen_comments) {

    companion object {

        fun instance(publicationId: Long, commentId: Long, action: NavigationAction) {
            Navigator.action(action, SComments(publicationId, commentId))
        }

    }

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventPublicationRemove::class) { this.onEventPublicationRemove(it) }
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)

    private val adapter: AdapterComments

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_comments))

        vRecycler.layoutManager = LinearLayoutManager(context)

        adapter = AdapterComments(publicationId, commentId, vRecycler)

        vFab.setOnClickListener { SplashComment(publicationId, false) { comment -> adapter.addComment(comment) }.asSheetShow() }
        vRecycler.adapter = adapter
    }


    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.publicationId == publicationId) adapter.loadBottom()
    }

    private fun onEventPublicationRemove(e: EventPublicationRemove) {
        if (e.publicationId == publicationId) Navigator.remove(this)
    }

}