package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.api.models.notifications.account.NotificationEffectAdd
import com.dzen.campfire.api.models.notifications.account.NotificationEffectRemove
import com.dzen.campfire.api.requests.accounts.RAccountsAdminEffectAdd
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.animations.DrawAnimationGoose
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sayzen.campfiresdk.models.events.account.EventAccountCurrentChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectAdd
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectRemove
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashChooseDate
import com.sup.dev.android.views.splash.SplashChooseTime
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

object ControllerEffects {

    var myAnimation: DrawAnimation? = null
    var myAnimationEffectId: Long? = null

    private val eventBus = EventBus
            .subscribe(EventNotification::class) {
                if (it.notification is NotificationEffectAdd) {
                    EventBus.post(EventAccountEffectAdd(it.notification.mAccEffect.accountId, it.notification.mAccEffect))
                    update()
                }
                if (it.notification is NotificationEffectRemove) {
                    EventBus.post(EventAccountEffectRemove(ControllerApi.account.getId(), it.notification.effectId))
                    update()
                }
            }
            .subscribe(EventAccountCurrentChanged::class) { update() }

    fun update() {
        if (myAnimationEffectId != null) {
            var found = false
            for (i in ControllerApi.account.getAccount().accountEffects) {
                if (i.id == myAnimationEffectId) {
                    found = true
                    break
                }
            }
            if (!found) {
                this.myAnimation = null
                this.myAnimationEffectId = null
                ControllerScreenAnimations.clearAnimation()
            }
        }

        for (i in ControllerApi.account.getAccount().accountEffects) {
            if (i.effectIndex == API.EFFECT_INDEX_SNOW && i.dateEnd > System.currentTimeMillis()) {
                setAnimation(DrawAnimationSnow(), i.id)
                return
            }
            if (i.effectIndex == API.EFFECT_INDEX_GOOSE && i.dateEnd > System.currentTimeMillis()) {
                setAnimation(DrawAnimationGoose(), i.id)
                return
            }
        }
    }

    fun get(effectIndex:Long):MAccountEffect?{
        val effects = ControllerApi.account.getAccount().accountEffects
        for(i in effects){
            if(i.effectIndex == effectIndex && i.dateEnd > System.currentTimeMillis()){
                return i
            }
        }
        return null
    }

    fun setAnimation(animation: DrawAnimation, effectId: Long) {
        this.myAnimation = animation
        this.myAnimationEffectId = effectId
        ControllerScreenAnimations.addAnimationWithClear(animation)
    }

    fun isCanChangeAnimation(animation: DrawAnimation?): Boolean {
        if (animation == myAnimation) return true
        for (i in ControllerApi.account.getAccount().accountEffects) {
            if (i.effectIndex == API.EFFECT_INDEX_SNOW && i.dateEnd > System.currentTimeMillis()) {
                ToolsToast.show(t(API_TRANSLATE.effect_toast_cant_change_animation))
                return false
            }
            if (i.effectIndex == API.EFFECT_INDEX_GOOSE && i.dateEnd > System.currentTimeMillis()) {
                val current = ControllerScreenAnimations.getCurrentAnimation()
                if (current is DrawAnimationGoose) {
                    current.makeCrazy()
                    return false
                }
                return false
            }
        }
        return true
    }

