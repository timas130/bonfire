package com.sup.dev.java.libs.visual_engine.objects

import com.sup.dev.java.libs.visual_engine.graphics.VeGraphics
import com.sup.dev.java.libs.visual_engine.models.Update
import com.sup.dev.java.libs.visual_engine.root.VeScene
import com.sup.dev.java.tools.ToolsMath

open class VisualObject {

    var parent: VisualObject? = null

    //  ----------------------------------------------
    //
    //
    //  Callbacks
    //
    //
    //  ----------------------------------------------

    open fun onUpdate(update: Update) {
        onUpdateChildren(update)
    }

    fun draw(g: VeGraphics) {
        drawSelf(g)
        drawChildren(g)
    }

    fun drawForeground(g: VeGraphics) {
        drawSelfForeground(g)
        drawChildrenForeground(g)
    }

    open fun drawSelf(g: VeGraphics) {

    }
    open fun drawSelfForeground(g: VeGraphics) {

    }

    open fun onAdd() {

    }

    open fun onRemove() {

    }

    open fun onBoundsChanged(){

    }
    open fun onSizeChanged(){

    }
    open fun onPositionChanged(){

    }

    open fun onChildAdd(vo:VisualObject){

    }

    //  ----------------------------------------------
    //
    //  
    //  Bounds
    //  
    //
    //  ----------------------------------------------

    private var x = 0f
    private var y = 0f
    private var w = 0f
    private var h = 0f

    fun set(x: Float, y: Float, w: Float, h: Float) {
        if (this.x == x && this.y == y && this.w == w && this.h == h) return
        this.x = x
        this.y = y
        this.w = w
        this.h = h
        onPositionChanged()
        onSizeChanged()
        onBoundsChanged()
    }

    fun setCenterPosition(x: Float, y: Float) {
        setPosition(x-w/2, y-h/2)
    }

    fun setPosition(x: Float, y: Float) {
        if (this.x == x && this.y == y) return
        this.x = x
        this.y = y
        onPositionChanged()
        onBoundsChanged()
    }

    fun setSize(w: Float, h: Float) {
        if (this.w == w && this.h == h) return
        this.w = w
        this.h = h
        onSizeChanged()
        onBoundsChanged()
    }

    fun setX(x: Float) {
        if (this.x == x) return
        this.x = x
        onPositionChanged()
        onBoundsChanged()
    }

    fun setY(y: Float) {
        if (this.y == y) return
        this.y = y
        onPositionChanged()
        onBoundsChanged()
    }

    fun setW(w: Float) {
        if (this.w == w) return
        this.w = w
        onSizeChanged()
        onBoundsChanged()
    }

    fun setH(h: Float) {
        if (this.h == h) return
        this.h = h
        onSizeChanged()
        onBoundsChanged()
    }

    fun calculateSizeByChildren() {
        var maxXW = 0f
        var maxYH = 0f

        for (c in lockChildren()) {
            if (c.x + c.w > maxXW) maxXW = c.x + c.w
            if (c.y + c.h > maxYH) maxYH = c.y + c.h
        }

        w = maxXW
        h = maxYH

    }

    fun getScreenX(): Float = parent?.getScreenX() ?: 0f + x
    fun getScreenY(): Float = parent?.getScreenY() ?: 0f + y

    fun collisionWith(xx: Float, yy: Float) = ToolsMath.collisionRectAndPoint(xx, yy, x, y, x + w, y + h)
    fun collisionWith(xx: Float, yy: Float, ww: Float, hh: Float) = ToolsMath.collisionOrContainRectAndRect(xx, yy, xx + ww, yy + hh, x, y, x + w, y + h)
    fun collisionWith(xx: Float, yy: Float, rr: Float) = ToolsMath.collisionRectAndCircle(x, y, x + w, y + h, xx, yy, rr)

    fun collisionWith_Screen(xx: Float, yy: Float): Boolean {
        val screenX = getScreenX()
        val screenY = getScreenY()
        return ToolsMath.collisionRectAndPoint(xx, yy, screenX, screenY, screenX + w, screenY + h)
    }

    fun collisionWith_Screen(xx: Float, yy: Float, ww: Float, hh: Float): Boolean {
        val screenX = getScreenX()
        val screenY = getScreenY()
        return ToolsMath.collisionOrContainRectAndRect(xx, yy, xx + ww, yy + hh, screenX, screenY, screenX + w, screenY + h)
    }

    fun collisionWith_Screen(xx: Float, yy: Float, rr: Float): Boolean {
        val screenX = getScreenX()
        val screenY = getScreenY()
        return ToolsMath.collisionRectAndCircle(getScreenY(), getScreenY(), screenX + w, screenY + h, xx, yy, rr)
    }

    fun getX() = x
    fun getY() = y
    fun getW() = w
    fun getH() = h


    //  ----------------------------------------------
    //
    //  
    //  Children
    //  
    //
    //  ----------------------------------------------

    enum class Operation {
        ADD, REMOVE
    }

    private var objects = ArrayList<VisualObject>()
    private var checkBoundAfterDraw = false

    fun removeFromParent(){
        parent?.remove(this)
    }

    fun onUpdateChildren(update: Update){
        val lockObjects = lockChildren()
        for (o in lockObjects) o.onUpdate(update)
    }

    fun drawChildren(g: VeGraphics){
        val lockObjects = lockChildren()
        for(o in lockObjects) {
            if (!checkBoundAfterDraw || o.collisionWith_Screen(VeScene.getScreenX(), VeScene.getScreenY(), VeScene.getScreenW(), VeScene.getScreenH())) {
                val offsetX = g.getOffsetX()
                val offsetY = g.getOffsetY()
                g.offset(o.getX(), o.getY())
                o.draw(g)
                g.setOffset(offsetX, offsetY)
            }
        }
    }

    fun drawChildrenForeground(g: VeGraphics){
        val lockObjects = lockChildren()
        for(o in lockObjects) {
            if (!checkBoundAfterDraw || o.collisionWith_Screen(VeScene.getScreenX(), VeScene.getScreenY(), VeScene.getScreenW(), VeScene.getScreenH())) {
                val offsetX = g.getOffsetX()
                val offsetY = g.getOffsetY()
                g.offset(o.getX(), o.getY())
                o.drawForeground(g)
                g.setOffset(offsetX, offsetY)
            }
        }
    }

    fun remove(visualObject: VisualObject) {
        synchronized(objects){ objects.remove(visualObject) }
        visualObject.onRemove()
    }

    fun removeAllChildren() {
        synchronized(objects){
            while (objects.isNotEmpty()){
                val v = objects.removeAt(0)
                v.onRemove()
            }
        }
    }

    fun add(visualObject: VisualObject) {
        visualObject.removeFromParent()
        synchronized(objects){ objects.add(visualObject) }
        visualObject.parent = this
        visualObject.onAdd()
    }

    fun lockChildren():ArrayList<VisualObject>{
        val list = ArrayList<VisualObject>()
        synchronized(objects){
            list.addAll(objects)
        }
        return list
    }

    fun setCheckBoundAfterDraw(checkBoundAfterDraw:Boolean){
        this.checkBoundAfterDraw = checkBoundAfterDraw
    }

}