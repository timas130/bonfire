package com.sup.dev.android.views.support.behavior

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View


class BehaviorAppBarCollapse<V : View>(context: Context, attrs: AttributeSet) : BehaviorCollapseSmooth<V>(context, attrs) {

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        child.y = dependency.y + dependency.height - child.height / 2
        return super.onDependentViewChanged(parent, child, dependency)
    }
}