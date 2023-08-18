package com.sup.dev.android.views.support.behavior

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class BehaviorBottomSheet<V : View> : BottomSheetBehavior<V> {

    private var canCollapse = false

    constructor() : super() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return canCollapse && super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return canCollapse && super.onTouchEvent(parent, child, event)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return canCollapse && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    @Suppress("DEPRECATION")
    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (canCollapse) super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed)
    }

    @Suppress("DEPRECATION")
    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View) {
        if (canCollapse) super.onStopNestedScroll(coordinatorLayout, child, target)
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        return canCollapse && super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    //
    //  Setters
    //

    fun setCanColapse(canCollapse: Boolean) {
        this.canCollapse = canCollapse
    }
}


