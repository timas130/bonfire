package com.sayzen.campfiresdk.controllers

import android.annotation.SuppressLint
import com.sayzen.campfiresdk.models.animations.DrawAnimationBox
import com.sayzen.campfiresdk.models.animations.DrawAnimationConfetti
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.android.views.views.draw_animations.DrawAnimationExplosion
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

object ControllerScreenAnimations {

    private var key = 0L
    private var currentAnimation: DrawAnimation? = null

    fun fireworks(clear: Boolean = true) {
        if (!ControllerHoliday.isCanChangeAnimation(null)) return
        if (!ControllerEffects.isCanChangeAnimation(null)) return

        if (clear) clearAnimation()
        val myKey = System.currentTimeMillis()
        key = myKey

        val cw = ToolsAndroid.getScreenW() / 2f
        val ch = ToolsAndroid.getScreenH() / 2f
        val of = ToolsView.dpToPx(128)

        ToolsThreads.thread {
            ToolsThreads.sleep(500)
            if (key != myKey) return@thread
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 60, cw, ch, 2f))
            ToolsThreads.sleep(500)
            if (key != myKey) return@thread
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 40, cw - of, ch, 2f))
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 40, cw + of, ch, 2f))
            ToolsThreads.sleep(1000)
            for (i in 0..10) {
                if (key != myKey) return@thread
                val size = ToolsView.dpToPx(ToolsMath.randomInt(40, 80))
                val sizeParticle = ToolsView.dpToPx(ToolsMath.randomInt(4, 8))
                val count = ToolsMath.randomInt(20, 40)
                val time = ToolsMath.randomFloat(1f, 3f)
                val xx = ToolsMath.randomFloat(cw - of, cw + of)
                val yy = ToolsMath.randomFloat(ch - of, ch + of)
                addAnimation(DrawAnimationExplosion(size, sizeParticle, count, xx, yy, time))
                ToolsThreads.sleep(ToolsMath.randomLong(100, 300))
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun parseHolidayClick():Boolean{
        if (ControllerHoliday.isBirthday()) {
            val current = getCurrentAnimation()
            if (current is DrawAnimationConfetti) {
                current.inflate(1000)
                ToolsVibration.vibrate(500L)
                return true
            }
        }
        return false
    }

    fun box(count: Int, clear: Boolean = true) {
        if (clear) addAnimationWithClear(DrawAnimationBox(count))
        else addAnimation(DrawAnimationBox(count))
    }

    fun clearAnimation() {
        key = 0
        ToolsThreads.main { SupAndroid.activity!!.vActivityDrawAnimations!!.clear() }
    }

    fun getCurrentAnimation(): DrawAnimation? {
        val list = SupAndroid.activity!!.vActivityDrawAnimations!!.getAnimations()
        return if (list.isEmpty()) null else list[0]
    }

    fun addAnimationWithClear(animation: DrawAnimation) {
        if (!ControllerHoliday.isCanChangeAnimation(animation)) return
        if (!ControllerEffects.isCanChangeAnimation(animation)) return

        if (SupAndroid.activity!!.vActivityDrawAnimations!!.contains(animation::class)) return
        clearAnimation()
        val myKey = System.currentTimeMillis()
        key = myKey
        currentAnimation = animation

        addAnimation(animation)
    }

    fun addAnimation(animation: DrawAnimation) {
        ToolsThreads.main { SupAndroid.activity!!.vActivityDrawAnimations!!.addAnimation(animation) }
    }

    fun getAnimation() = currentAnimation

}