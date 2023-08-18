package com.sayzen.campfiresdk.screens.other.minigame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.classes.animation.Delta
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

class ViewMiniGame @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onWinHuman = {}
    var onWinRobot = {}

    private var currentColor = ToolsResources.getColorAttr(R.attr.colorOnPrimary)


    private val PLAYER_ROBOT = 1
    private val PLAYER_HUMAN = 2
    private val DP = ToolsView.dpToPx(2)
    private val paint = Paint()
    private val cells = arrayOf<ViewIcon>(ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon), ToolsView.inflate(R.layout.z_icon))
    private var lastMoveIndex = -1
    private var winLine1 = -1
    private var winLine2 = -1
    private var gameCode = 0L
    private val animationDelta = Delta()

    init {
        setWillNotDraw(false)
        paint.color = currentColor
        for(i in cells) i. setFilter(currentColor)
        paint.strokeWidth = DP
        for (i in cells.indices) {
            val v = cells[i]
            addView(v)
            v.setImageDrawable(null)
            v.setPadding(ToolsView.dpToPx(4).toInt(), ToolsView.dpToPx(4).toInt(), ToolsView.dpToPx(4).toInt(), ToolsView.dpToPx(4).toInt())
            v.setOnClickListener {
                setCell(PLAYER_HUMAN, i)
                if (!checkWinFor(i)) pcTurn()
            }
        }
        pcTurn()
    }

    fun restart() {
        for (v in cells) {
            v.setImageDrawable(null)
            v.tag = null
        }
        lastMoveIndex = -1
        winLine1 = -1
        winLine2 = -1
        animationDelta.clear()
        gameCode = System.currentTimeMillis()
        pcTurn()
    }

    private fun setCell_X(index: Int) {
        if (cells[index].tag != null) return
        setCell(PLAYER_ROBOT, index)
    }

    private fun setCellWithCheck_X(index: Int): Boolean {
        setCell(PLAYER_ROBOT, index)
        return checkWinFor(index)
    }

    private fun setCell_O(index: Int) {
        setCell(PLAYER_HUMAN, index)
    }

    private fun setCell(player: Int, index: Int) {
        lastMoveIndex = index
        cells[index].tag = player
        cells[index].setImageDrawable(if (player == 1) ToolsResources.getDrawable(R.drawable.ic_clear_white_24dp) else ToolsResources.getDrawable(R.drawable.ic_panorama_fish_eye_white_24dp))
        cells[index].visibility = View.INVISIBLE
        cells[index].setFilter(currentColor)
        ToolsView.fromAlpha(cells[index], 600)
    }


    //
    //  Game
    //

    private fun humanTurn() {
        for (n in cells) n.isClickable = n.tag == null
    }

    private fun pcTurn() {
        val code = gameCode
        for (n in cells) n.isClickable = false
        ToolsThreads.main(600) {
            if (code != gameCode) return@main
            if (!checkPcWinMove()) {
                if (!checkPcDefMove()) {
                    pcMove()
                    checkWinFor(lastMoveIndex)
                }
            }
            if (winLine1 == -1) {
                val empty = getEmpty()
                if (empty.isEmpty()) {
                    ToolsThreads.main(2000) { if (code == gameCode) restart() }
                } else {
                    humanTurn()
                }
            }
        }
    }

    private fun checkPcWinMove(): Boolean {
        val pp = PLAYER_ROBOT
        val last = lastMoveIndex

        //  0 Horizontal
        if (last == lastMoveIndex && plr(0) == pp && plr(1) == pp && plr(2) == -1) setCell_X(2)
        if (last == lastMoveIndex && plr(0) == pp && plr(1) == -1 && plr(2) == pp) setCell_X(1)
        if (last == lastMoveIndex && plr(0) == -1 && plr(1) == pp && plr(2) == pp) setCell_X(0)
        //  0 Vertical
        if (last == lastMoveIndex && plr(0) == pp && plr(3) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(0) == pp && plr(3) == -1 && plr(6) == pp) setCell_X(3)
        if (last == lastMoveIndex && plr(0) == -1 && plr(3) == pp && plr(6) == pp) setCell_X(0)
        //  8 Horizontal
        if (last == lastMoveIndex && plr(8) == pp && plr(5) == pp && plr(2) == -1) setCell_X(2)
        if (last == lastMoveIndex && plr(8) == pp && plr(5) == -1 && plr(2) == pp) setCell_X(5)
        if (last == lastMoveIndex && plr(8) == -1 && plr(5) == pp && plr(2) == pp) setCell_X(8)
        //  8 Vertical
        if (last == lastMoveIndex && plr(8) == pp && plr(7) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(8) == pp && plr(7) == -1 && plr(6) == pp) setCell_X(7)
        if (last == lastMoveIndex && plr(8) == -1 && plr(7) == pp && plr(6) == pp) setCell_X(8)

        if (last == lastMoveIndex && plr(0) == pp && plr(4) == pp && plr(8) == -1) setCell_X(8)
        if (last == lastMoveIndex && plr(0) == pp && plr(4) == -1 && plr(8) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(0) == -1 && plr(4) == pp && plr(8) == pp) setCell_X(0)

        if (last == lastMoveIndex && plr(2) == pp && plr(4) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(2) == pp && plr(4) == -1 && plr(6) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(2) == -1 && plr(4) == pp && plr(6) == pp) setCell_X(2)

        if (last == lastMoveIndex && plr(1) == pp && plr(4) == pp && plr(7) == -1) setCell_X(7)
        if (last == lastMoveIndex && plr(1) == pp && plr(4) == -1 && plr(7) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(1) == -1 && plr(4) == pp && plr(7) == pp) setCell_X(1)

        if (last == lastMoveIndex && plr(3) == pp && plr(4) == pp && plr(5) == -1) setCell_X(5)
        if (last == lastMoveIndex && plr(3) == pp && plr(4) == -1 && plr(5) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(3) == -1 && plr(4) == pp && plr(5) == pp) setCell_X(3)

        if (last != lastMoveIndex) {
            checkWinFor(lastMoveIndex)
            return true
        } else {
            return false
        }
    }

    private fun checkPcDefMove(): Boolean {
        val pp = PLAYER_HUMAN
        val last = lastMoveIndex
        //  0 Horizontal
        if (last == lastMoveIndex && plr(0) == pp && plr(1) == pp && plr(2) == -1) setCell_X(2)
        if (last == lastMoveIndex && plr(0) == pp && plr(1) == -1 && plr(2) == pp) setCell_X(1)
        if (last == lastMoveIndex && plr(0) == -1 && plr(1) == pp && plr(2) == pp) setCell_X(0)
        //  0 Vertical
        if (last == lastMoveIndex && plr(0) == pp && plr(3) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(0) == pp && plr(3) == -1 && plr(6) == pp) setCell_X(3)
        if (last == lastMoveIndex && plr(0) == -1 && plr(3) == pp && plr(6) == pp) setCell_X(0)
        //  8 Horizontal
        if (last == lastMoveIndex && plr(8) == pp && plr(5) == pp && plr(2) == -1) setCell_X(2)
        if (last == lastMoveIndex && plr(8) == pp && plr(5) == -1 && plr(2) == pp) setCell_X(5)
        if (last == lastMoveIndex && plr(8) == -1 && plr(5) == pp && plr(2) == pp) setCell_X(8)
        //  8 Vertical
        if (last == lastMoveIndex && plr(8) == pp && plr(7) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(8) == pp && plr(7) == -1 && plr(6) == pp) setCell_X(7)
        if (last == lastMoveIndex && plr(8) == -1 && plr(7) == pp && plr(6) == pp) setCell_X(8)

        if (last == lastMoveIndex && plr(0) == pp && plr(4) == pp && plr(8) == -1) setCell_X(8)
        if (last == lastMoveIndex && plr(0) == pp && plr(4) == -1 && plr(8) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(0) == -1 && plr(4) == pp && plr(8) == pp) setCell_X(0)

        if (last == lastMoveIndex && plr(2) == pp && plr(4) == pp && plr(6) == -1) setCell_X(6)
        if (last == lastMoveIndex && plr(2) == pp && plr(4) == -1 && plr(6) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(2) == -1 && plr(4) == pp && plr(6) == pp) setCell_X(2)

        if (last == lastMoveIndex && plr(1) == pp && plr(4) == pp && plr(7) == -1) setCell_X(7)
        if (last == lastMoveIndex && plr(1) == pp && plr(4) == -1 && plr(7) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(1) == -1 && plr(4) == pp && plr(7) == pp) setCell_X(1)

        if (last == lastMoveIndex && plr(3) == pp && plr(4) == pp && plr(5) == -1) setCell_X(5)
        if (last == lastMoveIndex && plr(3) == pp && plr(4) == -1 && plr(5) == pp) setCell_X(4)
        if (last == lastMoveIndex && plr(3) == -1 && plr(4) == pp && plr(5) == pp) setCell_X(3)

        if (last != lastMoveIndex) {
            checkWinFor(lastMoveIndex)
            return true
        } else {
            return false
        }
    }

    private fun checkWinFor(index: Int): Boolean {
        val p = plr(index)
        var win = false

        if ((index == 0 || index == 1 || index == 2) && plr(0) == p && plr(1) == p && plr(2) == p) {; winLine1 = 0; winLine2 = 2;win = true; }
        if ((index == 0 || index == 3 || index == 6) && plr(0) == p && plr(3) == p && plr(6) == p) {; winLine1 = 0; winLine2 = 6;win = true; }
        if ((index == 8 || index == 5 || index == 2) && plr(8) == p && plr(5) == p && plr(2) == p) {; winLine1 = 2; winLine2 = 8;win = true; }
        if ((index == 8 || index == 7 || index == 6) && plr(8) == p && plr(7) == p && plr(6) == p) {; winLine1 = 6; winLine2 = 8;win = true; }
        if ((index == 0 || index == 8 || index == 4) && plr(0) == p && plr(4) == p && plr(8) == p) {; winLine1 = 0; winLine2 = 8;win = true; }
        if ((index == 2 || index == 6 || index == 4) && plr(2) == p && plr(6) == p && plr(4) == p) {; winLine1 = 2; winLine2 = 6;win = true; }
        if ((index == 1 || index == 7 || index == 4) && plr(1) == p && plr(7) == p && plr(4) == p) {; winLine1 = 1; winLine2 = 7;win = true; }
        if ((index == 3 || index == 5 || index == 4) && plr(3) == p && plr(5) == p && plr(4) == p) {; winLine1 = 3; winLine2 = 5;win = true; }

        if (win) {
            for (n in cells) n.isClickable = false
            invalidate()
            val code = gameCode
            ToolsThreads.main(3000) {
                if (code != gameCode) return@main
                restart()
            }
            if(p == PLAYER_HUMAN) onWinHuman.invoke()
            if(p == PLAYER_ROBOT) onWinRobot.invoke()
        }

        return win
    }

    private fun pcMove() {
        val last = lastMoveIndex
        val pointsCount = getPointsCount()
        val angles = getAngles()
        val sides = getSides()
        val anglesHuman = getAngles(PLAYER_HUMAN)
        val anglesPc = getAngles(PLAYER_ROBOT)
        val outsideHLines = getOutsideHLines()

        if (pointsCount == 0) {
            setCell_X(ToolsCollections.random(arrayOf(0, 4, 4, 2, 6, 8)))
            if (last != lastMoveIndex) return
        }
        if (pointsCount == 2 && plr(4) == PLAYER_ROBOT) {
            if (angles.isEmpty()) {
                if (sides[0].index == 1) setCell_X(6)
                if (sides[0].index == 3) setCell_X(8)
                if (sides[0].index == 5) setCell_X(0)
                if (sides[0].index == 7) setCell_X(2)
            } else {
                if (angles[0].index == 0) setCell_X(2)
                if (angles[0].index == 2) setCell_X(8)
                if (angles[0].index == 6) setCell_X(0)
                if (angles[0].index == 8) setCell_X(6)
            }
            if (last != lastMoveIndex) return
        }

        if (pointsCount == 2 && angles.size == 1 && sides.size == 1) {
            if (last == lastMoveIndex && angles[0].index == 0 && sides[0].index == 1) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 0 && sides[0].index == 3) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 2 && sides[0].index == 1) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 2 && sides[0].index == 5) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 6 && sides[0].index == 7) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 6 && sides[0].index == 3) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 8 && sides[0].index == 7) setCell_X(4)
            if (last == lastMoveIndex && angles[0].index == 8 && sides[0].index == 5) setCell_X(4)

            if (last != lastMoveIndex) return
        }

        if (pointsCount == 2 && anglesHuman.size == 1 && anglesPc.size == 1) {
            if (last == lastMoveIndex && anglesPc[0].index == 0 && anglesHuman[0].index == 2) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 0 && anglesHuman[0].index == 6) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 2 && anglesHuman[0].index == 0) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 2 && anglesHuman[0].index == 8) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 6 && anglesHuman[0].index == 0) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 6 && anglesHuman[0].index == 8) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 8 && anglesHuman[0].index == 2) setCell_X(4)
            if (last == lastMoveIndex && anglesPc[0].index == 8 && anglesHuman[0].index == 6) setCell_X(4)

            if (last != lastMoveIndex) return
        }

        if (pointsCount == 4 && plr(4) == PLAYER_ROBOT && outsideHLines.size == 1) {
            if (last == lastMoveIndex && outsideHLines[0].index1 == 1 && outsideHLines[0].index2 == 2) setCell_X(0)
            if (last == lastMoveIndex && outsideHLines[0].index1 == 5 && outsideHLines[0].index2 == 8) setCell_X(2)
            if (last == lastMoveIndex && outsideHLines[0].index1 == 7 && outsideHLines[0].index2 == 6) setCell_X(8)
            if (last == lastMoveIndex && outsideHLines[0].index1 == 3 && outsideHLines[0].index2 == 0) setCell_X(6)

            if (last != lastMoveIndex) return
        }

        if (pointsCount == 6 && plr(4) == PLAYER_ROBOT && outsideHLines.isEmpty() && angles.size == 3) {
            if (last == lastMoveIndex && plr(2) == PLAYER_ROBOT) setCell_X(1)
            if (last == lastMoveIndex && plr(8) == PLAYER_ROBOT) setCell_X(5)
            if (last == lastMoveIndex && plr(6) == PLAYER_ROBOT) setCell_X(7)
            if (last == lastMoveIndex && plr(0) == PLAYER_ROBOT) setCell_X(3)

            if (last != lastMoveIndex) return
        }

        if (last != lastMoveIndex) return
        val empty = getEmpty()
        if (empty.isEmpty()) restart()
        else setCell_X(empty[ToolsMath.randomInt(0, empty.size - 1)].index)

    }


    //
    //  Support
    //

    private inner class Point(val index: Int) {
        val player: Int = plr(index)
    }

    private inner class HLine(val index1: Int, val index2: Int) {
        val player: Int = plr(index1)
    }

    private fun plr(index: Int) = if (cells[index].tag == null) -1 else cells[index].tag as Int

    private fun getAngles(): Array<Point> {
        val list = ArrayList<Point>()
        if (cells[0].tag != null) list.add(Point(0))
        if (cells[2].tag != null) list.add(Point(2))
        if (cells[6].tag != null) list.add(Point(6))
        if (cells[8].tag != null) list.add(Point(8))
        return list.toTypedArray()
    }

    private fun getEmpty(): Array<Point> {
        val list = ArrayList<Point>()
        for (i in cells.indices) if (cells[i].tag == null) list.add(Point(i))
        return list.toTypedArray()
    }

    private fun getAngles(player: Int): Array<Point> {
        val list = ArrayList<Point>()
        val angles = getAngles()
        for (i in angles) if (i.player == player) list.add(i)
        return list.toTypedArray()
    }

    private fun getPointsCount(): Int {
        var count = 0
        for (i in cells) if (i.tag != null) count++
        return count
    }

    private fun getSides(): Array<Point> {
        val list = ArrayList<Point>()
        if (cells[1].tag != null) list.add(Point(1))
        if (cells[3].tag != null) list.add(Point(3))
        if (cells[5].tag != null) list.add(Point(5))
        if (cells[7].tag != null) list.add(Point(7))
        return list.toTypedArray()
    }

    private fun getOutsideHLines(): Array<HLine> {
        val list = ArrayList<HLine>()
        if (cells[0].tag != null && cells[1].tag == cells[0].tag) list.add(HLine(0, 1))
        if (cells[1].tag != null && cells[2].tag == cells[1].tag) list.add(HLine(1, 2))
        if (cells[0].tag != null && cells[3].tag == cells[0].tag) list.add(HLine(0, 3))
        if (cells[3].tag != null && cells[6].tag == cells[3].tag) list.add(HLine(3, 6))
        if (cells[7].tag != null && cells[8].tag == cells[7].tag) list.add(HLine(7, 8))
        if (cells[6].tag != null && cells[7].tag == cells[6].tag) list.add(HLine(6, 7))
        if (cells[2].tag != null && cells[5].tag == cells[2].tag) list.add(HLine(2, 5))
        if (cells[5].tag != null && cells[8].tag == cells[5].tag) list.add(HLine(5, 8))
        return list.toTypedArray()
    }

    //
    //  Drawing
    //

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
        canvas.drawLine(width.toFloat(), height.toFloat(), width.toFloat(), 0f, paint)
        canvas.drawLine(width.toFloat(), height.toFloat(), 0f, height.toFloat(), paint)

        canvas.drawLine(width / 3f, 0f, width / 3f, height.toFloat(), paint)
        canvas.drawLine(width / 3f * 2, 0f, width / 3f * 2, height.toFloat(), paint)

        canvas.drawLine(0f, height / 3f, width.toFloat(), height / 3f, paint)
        canvas.drawLine(0f, height / 3f * 2, width.toFloat(), height / 3f * 2, paint)

        if (winLine1 != -1 && winLine2 != -1) {
            val deltaMs = animationDelta.totalTimeMs() / 2f

            if (winLine1 == 0 && winLine2 == 2) {
                canvas.drawLine(0f, height / 6f, width * deltaMs, height / 6f, paint)
                canvas.drawLine(width.toFloat(), height / 6f, width - (width * deltaMs), height / 6f, paint)
            }

            if (winLine1 == 6 && winLine2 == 8) {
                canvas.drawLine(0f, height / 6f * 5, width * deltaMs, height / 6f * 5, paint)
                canvas.drawLine(width.toFloat(), height / 6f * 5, width - (width * deltaMs), height / 6f * 5, paint)
            }

            if (winLine1 == 0 && winLine2 == 6) {
                canvas.drawLine(width / 6f, 0f, width / 6f, height.toFloat() * deltaMs, paint)
                canvas.drawLine(width / 6f, height.toFloat(), width / 6f, height.toFloat() - (height.toFloat() * deltaMs), paint)
            }

            if (winLine1 == 2 && winLine2 == 8) {
                canvas.drawLine(width / 6f * 5, 0f, width / 6f * 5, height.toFloat() * deltaMs, paint)
                canvas.drawLine(width / 6f * 5, height.toFloat(), width / 6f * 5, height.toFloat() - (height.toFloat() * deltaMs), paint)
            }

            if (winLine1 == 0 && winLine2 == 8) {
                canvas.drawLine(0f, 0f, width.toFloat() * deltaMs, height.toFloat() * deltaMs, paint)
                canvas.drawLine(width.toFloat(), height.toFloat(), width.toFloat() - (width.toFloat() * deltaMs), height.toFloat() - (height.toFloat() * deltaMs), paint)
            }

            if (winLine1 == 2 && winLine2 == 6) {
                canvas.drawLine(width.toFloat(), 0f, width.toFloat() - (width.toFloat() * deltaMs), height.toFloat() * deltaMs, paint)
                canvas.drawLine(0f, height.toFloat(), width.toFloat() * deltaMs, height.toFloat() - (height.toFloat() * deltaMs), paint)
            }

            if (winLine1 == 3 && winLine2 == 5) {
                canvas.drawLine(0f, height / 2f, width.toFloat() * deltaMs, height / 2f, paint)
                canvas.drawLine(width.toFloat(), height / 2f, width.toFloat() - (width.toFloat() * deltaMs), height / 2f, paint)
            }

            if (winLine1 == 1 && winLine2 == 7) {
                canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat() * deltaMs, paint)
                canvas.drawLine(width / 2f, height.toFloat(), width / 2f, height.toFloat() - (height.toFloat() * deltaMs), paint)
            }
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        (cells[1].layoutParams as LayoutParams).leftMargin = width / 3
        (cells[2].layoutParams as LayoutParams).leftMargin = width / 3 * 2

        (cells[3].layoutParams as LayoutParams).topMargin = height / 3
        (cells[4].layoutParams as LayoutParams).topMargin = height / 3
        (cells[4].layoutParams as LayoutParams).leftMargin = width / 3
        (cells[5].layoutParams as LayoutParams).topMargin = height / 3
        (cells[5].layoutParams as LayoutParams).leftMargin = width / 3 * 2

        (cells[6].layoutParams as LayoutParams).topMargin = height / 3 * 2
        (cells[7].layoutParams as LayoutParams).topMargin = height / 3 * 2
        (cells[7].layoutParams as LayoutParams).leftMargin = width / 3
        (cells[8].layoutParams as LayoutParams).topMargin = height / 3 * 2
        (cells[8].layoutParams as LayoutParams).leftMargin = width / 3 * 2

        for (i in cells) {
            if (i.layoutParams.width == width / 3) continue
            i.layoutParams.width = width / 3
            i.layoutParams.height = height / 3
            i.requestLayout()
        }

    }

}
