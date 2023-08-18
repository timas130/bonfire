package com.sayzen.campfiresdk.screens.activities.quests

class QuestItem() {

    var index = ""
    var text = ""

    val buttons = ArrayList<QuestButton>()

    var onStart: () -> Unit = {}
    var onFinish: () -> Unit = {}


    constructor(index:String) : this() {
        this.index = index
    }

    fun text(text: String):QuestItem{
        this.text = text
        return this
    }

    fun addText(text: String): QuestItem {
        this.text += "\n$text"
        return this
    }

    fun addTextJump(text: String): QuestItem {
        this.text += "\n\n$text"
        return this
    }

    fun addButtonParams(text: String): QuestButton {
        return addButtonParams(text){}
    }

    fun addButtonParams(text: String, key: String): QuestButton {
        return addButtonParams(text, key){}
    }

    fun addButtonParams(text: String, action: () -> Unit): QuestButton {
        return addButtonParams(text, text, action)
    }

    fun addButtonParams(text: String, toKey: String?, action: () -> Unit): QuestButton {
        val button = QuestButton()
        button.text = text
        button.action = action
        button.toIndex = toKey
        buttons.add(button)
        return button
    }

    fun addButton(text: String): QuestItem {
        return addButton(text) {}
    }

    fun addButton(text: String, action: () -> Unit): QuestItem {
        return addButton(text, text, action)
    }

    fun addButton(text: String, toKey: String?): QuestItem {
        addButtonParams(text, toKey){}
        return this
    }

    fun addButton(text: String, toKey: String?, action: () -> Unit): QuestItem {
        addButtonParams(text, toKey, action)
        return this
    }

    fun setOnStart(onStart: () -> Unit): QuestItem {
        this.onStart = onStart
        return this
    }

    fun setOnFinish(onFinish: () -> Unit): QuestItem {
        this.onFinish = onFinish
        return this
    }

    //
    //  Support
    //

    inner class QuestButton {

        var text = ""
        var toIndex:String? = null
        var enabled : () -> Boolean = {true}
        var visible : () -> Boolean = {true}
        var action: () -> Unit = {}

        fun enabled(enabled:() -> Boolean):QuestButton{
            this.enabled = enabled
            return this
        }

        fun visible(visible:() -> Boolean):QuestButton{
            this.visible = visible
            return this
        }

        fun finish() = this@QuestItem

    }

}