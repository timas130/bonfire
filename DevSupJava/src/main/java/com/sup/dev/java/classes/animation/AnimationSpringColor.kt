package com.sup.dev.java.classes.animation

import com.sup.dev.java.tools.ToolsColor

class AnimationSpringColor(color: Int, speedMs: Long) : Animation() {

    val a: AnimationSpring = AnimationSpring(ToolsColor.alpha(color).toFloat(), AnimationSpring.SpeedType.TIME_MS, speedMs.toFloat())
    val r: AnimationSpring = AnimationSpring(ToolsColor.red(color).toFloat(), AnimationSpring.SpeedType.TIME_MS, speedMs.toFloat())
    val g: AnimationSpring = AnimationSpring(ToolsColor.green(color).toFloat(), AnimationSpring.SpeedType.TIME_MS, speedMs.toFloat())
    val b: AnimationSpring = AnimationSpring(ToolsColor.blue(color).toFloat(), AnimationSpring.SpeedType.TIME_MS, speedMs.toFloat())
    private val animationGroup: AnimationGroup = AnimationGroup(a, r, g, b)
    private var to: Int = 0

    override fun update() {
        animationGroup.update()
    }

    override fun isNeedUpdate(): Boolean {
        return animationGroup.isNeedUpdate()
    }

    fun change(animated: Boolean, color: Int) {
        if (animated)
            to(color)
        else
            set(color)
    }

    fun set(color: Int) {
        this.to = color
        a.set(ToolsColor.alpha(color).toFloat())
        r.set(ToolsColor.red(color).toFloat())
        g.set(ToolsColor.green(color).toFloat())
        b.set(ToolsColor.blue(color).toFloat())
    }

    fun to(color: Int) {
        this.to = color
        a.to(ToolsColor.alpha(color).toFloat())
        r.to(ToolsColor.red(color).toFloat())
        g.to(ToolsColor.green(color).toFloat())
        b.to(ToolsColor.blue(color).toFloat())
    }

    fun getColor() = ToolsColor.argb(a.value.toInt(), r.value.toInt(), g.value.toInt(), b.value.toInt())


}