    fun addEffect(accountId: Long) {
        val w = SplashMenu()
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_HATE)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_PIG)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_VAHTER)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_GOOSE)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_SNOW)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_ADMIN_BAN)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_TRANSLATOR)
        addEffect_buildMenu(w, accountId, API.EFFECT_INDEX_MENTION_LOCK)
        w.asSheetShow()
    }

    fun addEffect_buildMenu(w: SplashMenu, accountId: Long, effectIndex: Long) {
        w.add(getTitle(effectIndex)).description(getDescription(effectIndex)).onClick { addEffect(accountId, effectIndex) }
    }

    fun addEffect(accountId: Long, effectIndex: Long) {
        SplashChooseDate()
                .setOnEnter(t(API_TRANSLATE.app_choose)) { _, date ->
                    SplashChooseTime()
                            .setOnEnter(t(API_TRANSLATE.app_choose)) { _, h, m ->
                                val endDate = ToolsDate.getStartOfDay_GlobalTimeZone(date) + (h * 60L * 60 * 1000) + (m * 60L * 1000)
                                if (endDate < System.currentTimeMillis()) {
                                    ToolsToast.show(t(API_TRANSLATE.effect_error_time))
                                    return@setOnEnter
                                }
                                addEffect(accountId, effectIndex, endDate)
                            }
                            .asSheetShow()
                }
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .asSheetShow()
    }

    fun addEffect(accountId: Long, effectIndex: Long, endDate: Long) {
        ControllerApi.moderation(t(API_TRANSLATE.profile_add_effect), t(API_TRANSLATE.app_apply_effect), { RAccountsAdminEffectAdd(accountId, effectIndex, endDate, it) }) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun getTitle(mAccountEffect: MAccountEffect) = getTitle(mAccountEffect.effectIndex)

    fun getTitle(effectIndex: Long): String {
        return when (effectIndex) {
            API.EFFECT_INDEX_HATE -> t(API_TRANSLATE.effect_title_hater)
            API.EFFECT_INDEX_PIG -> t(API_TRANSLATE.effect_title_pig)
            API.EFFECT_INDEX_VAHTER -> t(API_TRANSLATE.effect_title_vahter)
            API.EFFECT_INDEX_GOOSE -> t(API_TRANSLATE.effect_title_goose)
            API.EFFECT_INDEX_SNOW -> t(API_TRANSLATE.effect_title_snow)
            API.EFFECT_INDEX_TRANSLATOR -> t(API_TRANSLATE.effect_title_translator)
            API.EFFECT_INDEX_ADMIN_BAN -> t(API_TRANSLATE.effect_title_addmin_ban)
            API.EFFECT_INDEX_MENTION_LOCK -> t(API_TRANSLATE.effect_title_mention_lock)
            else -> "null"
        }
    }

    fun getDescription(mAccountEffect: MAccountEffect) = getDescription(mAccountEffect.effectIndex)

    fun getDescription(effectIndex: Long): String {
        return when (effectIndex) {
            API.EFFECT_INDEX_HATE -> t(API_TRANSLATE.effect_title_hater_description)
            API.EFFECT_INDEX_PIG -> t(API_TRANSLATE.effect_title_pig_description)
            API.EFFECT_INDEX_VAHTER -> t(API_TRANSLATE.effect_title_vahter_description)
            API.EFFECT_INDEX_GOOSE -> t(API_TRANSLATE.effect_title_goose_description)
            API.EFFECT_INDEX_SNOW -> t(API_TRANSLATE.effect_title_snow_description)
            API.EFFECT_INDEX_TRANSLATOR -> t(API_TRANSLATE.effect_title_translator_description)
            API.EFFECT_INDEX_ADMIN_BAN -> t(API_TRANSLATE.effect_title_addmin_ban_description)
            API.EFFECT_INDEX_MENTION_LOCK -> t(API_TRANSLATE.effect_title_mention_lock_description)
            else -> "null"
        }
    }

    fun getSource(mAccountEffect: MAccountEffect): String {
        return if (mAccountEffect.tag == API.EFFECT_TAG_SOURCE_SYSTEM) return "{${CampfireConstants.YELLOW} ${t(API_TRANSLATE.app_system)}}"
        else ControllerLinks.linkToAccount(mAccountEffect.fromAccountName)
    }

    fun getComment(mAccountEffect: MAccountEffect): String {
        if (mAccountEffect.tag != API.EFFECT_TAG_SOURCE_SYSTEM) return mAccountEffect.comment
        return when (mAccountEffect.effectIndex) {
            API.EFFECT_INDEX_HATE ->
                if (mAccountEffect.commentTag == API.EFFECT_COMMENT_TAG_GODS) t(API_TRANSLATE.effect_title_hater_comment_gods)
                else t(API_TRANSLATE.effect_title_hater_comment)
            API.EFFECT_INDEX_PIG -> t(API_TRANSLATE.effect_title_pig_comment)
            API.EFFECT_INDEX_VAHTER ->
                if (mAccountEffect.commentTag == API.EFFECT_COMMENT_TAG_REJECTED) t(API_TRANSLATE.effect_title_vahter_comment_rejected)
                else t(API_TRANSLATE.effect_title_vahter_comment_too_namy)
            else -> mAccountEffect.comment
        }
    }

    //
    //  Effects
    //


    fun getAvatar(account: Account): Long? {

        for (i in account.accountEffects) {
            if (i.effectIndex == API.EFFECT_INDEX_PIG && i.dateEnd > System.currentTimeMillis()) {
                return API_RESOURCES.IMAGE_NEW_YEAR_PIG
            }
        }

        return null
    }

    fun getAvatarBackground(account: Account): Int? {

        for (i in account.accountEffects) {
            if (i.effectIndex == API.EFFECT_INDEX_PIG && i.dateEnd > System.currentTimeMillis()) {
                return R.color.blue_500
            }
        }

        return null
    }


}