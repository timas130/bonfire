package com.sup.dev.android.views.support.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.sup.dev.android.models.EventConfigurationChanged
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.libs.eventBus.EventBus

class BehaviorAppBarLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :  AppBarLayout.Behavior(context, attrs){

    private var scrollableRecyclerView = false
    private var appBarLayout:AppBarLayout? = null
    private var lastScreenOrientation = ToolsAndroid.getScreenOrientation()
    private var eventBus = EventBus.subscribe(EventConfigurationChanged::class){
        val p = ToolsAndroid.getScreenOrientation()
        if(lastScreenOrientation != p){
            lastScreenOrientation = p
            appBarLayout?.setExpanded(true)
        }
    }


    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
        return scrollableRecyclerView &&  super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        updatedScrollable(parent, directTargetChild, child)
        return scrollableRecyclerView && super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return scrollableRecyclerView && super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    private fun updatedScrollable(parent: CoordinatorLayout, directTargetChild: View, appBar: AppBarLayout) {
        this.appBarLayout = appBar

        if(appBar.top < 0){
            scrollableRecyclerView = true
            return
        }

        val recyclerView = findRecyclerView(directTargetChild)
        if (recyclerView == null) {
            scrollableRecyclerView = true
            return
        }
        val adapter = recyclerView.adapter
        val layoutManager = recyclerView.layoutManager
        if (adapter == null || layoutManager == null || appbarContainsCollapse(appBar)) {
            scrollableRecyclerView = true
            return
        }

        var lastVisibleItem = -1
        if (layoutManager is LinearLayoutManager) {
            lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val lastItems = layoutManager.findLastCompletelyVisibleItemPositions(IntArray(layoutManager.spanCount))
            lastVisibleItem = Math.abs(lastItems[lastItems.size - 1])
        }

        if(lastVisibleItem < 0){
            appBarLayout?.setExpanded(true)
            scrollableRecyclerView = false
            return
        }

        val v = layoutManager.findViewByPosition(lastVisibleItem)

        if(v == null){
            scrollableRecyclerView = true
            return
        }

        scrollableRecyclerView = lastVisibleItem <  adapter.itemCount - 1 || (ToolsView.viewPointAsScreenPoint(v, 0f, 0f)[1] + v.height) > (ToolsView.viewPointAsScreenPoint(parent, 0f, 0f)[1] + parent.height)
    }

    private fun appbarContainsCollapse(appBar: AppBarLayout):Boolean{
        for(i in 0 until appBar.childCount) if(appBar.getChildAt(i) is CollapsingToolbarLayout) return true
        return false
    }

    private fun findRecyclerView(view: View):RecyclerView? {
        if (view is RecyclerView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount)
                if (view.getChildAt(i) is RecyclerView)
                    return view.getChildAt(i) as RecyclerView
        }
        return null
    }

}