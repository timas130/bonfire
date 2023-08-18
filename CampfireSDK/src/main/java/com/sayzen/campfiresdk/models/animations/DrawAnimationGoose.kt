package com.sayzen.campfiresdk.models.animations

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath

class DrawAnimationGoose : DrawAnimation() {

    private val grey = 0x40FFFFFF
    private val color_eyeClosed = 0x80000000.toInt()
    private val orange = ToolsResources.getColor(R.color.orange_600)
    private val DP = ToolsView.dpToPx(1)
    private val SPEED = DP * 60f
    private val particles = ArrayList<Particle>()

    private var x = -500f
    private var y = ToolsMath.randomFloat(0f, ToolsAndroid.getScreenH().toFloat())
    private var axeX = 0f
    private var axeY = 0f
    private var nextParticleTime = 0.8f
    private var eyeTimer = 0f
    private var eyeClosed = false
    private var eyeClosedForce = false

    private var action: Action = ActionWaite()

    override fun update(delta: Float) {
        super.update(delta)
        action.update(delta)
        if (action.isDone()) {
            action.onFinish()
            action = action.nextAction()
        }

        nextParticleTime -= delta
        if (nextParticleTime <= 0) {
            nextParticleTime = 0.5f
            val r = DP * 2
            particles.add(Particle(x + DP * 14 + ToolsMath.randomFloat(-r, r), y + DP * 16 + ToolsMath.randomFloat(-r, r)))
            particles.add(Particle(x + DP * 14 + DP * 8 + ToolsMath.randomFloat(-r, r), y + DP * 16 + DP * 6 + ToolsMath.randomFloat(-r, r)))
        }

        while (particles.size > 500) particles.removeAt(0)

        eyeTimer -= delta
        if (eyeTimer <= 0) {
            eyeClosed = !eyeClosed
            if (eyeClosedForce) eyeClosed = true
            eyeTimer = if (eyeClosed) 0.3f else 4f
        }

    }

    override fun draw(canvas: Canvas) {

        paint.color = grey
        for (i in particles) i.draw(canvas)

        if (axeX > 0) {
            paint.color = Color.WHITE
            canvas.drawCircle(x + DP * 20 + DP * 16, y, DP * 12, paint)
            canvas.drawCircle(x + DP * 20 + DP * 16 + DP * 2, y + DP * 2, DP * 8, paint)
            paint.color = orange
            canvas.drawCircle(x + DP * 20 + DP * 16 + DP * 10, y + DP * 4, DP * 6, paint)
            canvas.drawCircle(x + DP * 20 + DP * 16 + DP * 13, y + DP * 6, DP * 4, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x + DP * 20 + DP * 16 + DP * 10, y - DP * 4, DP * 2, paint)
            canvas.drawCircle(x + DP * 20 + DP * 16 + DP * 6, y - DP * 4, DP * 2, paint)
        } else {
            paint.color = Color.WHITE
            canvas.drawCircle(x, y, DP * 12, paint)
            canvas.drawCircle(x + DP * 2, y + DP * 2, DP * 8, paint)
            paint.color = orange
            canvas.drawCircle(x - DP * 10, y + DP * 4, DP * 6, paint)
            canvas.drawCircle(x - DP * 13, y + DP * 6, DP * 4, paint)
            paint.color = if (eyeClosed) color_eyeClosed else Color.BLACK
            val eyeR = if (eyeClosed) DP else DP * 2
            canvas.drawCircle(x - DP * 10, y - DP * 4, eyeR, paint)
            canvas.drawCircle(x - DP * 6, y - DP * 4, eyeR, paint)
        }


        paint.color = Color.WHITE
        canvas.drawCircle(x + DP * 10, y + DP * 16, DP * 14, paint)
        canvas.drawCircle(x + DP * 10 + DP * 16, y + DP * 16, DP * 14, paint)
        canvas.drawRect(x + DP * 10, y + DP * 16 - DP * 14, x + DP * 10 + DP * 16, y + DP * 16 + DP * 14, paint)

        action.draw(canvas)

        super.draw(canvas)
    }

    fun makeCrazy() {
        action.onFinish()
        action = ActionCrazy()
    }

    //
    //  Particles
    //

    inner class Particle(
            val x: Float,
            val y: Float
    ) {

        fun draw(canvas: Canvas) {
            canvas.drawCircle(x, y, DP * 3, paint)
        }

    }

    //
    //  Actions
    //

    abstract inner class Action {

        abstract fun update(delta: Float)
        abstract fun isDone(): Boolean
        open fun nextAction(): Action = ActionWaite()
        open fun draw(canvas: Canvas) {

        }

        open fun onFinish() {

        }

    }

