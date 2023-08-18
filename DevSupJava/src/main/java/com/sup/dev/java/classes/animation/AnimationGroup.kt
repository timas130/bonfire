package com.sup.dev.java.classes.animation

import java.util.*

class AnimationGroup(vararg animations: Animation) : Animation() {

    private val animations = ArrayList<Animation>()

    init {
        this.animations.addAll(Arrays.asList(*animations))
    }

    override fun update() {
        for (animation in animations)
            animation.update()
    }

    override fun isNeedUpdate(): Boolean {
        for (animation in animations)
            if (animation.isNeedUpdate())
                return true
        return false
    }
}
