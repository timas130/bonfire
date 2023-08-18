package com.sup.dev.java_pc.views.views

import java.awt.*
import java.awt.event.MouseWheelEvent
import javax.swing.JPopupMenu
import javax.swing.JScrollBar


class ZPopup @JvmOverloads constructor(label: String? = null) : JPopupMenu(label) {

    var maximumVisibleRows = 10

    private var popupScrollBar: JScrollBar? = null

    protected val scrollBar: JScrollBar
        get() {
            if (popupScrollBar == null) {
                popupScrollBar = JScrollBar(JScrollBar.VERTICAL)
                popupScrollBar!!.addAdjustmentListener {
                    doLayout()
                    repaint()
                }

                popupScrollBar!!.isVisible = false
            }

            return popupScrollBar as JScrollBar
        }


    init {
        layout = ScrollPopupMenuLayout()

        super.add(scrollBar)
        addMouseWheelListener { event ->
            val scrollBar = scrollBar
            val amount = if (event.scrollType == MouseWheelEvent.WHEEL_UNIT_SCROLL)
                event.unitsToScroll * scrollBar.unitIncrement
            else
                (if (event.wheelRotation < 0) -1 else 1) * scrollBar.blockIncrement

            scrollBar.value = scrollBar.value + amount
            event.consume()
        }
    }

    public override fun paintChildren(g: Graphics) {
        val insets = insets
        g.clipRect(insets.left, insets.top, width, height - insets.top - insets.bottom)
        super.paintChildren(g)
    }

    override fun addImpl(comp: Component, constraints: Any, index: Int) {
        super.addImpl(comp, constraints, index)

        if (maximumVisibleRows < componentCount - 1) {
            scrollBar.isVisible = true
        }
    }

    override fun remove(index: Int) {
        var indexV = index
        // can't remove the scrollbar
        ++indexV

        super.remove(indexV)

        if (maximumVisibleRows >= componentCount - 1) {
            scrollBar.isVisible = false
        }
    }

    override fun show(invoker: Component, x: Int, y: Int) {
        val scrollBar = scrollBar
        if (scrollBar.isVisible) {
            var extent = 0
            var max = 0
            var i = 0
            var un = -1
            var width = 0
            for (comp in components) {
                if (comp !is JScrollBar) {
                    val preferredSize = comp.preferredSize
                    width = Math.max(width, preferredSize.width)
                    if (un < 0) {
                        un = preferredSize.height
                    }
                    if (i++ < maximumVisibleRows) {
                        extent += preferredSize.height
                    }
                    max += preferredSize.height
                }
            }

            val insets = insets
            val widthMargin = insets.left + insets.right
            val heightMargin = insets.top + insets.bottom
            scrollBar.unitIncrement = un
            scrollBar.blockIncrement = extent
            scrollBar.setValues(0, heightMargin + extent, 0, heightMargin + max)

            width += scrollBar.preferredSize.width + widthMargin
            val height = heightMargin + extent

            setPopupSize(Dimension(width, height))
        }

        super.show(invoker, x, y)
    }

    protected class ScrollPopupMenuLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}

        override fun preferredLayoutSize(parent: Container): Dimension {
            var visibleAmount = Integer.MAX_VALUE
            val dim = Dimension()
            for (comp in parent.components) {
                if (comp.isVisible) {
                    if (comp is JScrollBar) {
                        visibleAmount = comp.visibleAmount
                    } else {
                        val pref = comp.preferredSize
                        dim.width = Math.min(Math.max(dim.width, pref.width), 1024)
                        dim.height += pref.height
                    }
                }
            }

            val insets = parent.insets
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount)

            return dim
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            var visibleAmount = Integer.MAX_VALUE
            val dim = Dimension()
            for (comp in parent.components) {
                if (comp.isVisible) {
                    if (comp is JScrollBar) {
                        visibleAmount = comp.visibleAmount
                    } else {
                        val min = comp.minimumSize
                        dim.width = Math.max(dim.width, min.width)
                        dim.height += min.height
                    }
                }
            }

            val insets = parent.insets
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount)

            return dim
        }

        override fun layoutContainer(parent: Container) {
            val insets = parent.insets

            var width = parent.width - insets.left - insets.right
            val height = parent.height - insets.top - insets.bottom

            val x = insets.left
            var y = insets.top
            var position = 0

            for (comp in parent.components) {
                if (comp is JScrollBar && comp.isVisible()) {
                    val dim = comp.preferredSize
                    comp.setBounds(x + width - dim.width, y, dim.width, height)
                    width -= dim.width
                    position = comp.value
                }
            }

            y -= position
            for (comp in parent.components) {
                if (comp !is JScrollBar && comp.isVisible) {
                    val pref = comp.preferredSize
                    comp.setBounds(x, y, width, pref.height)
                    y += pref.height
                }
            }
        }
    }

}
