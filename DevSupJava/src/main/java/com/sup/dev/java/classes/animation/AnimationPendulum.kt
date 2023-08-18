package com.sup.dev.java.classes.animation

class AnimationPendulum(value: Float, to_1: Float, to_2: Float, var speedType: SpeedType, speed: Float, private val animationType: AnimationType) : Animation() {

    private val delta = Delta()
    private var speed: Float = speed

    var value: Float = 0.toFloat()
        private set

    var to: Float = 0.toFloat()
        private set
    var to_1: Float = 0.toFloat()
        private set
    var to_2: Float = 0.toFloat()
        private set
    private var step: Float = 0.toFloat()

    val isTo_1: Boolean
        get() = to == to_1

    val isTo_2: Boolean
        get() = to == to_2


    enum class SpeedType {
        POINT_IN_SEC, TIME_MS
    }

    enum class AnimationType {
        INFINITY, SWITCH, TO_2_AND_BACK
    }


    init {
        this.value = value
        this.to_1 = to_1
        this.to_2 = to_2

        setSpeed(speedType, speed)
        if (animationType == AnimationType.INFINITY) {
            if (value == to_2)
                to_1()
            else
                to_2()
        }
    }

    //
    //  Methods
    //

    override fun update() {

        if (step == 0f) return

        value += step * delta.deltaMs()

        if (step > 0 && value >= to || step < 0 && value <= to) {

            if (animationType == AnimationType.INFINITY || animationType == AnimationType.TO_2_AND_BACK && isTo_2) {
                value += to - value
                toggle()
            } else {
                step = 0f
                value = to
            }

        }
    }

    //
    //  Setters
    //

    fun set(value: Float) {
        this.value = value
        if (isTo_1)
            to_1()
        else
            to_2()
    }

    fun set(to_1: Float, to_2: Float) {
        val b = isTo_1

        this.to_1 = to_1
        this.to_2 = to_2

        if (b)
            to_1()
        else
            to_2()
    }

    fun toggle() {
        if (isTo_1)
            to_2()
        else
            to_1()
    }

    fun to_1() {
        to(to_1)
    }

    fun to_2() {
        to(to_2)
    }

    private fun to(to: Float) {

        this.to = to

        if (to_1 == to_2) return

        if (speedType == SpeedType.TIME_MS)
            step = (to_1 - to_2) / speed
        else
            step = speed / 1000 * if (to_1 - to_2 < 0) -1 else 1
        if (to < value && step > 0) step *= -1f
        if (to > value && step < 0) step *= -1f

        delta.clear()
        update()

        if (step == 0f && (animationType == AnimationType.INFINITY || animationType == AnimationType.TO_2_AND_BACK && isTo_2))
            toggle()
    }


    fun setSpeed(speed: Float) {
        setSpeed(speedType, speed)
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

    fun getSpeed() = speed


}
