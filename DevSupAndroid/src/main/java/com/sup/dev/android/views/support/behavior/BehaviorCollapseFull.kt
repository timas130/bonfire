package com.sup.dev.android.views.support.behavior

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.tools.ToolsThreads

open class BehaviorCollapseFull<V : View>(context: Context, attrs: AttributeSet) : androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<V>(context, attrs) {

    private var subscription: Subscription? = null
    private var target = 1f

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: V, dependency: View): Boolean {
        if (child.visibility != View.GONE) {
            if (target == 1f) {
                if (-dependency.y > child.height / 2f) {
                    target = 0f
                    startAnimation(child)
                }
            } else if (target == 0f) {
                if (-dependency.y < child.height / 2f) {
                    target = 1f
                    startAnimation(child)
                }
            }
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    private fun startAnimation(view: View) {
        if (subscription != null) subscription?.unsubscribe()
        val step = 20f
        val time = 200L
        subscription = ToolsThreads.timerMain(step.toLong(), time, {
            view.scaleX += (step / time) * (if(target == 1f) 1f else -1f)
            if (target == 1f && view.scaleX > target) view.scaleX = target
            if (target == 0f && view.scaleX < target) view.scaleX = target
            view.scaleY = view.scaleX
            view.visibility = if (view.scaleX == 0f) View.INVISIBLE else View.VISIBLE
            view.invalidate()
        }, {
            view.scaleX = target
            view.scaleY = view.scaleX
            view.visibility = if (view.scaleX == 0f) View.INVISIBLE else View.VISIBLE
            view.invalidate()
        })
    }

}