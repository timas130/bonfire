package com.sup.dev.android.views.support.behavior

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

open class BehaviorCollapseSmooth<V : View>(context: Context, attrs: AttributeSet) : androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<V>(context, attrs) {

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: V, dependency: View): Boolean {
        if(child.visibility != View.GONE) {
            child.scaleX = Math.max(1 - -dependency.y / child.height, 0f)
            child.scaleY = Math.max(1 - -dependency.y / child.height, 0f)
            child.visibility = if (child.scaleX == 0f) View.INVISIBLE else View.VISIBLE
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }
}