package com.sup.dev.android.views.support.adapters.pager

class PagerCardInfinityAdapter : PagerCardAdapter() {

    companion object {
        var LOOPS_COUNT = 100000
    }

    val realCount: Int
        get() = super.getCount()

    override fun realPosition(position: Int): Int {
        return if(realCount == 0) 0 else position % realCount
    }

    override fun getCount(): Int {
        val realCount = realCount
        return if (realCount == 0) 0 else if (realCount == 1) 1 else realCount * LOOPS_COUNT
    }

    fun getCenter() = (count / 2) - realPosition(count / 2)
}

