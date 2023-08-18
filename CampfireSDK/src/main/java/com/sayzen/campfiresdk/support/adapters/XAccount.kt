package com.sayzen.campfiresdk.support.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.models.notifications.project.NotificationProjectABParamsChanged
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerEffects
import com.sayzen.campfiresdk.controllers.ControllerHoliday
import com.sayzen.campfiresdk.controllers.ControllerOnline
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectAdd
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectRemove
import com.sayzen.campfiresdk.models.events.account.EventAccountOnlineChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.account.profile.CardEffect
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.support.DrawableLevel
import com.sayzen.campfiresdk.views.SplashAccountInfo
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

class XAccount {

    companion object{

        var GLOBAL_ON_TO_PROFILE_SCREEN: (Account) -> Unit = {
            SProfile.instance(it, Navigator.TO)
        }

    }

    private var account = Account()
    private var date = 0L
    private var titleImageId = 0L
    private var titleImageGifId = 0L
    private var onChanged: () -> Unit = {}
    private var onToProfileScreen: (Account) -> Unit = GLOBAL_ON_TO_PROFILE_SCREEN

    private val drawableLevel = DrawableLevel(this)
    private var ignoreSelfUpdate = false
    var showLevel = true
    private var longClickEnabled = true
    private var clickEnabled = true

    private val eventBus = EventBus
            .subscribe(EventAccountChanged::class) { onEventAccountChanged(it) }
            .subscribe(EventAccountOnlineChanged::class) { onEventAccountOnlineChanged(it) }
            .subscribe(EventNotification::class) {
                if (it.notification is NotificationProjectABParamsChanged) onChanged.invoke()
            }
            .subscribe(EventAccountEffectAdd::class) {
                if (it.accountId == account.id) {
                    for(i in account.accountEffects) if(i.id == it.mEffect.id) {
                        onChanged.invoke()
                        return@subscribe
                    }
                    account.accountEffects = ToolsCollections.add(it.mEffect, account.accountEffects)
                    onChanged.invoke()
                }
            }
            .subscribe(EventAccountEffectRemove::class) {
                if (it.accountId == account.id) {
                    account.accountEffects = ToolsCollections.removeIf(account.accountEffects) { o -> it.effectId == o.id }
                    onChanged.invoke()
                }
            }


    init {
        ImageLoader.load(account.imageId).intoCash()
    }

    //
    //  Setters
    //

    fun setAccount(account: Account): XAccount {
        this.account = account
        ControllerOnline.set(account.id, account.lastOnlineDate)
        return this
    }

    fun setId(accountId: Long): XAccount {
        this.account.id = accountId; return this
    }

    fun setLongClickEnabled(b: Boolean): XAccount {
        this.longClickEnabled = b; return this
    }

    fun setClickEnabled(b: Boolean): XAccount {
        this.clickEnabled = b; return this
    }

    fun setImageId(imageId: Long): XAccount {
        this.account.imageId = imageId; return this
    }

    fun setLevel(level: Long): XAccount {
        this.account.lvl = level; return this
    }

    fun setKarma30(karma30: Long): XAccount {
        this.account.karma30 = karma30; return this
    }

    fun setName(name: String): XAccount {
        this.account.name = name; return this
    }

    fun setSex(sex: Long): XAccount {
        this.account.sex = sex; return this
    }

    fun setLastOnlineTime(time: Long): XAccount {
        ignoreSelfUpdate = true
        ControllerOnline.set(account.id, time)
        return this
    }

    fun setTitleImageId(titleImageId: Long): XAccount {
        this.titleImageId = titleImageId; return this
    }

    fun setTitleImageGifId(titleImageGifId: Long): XAccount {
        this.titleImageGifId = titleImageGifId; return this
    }

    fun setOnChanged(onChanged: () -> Unit): XAccount {
        this.onChanged = onChanged; return this
    }

    fun onToProfileScreen(onToProfileScreen: (Account) -> Unit): XAccount {
        this.onToProfileScreen = onToProfileScreen; return this
    }

    fun setDate(date: Long): XAccount {
        this.date = date; return this
    }

    fun setDateAccountCreated(date: Long): XAccount {
        this.account.dateCreate = date; return this
    }


    //
    //  View
    //

    fun toProfileScreen() {
        if(!clickEnabled) return
        onToProfileScreen.invoke(account)
    }

    fun showProfileDialog(){
        if(!longClickEnabled) return
        SplashAccountInfo(account).asSheetShow()
    }

