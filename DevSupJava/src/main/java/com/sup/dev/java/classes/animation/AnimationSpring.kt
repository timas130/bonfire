package com.sup.dev.java.classes.animation

class AnimationSpring(value: Float, speedType: SpeedType, speed: Float) : Animation() {

    private val delta = Delta()

    private var speedType: SpeedType? = null
    private var speed: Float = 0.toFloat()

    var value: Float = 0.toFloat()
        private set

    var to: Float = 0.toFloat()
        private set
    private var step: Float = 0.toFloat()

    enum class SpeedType {
        POINT_IN_SEC, TIME_MS
    }

    init {
        this.value = value
        setSpeed(speedType, speed)
    }

    override fun update() {

        if (step == 0f) return

        value += step * delta.deltaMs()

        if (step > 0 && value >= to || step < 0 && value <= to) {
            step = 0f
            value = to
        }
    }

    //
    //  Setters
    //

    fun set(to: Float) {
        this.to = to
        this.value = to
        step = 0f
    }

    fun to(to: Float) {
        this.to = to

        if (speedType == SpeedType.TIME_MS)
            step = (to - value) / speed
        else
            step = speed / 1000 * if (to - value < 0) -1 else 1
        delta.clear()
        update()
    }


    fun setSpeed(speedType: SpeedType, speed: Float) {
        var speedS = speed
        if (speedType == SpeedType.TIME_MS && speedS < 1)
            speedS = 1f

        this.speed = speedS
        this.speedType = speedType
        update()
    }


    //
    //  Getters
    //

    override fun isNeedUpdate(): Boolean {
        return step != 0f
    }

}
