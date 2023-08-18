package com.sup.dev.java.tools

import com.sup.dev.java.classes.Subscription

object ToolsThreads {

    var onMain: (Boolean, () -> Unit)-> Unit = {_, function -> run { thread(function) } }

    fun thread(runnable: () -> Unit) {
        Thread(runnable).start()
    }

    fun thread(sleep: Long, runnable: () -> Unit): Subscription {
        val subscription = Subscription()
        thread {
            sleep(sleep)
            if (subscription.isSubscribed())
                runnable.invoke()
        }
        return subscription
    }

    fun sleep(sleep: Long) {
        try {
            Thread.sleep(sleep)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun main(runnable: () -> Unit) {
        main(false, runnable)
    }


    fun main(onNextTime: Boolean, runnable: () -> Unit) {
        onMain.invoke(onNextTime, runnable)
    }

    fun main(sleep: Long, runnable: () -> Unit): Subscription {
        val subscription = Subscription()
        if(sleep <= 0){
            main(runnable)
        }else {
            thread {
                sleep(sleep)
                if (subscription.isSubscribed())
                    main(runnable)
            }
        }
        return subscription
    }

    fun timerThread(stepTime: Long, onStep: (Subscription) -> Unit): Subscription {
        return timerThread(stepTime, 0, onStep, null)
    }

    @JvmOverloads
    fun timerThread(stepTime: Long, time: Long, onStep: (Subscription) -> Unit, onFinish: (() -> Unit)? = null): Subscription {
        val subscription = Subscription()
        val endTime = if (time > 0) System.currentTimeMillis() + time else 0
        thread {
            onStep.invoke(subscription)
            while (true) {
                sleep(stepTime)
                if (!subscription.isSubscribed()) break
                if (endTime != 0L && System.currentTimeMillis() >= endTime) break
                onStep.invoke(subscription)
            }
            onFinish?.invoke()
        }
        return subscription
    }

    fun timerMain(stepTime: Long, onStep: (Subscription) -> Unit): Subscription {
        return timerMain(stepTime, 0, onStep, null)
    }

    @JvmOverloads
    fun timerMain(stepTime: Long, time: Long, onStep: (Subscription) -> Unit, onFinish: (() -> Unit)? = null): Subscription {
        val subscription = Subscription()
        val endTime = if (time > 0) System.currentTimeMillis() + time else 0
        thread {
            main { onStep.invoke(subscription) }
            while (true) {
                sleep(stepTime)
                if (!subscription.isSubscribed()) break
                if (endTime != 0L && System.currentTimeMillis() >= endTime) break
                main { onStep.invoke(subscription) }
            }
            if (onFinish != null) main { onFinish.invoke() }
        }
        return subscription
    }

}
