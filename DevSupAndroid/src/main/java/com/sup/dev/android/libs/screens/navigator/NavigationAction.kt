package com.sup.dev.android.libs.screens.navigator

import com.sup.dev.android.libs.screens.Screen


class NavigationAction private constructor(private var action: (Screen) -> Unit = {}) {
    private var before: (Screen) -> Unit = {}
    private var after: (Screen) -> Unit = {}
    internal var immutable: Boolean = false

    fun doAction(screen: Screen) {
        before.invoke(screen)
        action.invoke(screen)
        after.invoke(screen)
    }

    fun action(action: (Screen) -> Unit): NavigationAction {
        if (immutable) throw RuntimeException("Can't change immutable NavigationAction")
        this.action = action
        return this
    }

    fun after(after: (Screen) -> Unit): NavigationAction {
        if (immutable) throw RuntimeException("Can't change immutable NavigationAction")
        this.after = after
        return this
    }

    fun before(before: (Screen) -> Unit): NavigationAction {
        if (immutable) throw RuntimeException("Can't change immutable NavigationAction")
        this.before = before
        return this
    }

    fun immutable(): NavigationAction {
        immutable = true
        return this
    }

    companion object {

        fun to(): NavigationAction {
            return NavigationAction { Navigator.to(it) }
        }

        fun set(): NavigationAction {
            return NavigationAction { Navigator.set(it) }
        }

        fun replace(): NavigationAction {
            return NavigationAction { Navigator.replace(it) }
        }

        fun reorder(): NavigationAction {
            return NavigationAction { Navigator.reorder(it) }
        }

        fun toBackStackOrNew(): NavigationAction {
            return NavigationAction { Navigator.toBackStackOrNew(it) }
        }
    }

}
