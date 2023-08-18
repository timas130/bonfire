package com.sup.dev.android.libs.screens.navigator

import com.sup.dev.android.libs.screens.Screen
import java.util.ArrayList

class NavigatorStack {

    var stack = ArrayList<Screen>()

    fun isEmpty() = stack.isEmpty()

    fun isNotEmpty() = stack.isNotEmpty()

    fun size() = stack.size

    fun clear(){
        stack.clear()
    }

    fun stackCopy() = ArrayList(stack)

}