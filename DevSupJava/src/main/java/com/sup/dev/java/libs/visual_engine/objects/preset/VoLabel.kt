package com.sup.dev.java.libs.visual_engine.objects.preset

import com.sup.dev.java.libs.visual_engine.graphics.VeFont
import com.sup.dev.java.libs.visual_engine.graphics.VeGraphics
import com.sup.dev.java.libs.visual_engine.objects.VisualObject
import com.sup.dev.java.libs.visual_engine.root.VeGui

class VoLabel : VisualObject() {

    private var font = VeGui.FONT_TITLE
    private var color = VeGui.BLACK
    private var text = ""

    init {
        setSize(100f, VeGui.getFontSize(font))
    }

    fun setColor(color:Int){
        this.color = color
    }

    fun setText(text:String){
        this.text = text
        setW(VeGui.getStringSize(text, font))
    }

    fun setFont(font:VeFont){
        this.font = font
        setSize(VeGui.getFontSize(font), font.getWidth(text))
    }

    override fun drawSelf(g: VeGraphics) {
        super.drawSelf(g)

        g.setFont(font)
        g.setColor(color)
        g.drawString(text, 0f, 0f)
    }

}