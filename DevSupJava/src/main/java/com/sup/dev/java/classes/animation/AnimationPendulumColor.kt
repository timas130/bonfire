package com.sup.dev.java.classes.animation

import com.sup.dev.java.tools.ToolsColor

class AnimationPendulumColor(color: Int, color_1: Int, color_2: Int, speedMs: Long, animationType: AnimationPendulum.AnimationType) : Animation() {

    val a: AnimationPendulum = AnimationPendulum(ToolsColor.alpha(color).toFloat(), ToolsColor.alpha(color_1).toFloat(), ToolsColor.alpha(color_2).toFloat(), AnimationPendulum.SpeedType.TIME_MS, speedMs.toFloat(), animationType)
    val r: AnimationPendulum = AnimationPendulum(ToolsColor.red(color).toFloat(), ToolsColor.red(color_1).toFloat(), ToolsColor.red(color_2).toFloat(), AnimationPendulum.SpeedType.TIME_MS, speedMs.toFloat(), animationType)
    val g: AnimationPendulum = AnimationPendulum(ToolsColor.green(color).toFloat(), ToolsColor.green(color_1).toFloat(), ToolsColor.green(color_2).toFloat(), AnimationPendulum.SpeedType.TIME_MS, speedMs.toFloat(), animationType)
    val b: AnimationPendulum = AnimationPendulum(ToolsColor.blue(color).toFloat(), ToolsColor.blue(color_1).toFloat(), ToolsColor.blue(color_2).toFloat(), AnimationPendulum.SpeedType.TIME_MS, speedMs.toFloat(), animationType)
    private val animationGroup: AnimationGroup = AnimationGroup(a, r, g, b)

    val color: Int
        get() = ToolsColor.argb(a.value.toInt(), r.value.toInt(), g.value.toInt(), b.value.toInt())

    val isTo_1: Boolean
        get() = a.isTo_1

    val isTo_2: Boolean
        get() = a.isTo_2

    constructor(color_1: Int, color_2: Int, speedMs: Long, animationType: AnimationPendulum.AnimationType) : this(color_1, color_1, color_2, speedMs, animationType) {}

    override fun update() {
        animationGroup.update()
    }

    override fun isNeedUpdate(): Boolean {
        return animationGroup.isNeedUpdate()
    }

    fun set(color: Int) {
        a.set(ToolsColor.alpha(color).toFloat())
        r.set(ToolsColor.red(color).toFloat())
        g.set(ToolsColor.green(color).toFloat())
        b.set(ToolsColor.blue(color).toFloat())
    }

    fun set(color_1: Int, color_2: Int) {
        a.set(ToolsColor.alpha(color_1).toFloat(), ToolsColor.alpha(color_2).toFloat())
        r.set(ToolsColor.red(color_1).toFloat(), ToolsColor.red(color_2).toFloat())
        g.set(ToolsColor.green(color_1).toFloat(), ToolsColor.green(color_2).toFloat())
        b.set(ToolsColor.blue(color_1).toFloat(), ToolsColor.blue(color_2).toFloat())
    }

    fun toggle() {
        a.toggle()
        r.toggle()
        g.toggle()
        b.toggle()
    }

    //
    //  Getters
    //

    fun to_1() {
        a.to_1()
        r.to_1()
        g.to_1()
        b.to_1()
    }

    fun to_2() {
        a.to_2()
        r.to_2()
        g.to_2()
        b.to_2()
    }

}