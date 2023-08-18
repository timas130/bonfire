package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerFandoms
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemove
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardFandom constructor(
        val fandom: Fandom,
        var onClick: ((Long) -> Unit)? = null,
        val showLanguage: Boolean = false,
        var onLongClick: (() -> Unit)? = null,
) : Card(R.layout.card_fandom) {

    private val eventBus = EventBus
            .subscribe(EventFandomRemove::class) { if (it.fandomId == fandom.id) adapter.remove(this) }
            .subscribe(EventFandomSubscribe::class) {
                if (removeIfUnsubscribe && subscribed && xFandom.getId() == it.fandomId && xFandom.getLanguageId() == it.languageId && it.subscriptionType == API.PUBLICATION_IMPORTANT_NONE) {
                    adapter.remove(this)
                }
            }

    private val xFandom = XFandom().setFandom(fandom).setOnChanged { update() }
    private val subscribesCount = fandom.subscribesCount
    var subscribed = false
    var showSubscribes = true
    var avatarClickable = true
    var removeIfUnsubscribe = false
    private var avatarSize: Int? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vSubscribers: TextView = view.findViewById(R.id.vSubscribers)
        val vTouch: View = view.findViewById(R.id.vTouch)


        if (showSubscribes) {
            vSubscribers.text = "$subscribesCount"
            vSubscribers.visibility = View.VISIBLE
        } else {
            vSubscribers.visibility = View.INVISIBLE
        }

        vTouch.setOnClickListener { onClick() }
        ToolsView.setOnLongClickCoordinates(vTouch) { v, x, y -> onLongClick(v, x, y) }
        xFandom.setView(vAvatar)

        if (!avatarClickable) vAvatar.vAvatar.setOnClickListener(null)

        if (avatarSize != null) {
            vAvatar.vAvatar.layoutParams.width = avatarSize!!
            vAvatar.vAvatar.layoutParams.height = avatarSize!!
        }
    }

    private fun onClick() {
        if (onClick != null) {
            onClick?.invoke(fandom.languageId)
            return
        }
        SFandom.instance(xFandom.getFandom(), Navigator.TO)
    }

    private fun onLongClick(view: View, x: Float, y: Float) {
        if (onLongClick != null) {
            onLongClick?.invoke()
            return
        }
        ControllerFandoms.showPopupMenu(xFandom, view, x, y, onClick.takeIf { showLanguage })
    }

    //
    //  Setters
    //

    fun setAvatarSize(avatarSize: Int): CardFandom {
        this.avatarSize = avatarSize
        update()
        return this
    }

    fun setSubscribed(subscribed: Boolean): CardFandom {
        this.subscribed = subscribed
        return this
    }

    fun setShowSubscribes(showSubscribes: Boolean): CardFandom {
        this.showSubscribes = showSubscribes
        return this
    }

    fun getFandomId() = xFandom.getId()
}
