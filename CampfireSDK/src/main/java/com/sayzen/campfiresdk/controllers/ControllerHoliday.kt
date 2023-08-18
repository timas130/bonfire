package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.animations.DrawAnimationConfetti
import com.sayzen.campfiresdk.models.animations.DrawAnimationGoose
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import java.util.*

object ControllerHoliday {

    var myAnimation: DrawAnimation? = null

    fun onAppStart() {
        if (isNewYear()) setAnimation(DrawAnimationSnow(ControllerSettings.styleNewYearSnow))
        if (isFirstApril()) setAnimation(DrawAnimationGoose())
        if (isBirthday())  setAnimation(DrawAnimationConfetti())
    }

    fun setAnimation(animation: DrawAnimation){
        this.myAnimation = animation
        ControllerScreenAnimations.addAnimationWithClear(animation)
    }

    fun isCanChangeAnimation(animation: DrawAnimation?):Boolean{
        if(animation == myAnimation) return true
        if (isFirstApril()) {
            val current = ControllerScreenAnimations.getCurrentAnimation()
            if (current is DrawAnimationGoose) {
                current.makeCrazy()
                return false
            }
        }
        return true
    }

    fun getProfileBackgroundImage(): Long? {
        if (isNewYear()) {
            return API_RESOURCES.IMAGE_NEW_YEAR_LIGHT_GIF
        }
        return null
    }

    fun getAvatar(accountId: Long, accountLvl: Long, karma30: Long): Long? {


        if (isNewYear() && ControllerSettings.styleNewYearAvatars) {

            if (ControllerApi.isProtoadmin(accountId, accountLvl)) return API_RESOURCES.IMAGE_NEW_YEAR_SANTA
            if (ControllerApi.isAdmin(accountLvl, karma30)) {
                val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_DEER_1, API_RESOURCES.IMAGE_NEW_YEAR_DEER_2, API_RESOURCES.IMAGE_NEW_YEAR_DEER_3, API_RESOURCES.IMAGE_NEW_YEAR_DEER_4, API_RESOURCES.IMAGE_NEW_YEAR_DEER_5)
                return array[(accountId % array.size).toInt()]
            }
            if (ControllerApi.isModerator(accountLvl, karma30)) {
                val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_ELF_1, API_RESOURCES.IMAGE_NEW_YEAR_ELF_2, API_RESOURCES.IMAGE_NEW_YEAR_ELF_3, API_RESOURCES.IMAGE_NEW_YEAR_ELF_4)
                return array[(accountId % array.size).toInt()]
            }
            val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_KID_1, API_RESOURCES.IMAGE_NEW_YEAR_KID_2, API_RESOURCES.IMAGE_NEW_YEAR_KID_3, API_RESOURCES.IMAGE_NEW_YEAR_KID_4, API_RESOURCES.IMAGE_NEW_YEAR_KID_5, API_RESOURCES.IMAGE_NEW_YEAR_KID_6, API_RESOURCES.IMAGE_NEW_YEAR_KID_7, API_RESOURCES.IMAGE_NEW_YEAR_KID_8, API_RESOURCES.IMAGE_NEW_YEAR_KID_9, API_RESOURCES.IMAGE_NEW_YEAR_KID_10)
            return array[(accountId % array.size).toInt()]
        }
        return null
    }

    fun getAvatarBackground(accountId: Long): Int? {
        if (isNewYear() && ControllerSettings.styleNewYearAvatars) {
            val array = arrayOf(R.color.blue_500, R.color.light_blue_500,
                    R.color.cyan_500, R.color.indigo_300, R.color.red_500,
                    R.color.yellow_500, R.color.lime_500, R.color.light_green_500,
                    R.color.teal_500, R.color.deep_purple_300, R.color.purple_300,
                    R.color.pink_400, R.color.deep_orange_400)
            return ToolsResources.getColor(array[(accountId % array.size).toInt()])
        }
        return null
    }

    fun isHoliday() = isNewYear() || isFirstApril() || isBirthday()

    fun isNewYear(): Boolean {
        val instance = GregorianCalendar.getInstance()
        instance.timeInMillis = ControllerApi.currentTime()

        return (instance.get(Calendar.MONTH) + 1 == 12 && instance.get(Calendar.DAY_OF_MONTH) > 28)
                ||
                (instance.get(Calendar.MONTH) + 1 == 1 && instance.get(Calendar.DAY_OF_MONTH) < 2)
    }

    fun isFirstApril(): Boolean {
        val instance = GregorianCalendar.getInstance()
        instance.timeInMillis = ControllerApi.currentTime()
        return instance.get(Calendar.MONTH) + 1 == 4 && instance.get(Calendar.DAY_OF_MONTH) == 1
    }

    fun isBirthday(): Boolean {
        val instance = GregorianCalendar.getInstance()
        instance.timeInMillis = ControllerApi.currentTime()
        return instance.get(Calendar.MONTH) + 1 == 4 && instance.get(Calendar.DAY_OF_MONTH) == 10
    }


}