    fun setView(viewAvatar: ViewAvatar) {
        setView(viewAvatar.vImageView)
        viewAvatar.setChipIcon(0)

        if (isBot()) viewAvatar.vChip.setText("B")
        else viewAvatar.vChip.setText("${account.lvl / 100}")
        viewAvatar.vChip.background = drawableLevel
        viewAvatar.vChip.visibility = if (!showLevel || account.lvl < 1) View.GONE else View.VISIBLE
        viewAvatar.setOnClickListener { toProfileScreen() }
        viewAvatar.setOnLongClickListener {
            showProfileDialog();true }
    }

    fun setView(viewAvatar: ViewAvatarTitle) {
        setView(viewAvatar.vAvatar)
        viewAvatar.setTitle(account.name)
        if (date != 0L) viewAvatar.setSubtitle(ToolsDate.dateToString(date))
    }

    fun setViewBig(vImage: ImageView) {
        if (titleImageId != 0L) ImageLoader.loadGif(titleImageId, titleImageGifId, vImage)
        else vImage.setImageBitmap(null)
    }

    fun setView(vImage: ImageView) {
        val effectImage = ControllerEffects.getAvatar(account)
        if (effectImage != null) {
            val background = ControllerEffects.getAvatarBackground(account)
            ImageLoader.load(effectImage).into(vImage)
            if (background != null) vImage.setBackgroundColor(background)
        }else {
            val holidayImage = ControllerHoliday.getAvatar(account.id, account.lvl, account.karma30)
            if (holidayImage != null) {
                val background = ControllerHoliday.getAvatarBackground(account.id)
                ImageLoader.load(holidayImage).into(vImage)
                if (background != null) vImage.setBackgroundColor(background)
            } else if (account.imageId < 1) {
                vImage.setImageResource(R.drawable.logo_campfire_128x128)
            } else {
                ImageLoader.load(account.imageId).into(vImage)
            }
        }
    }

    fun setView(vTitle: TextView) {
        vTitle.text = account.name
    }

    private fun onEventAccountChanged(e: EventAccountChanged) {
        if (account.id == e.accountId) {
            if (e.name.isNotEmpty()) account.name = e.name
            if (e.imageId != -1L) account.imageId = e.imageId
            if (e.imageTitleId != -1L) titleImageId = e.imageTitleId
            if (e.imageTitleId != -1L) titleImageGifId = e.imageTitleGifId
            onChanged.invoke()
        }
    }

    private fun onEventAccountOnlineChanged(e: EventAccountOnlineChanged) {
        if (account.id == e.accountId) {
            if (ignoreSelfUpdate) {
                ignoreSelfUpdate = false
                return
            }
            onChanged.invoke()
        }
    }

    fun cashAvatar() {
        ImageLoader.load(account.imageId).intoCash()
    }

    //
    //  Getters
    //

    fun getAccount() = account

    fun can(lvl: LvlInfoAdmin) = ControllerApi.can(lvl)

    fun can(lvl: LvlInfoUser) = ControllerApi.can(lvl)

    fun isCurrentAccount() = ControllerApi.isCurrentAccount(account.id)

    fun isOnline() = ControllerOnline.isOnline(account.id)

    fun getSponsor() = account.sponsor

    fun getLastOnlineTime() = ControllerOnline.get(account.id)

    fun isModerator() = ControllerApi.isModerator(account.lvl) && account.karma30 >= API.LVL_MODERATOR_BLOCK.karmaCount

    fun isAdmin() = ControllerApi.isAdmin(account.lvl) && account.karma30 >= API.LVL_ADMIN_MODER.karmaCount

    fun isProtoadmin() = ControllerApi.isProtoadmin(account.id, account.lvl)

    fun isUser() = !isProtoadmin() && !isModerator()

    fun getLevelColor() =  ToolsResources.getColor(if(isProtoadmin()) R.color.orange_700 else if(isAdmin()) R.color.red_700 else if(isModerator()) R.color.blue_700 else R.color.green_700)

    fun getLevelColorHex() =  if(!isOnline()) "757575" else if(isProtoadmin()) "F57C00" else if(isAdmin()) "D32F2F" else if(isModerator()) "1976D2" else "388E3C"

    fun isBot() = ControllerApi.isBot(account.name)

    fun getImageId() = account.imageId

    fun getId() = account.id

    fun getName() = account.name

    fun getLevel() = account.lvl

    fun getKarma30() = account.karma30

    fun getSex() = account.sex

    fun getTitleImageGifId() = titleImageGifId

    fun getTitleImageId() = titleImageId

    fun getDateAccountCreated() = account.dateCreate
}
