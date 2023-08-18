package com.sayzen.campfiresdk.support.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomClose
import com.sayzen.campfiresdk.models.events.publications.EventPublicationFandomChanged
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class XFandom() {

    private var fandom = Fandom()
    private var publicationId = 0L
    private var date= 0L
    private var showLanguage = true
    private var allViewIsClickable = false
    private var onChanged: () -> Unit = {}

    private val eventBus = EventBus
            .subscribe(EventPublicationFandomChanged::class) { onEventPublicationFandomChanged(it) }
            .subscribe(EventFandomChanged::class) { onEventFandomChanged(it) }
            .subscribe(EventFandomClose::class) { onEventFandomClose(it) }

    init {
        ImageLoader.load(fandom.imageId).intoCash()
    }

    //
    //  Setters
    //

    fun setId(fandomId: Long): XFandom {
        this.fandom.id = fandomId; return this
    }

    fun setLanguageId(languageId: Long): XFandom {
        this.fandom.languageId = languageId; return this
    }

    fun setName(name: String): XFandom {
        this.fandom.name = name; return this
    }

    fun setImageId(imageId: Long): XFandom {
        this.fandom.imageId = imageId; return this
    }

    fun setFandom(fandom: Fandom): XFandom {
        this.fandom = fandom; return this
    }

    fun setPublicationId(publicationId: Long): XFandom {
        this.publicationId = publicationId; return this
    }

    fun setDate(date: Long): XFandom {
        this.date = date; return this
    }

    fun setShowLanguage(showLanguage: Boolean): XFandom {
        this.showLanguage = showLanguage; return this
    }


    fun setAllViewIsClickable(allViewIsClickable: Boolean): XFandom {
        this.allViewIsClickable = allViewIsClickable; return this
    }

    fun setOnChanged(onChanged: () -> Unit): XFandom {
        this.onChanged = onChanged; return this
    }

    fun setImageTitleId(imageId:Long): XFandom {
        this.fandom.imageTitleId = imageId; return this
    }

    fun setImageTitleGifId(imageId:Long): XFandom {
        this.fandom.imageTitleGifId = imageId; return this
    }

    //
    //  View
    //

    fun toFandom(){
        if(fandom.karmaCof == 0L) SFandom.instance(fandom.id, fandom.languageId, Navigator.TO)
        else SFandom.instance(fandom.copy(), Navigator.TO)
    }

    fun setView(viewAvatar: ViewAvatar) {
        ImageLoader.load(fandom.imageId).into(viewAvatar.vImageView)
        viewAvatar.setChipIcon(0)
        viewAvatar.setChipText("")

        if (showLanguage && fandom.languageId != 0L && fandom.languageId != ControllerApi.getLanguageId()) {
            ControllerApi.getIconForLanguage(fandom.languageId).into(viewAvatar.vChipIcon){
                viewAvatar.vChipIcon.visibility = View.VISIBLE
            }
        }else{
            viewAvatar.vChip.setBackgroundColor(Color.TRANSPARENT)
            viewAvatar.vChipIcon.setImageDrawable(null)
            viewAvatar.vChipIcon.visibility = View.GONE
        }

        viewAvatar.setOnClickListener { toFandom()  }
    }

    fun setView(viewAvatar: ViewAvatarTitle) {
        setView(viewAvatar.vAvatar)

        if (fandom.name.isNotEmpty()) viewAvatar.setTitle(fandom.name)
        if (date != 0L) viewAvatar.setSubtitle(ToolsDate.dateToString(date))
        if (allViewIsClickable) {
            viewAvatar.vAvatar.setOnClickListener(null)
            viewAvatar.setOnClickListener {  toFandom() }
        }
    }

    fun setViewBig(vImage: ImageView) {
        if (fandom.imageTitleId != 0L) ImageLoader.loadGif(fandom.imageTitleId, fandom.imageTitleGifId, vImage)
        else vImage.setImageBitmap(null)
    }

    fun setView(vImage: ImageView) {
        ImageLoader.load(fandom.imageId).into(vImage)
    }

    fun setView(vText: TextView) {
        vText.text = fandom.name
    }

    fun cashAvatar(){
        ImageLoader.load(fandom.imageId).intoCash()
    }

    //
    //  Getters
    //

    fun getId() = fandom.id

    fun getLanguageId() = fandom.languageId

    fun getImageId() = fandom.imageId

    fun getName() = fandom.name

    fun getImageTitleGifId() = fandom.imageTitleGifId

    fun getImageTitleId() = fandom.imageTitleId

    fun isClosed() = fandom.closed

    fun getFandom() = fandom

    //
    //  EventBus
    //

    private fun onEventFandomChanged(e: EventFandomChanged) {
        if (e.fandomId == fandom.id) {
            if (e.name.isNotEmpty()) fandom.name = e.name
            if (e.imageId != -1L) fandom.imageId = e.imageId
            if (e.imageTitleId != -1L) fandom.imageTitleId = e.imageTitleId
            if (e.imageTitleGifId != -1L) fandom.imageTitleGifId = e.imageTitleGifId
            onChanged.invoke()
        }
    }

    private fun onEventPublicationFandomChanged(e: EventPublicationFandomChanged) {
        if (e.publicationId == publicationId) {
            fandom.id = e.fandomId
            fandom.languageId = e.languageId
            fandom.name = e.fandomName
            fandom.imageId = e.fandomImageId
            onChanged.invoke()
        }
    }

    private fun onEventFandomClose(e: EventFandomClose) {
        if (e.fandomId == fandom.id) {
            fandom.closed = e.closed
            onChanged.invoke()
        }
    }


    //
    //  Getters
    //

    fun linkTo() = ControllerLinks.linkToFandom(fandom.id)

    fun linkToWithLanguage() = ControllerLinks.linkToFandom(fandom.id, fandom.languageId)


}