    inner class ActionMove(
            val speed: Float
    ) : Action() {

        private var targetX = x
        private var targetY = y

        init {
            targetX = ToolsMath.randomFloat(0f, ToolsAndroid.getScreenW().toFloat())
            targetY = ToolsMath.randomFloat(0f, ToolsAndroid.getScreenH().toFloat())
            axeX = targetX - x
            axeY = targetY - y
        }

        override fun update(delta: Float) {
            if (isDone()) return

            val changeX = ToolsMath.changeX(x, y, targetX, targetY)
            val changeY = ToolsMath.changeY(x, y, targetX, targetY)

            x += changeX * speed * delta
            y += changeY * speed * delta
        }

        override fun isDone(): Boolean {
            val doneX = axeX == 0f || (axeX > 0 && x >= targetX) || (axeX < 0 && x <= targetX)
            val doneY = axeY == 0f || (axeY > 0 && y >= targetY) || (axeY < 0 && y <= targetY)
            return doneX && doneY
        }

    }

    inner class ActionWaite : Action() {

        private var waitTime = 0f

        init {
            waitTime = ToolsMath.randomFloat(0.5f, 5f)
        }

        override fun update(delta: Float) {
            waitTime -= delta
        }

        override fun isDone() = waitTime <= 0f

        override fun nextAction(): Action {
            val r = ToolsMath.randomInt(1, 100)
            return when {
                r < 5 -> ActionCrazy()
                r < 15 -> ActionMove(SPEED * 3)
                r < 25 -> ActionRotations()
                r < 35 -> ActionSleep()
                else -> return ActionMove(SPEED)
            }
        }
    }

    inner class ActionRotations : Action() {

        private var counter = ToolsMath.randomInt(5, 10)
        private var time = 0.5f

        override fun update(delta: Float) {
            time -= delta
            if (time <= 0) {
                time = ToolsMath.randomFloat(0.3f, 1f)
                counter--
                axeX = -axeX
            }
        }

        override fun isDone() = counter <= 0
    }

    inner class ActionSleep : Action() {

        private var time = ToolsMath.randomFloat(5f, 60f)
        private var zTime = 0f
        private var list = ArrayList<ZZZ>()

        override fun update(delta: Float) {
            time -= delta
            zTime -= delta
            if (zTime <= 0) {
                zTime = 0.5f
                list.add(ZZZ())
            }
            if (list.isNotEmpty() && list[list.size - 1].alpha <= 0) list.removeAt(list.size - 1)
            for (i in list) i.update(delta)
        }

        override fun draw(canvas: Canvas) {
            eyeClosedForce = true
            eyeClosed = true
            axeX = -1f
            for (i in list) i.draw(canvas)
            super.draw(canvas)
        }

        override fun onFinish() {
            eyeClosedForce = false
            eyeClosed = false
        }

        override fun isDone() = time <= 0

        inner class ZZZ {

            var alpha = 255f
            var yy = y
            var xx = ToolsMath.randomFloat(x - DP * 10, x + DP * 10)

            fun update(delta: Float) {
                alpha -= 100 * delta
                yy -= DP * 10 * delta
            }

            fun draw(canvas: Canvas) {
                if (alpha <= 0) return
                paint.textSize = DP * 20
                paint.color = ToolsColor.setAlpha(alpha.toInt(), Color.WHITE)
                canvas.drawText("Z", xx, yy, paint)
            }

        }

    }

    inner class ActionCrazy : Action() {

        private var time = 8f
        private val speed = SPEED * 4
        private var targetX = x
        private var targetY = y

        init {
            vibrate()
            resetTarget()
        }

        @SuppressLint("MissingPermission")
        fun vibrate() {
            ToolsVibration.vibratePatternWithSleeps(200L, 200L, 200L, 200L, 200L, 200L, 200L, 200L)
        }

        fun resetTarget() {
            targetX = x + ToolsMath.randomFloat(-DP * 100, DP * 100)
            targetY = y + ToolsMath.randomFloat(-DP * 100, DP * 100)

            if (targetX < 0f) targetX = 0f
            if (targetX > ToolsAndroid.getScreenW()) targetX = ToolsAndroid.getScreenW().toFloat()
            if (targetY < 0f) targetY = 0f
            if (targetY > ToolsAndroid.getScreenH()) targetY = ToolsAndroid.getScreenH().toFloat()

            axeX = targetX - x
            axeY = targetY - y
        }

        override fun update(delta: Float) {
            if (isDone()) return
            time -= delta

            val changeX = ToolsMath.changeX(x, y, targetX, targetY)
            val changeY = ToolsMath.changeY(x, y, targetX, targetY)

            x += changeX * speed * delta
            y += changeY * speed * delta

            val doneX = axeX == 0f || (axeX > 0 && x >= targetX) || (axeX < 0 && x <= targetX)
            val doneY = axeY == 0f || (axeY > 0 && y >= targetY) || (axeY < 0 && y <= targetY)
            if (doneX && doneY) {
                resetTarget()
            }
        }

        override fun isDone(): Boolean {
            return time <= 0f
        }
    }

}