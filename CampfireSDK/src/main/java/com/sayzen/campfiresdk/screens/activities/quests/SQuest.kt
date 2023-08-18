package com.sayzen.campfiresdk.screens.activities.quests

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLoaderUrl
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsThreads

abstract class SQuest : Screen(R.layout.screen_quest) {

    var quest = ArrayList<QuestItem>()
    val vTitleImage: ImageView = findViewById(R.id.vTitleImage)
    val vLabel: TextView = findViewById(R.id.vLabel)
    val vText: ViewText = findViewById(R.id.vText)
    val vButton_1: Button = findViewById(R.id.vButton_1)
    val vButton_2: Button = findViewById(R.id.vButton_2)
    val vButton_3: Button = findViewById(R.id.vButton_3)
    val vButton_4: Button = findViewById(R.id.vButton_4)
    val vButtonContainer: ViewGroup = findViewById(R.id.vButtonContainer)

    var currentItem: QuestItem? = null
    var globalLabel = ""
    var globalImage:Any = 0L

    init {
        isNavigationAllowed = false
        navigationBarColor = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)
        statusBarColor = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)

        ToolsThreads.main(true) {
            val saveItem = ToolsStorage.getString(getSaveKey(), "") ?: ""
            if (!toScreen(saveItem)) toFirstItem()
        }
        createQuest()
        checkQuest()
    }

    fun checkQuest(){
        for(i in quest){
            for(b in i.buttons){
                if(b.toIndex != null) {
                    var found = false
                    for (q in quest) if (q.index == b.toIndex)
                        found = true
                    if (!found) err("Quest Нет ключа для экрана квеста [${b.toIndex}]")
                }
            }
        }
    }

    abstract fun createQuest()

    abstract fun toFirstItem()

    abstract fun getKey(): String

    private fun getSaveKey() = "SQuest.item:" + ControllerApi.account.getId() + ":" + getKey()

    fun addQuest(vararg items: QuestItem) {
        for (i in items) {
            for (q in quest) {
                if(q.index == i.index)
                    err("Quest Уже существует экран с ключем [${q.index}]")
            }
            quest.add(i)
        }
        quest.addAll(items)
    }

    fun toScreen(index: String): Boolean {
        for (i in quest) if (i.index == index) {
            toScreen(i)
            ToolsStorage.put(getSaveKey(), index)
            return true
        }
        return false
    }

    fun toScreen(item: QuestItem) {
        if (currentItem == null) setQuestItemNoAnimation(item)
        else setQuestItemWithAnimation(item)
    }

    fun setQuestItemWithAnimation(item: QuestItem) {
        vButton_1.setOnClickListener { }
        vButton_2.setOnClickListener { }
        vButton_3.setOnClickListener { }
        vButton_4.setOnClickListener { }

        ToolsView.toAlpha(vButton_1)
        ToolsView.toAlpha(vButton_2)
        ToolsView.toAlpha(vButton_3)
        ToolsView.toAlpha(vButton_4)
        ToolsView.toAlpha(vText) {
            setQuestItemNoAnimation(item)
            ToolsView.fromAlpha(vButton_1)
            ToolsView.fromAlpha(vButton_2)
            ToolsView.fromAlpha(vButton_3)
            ToolsView.fromAlpha(vButton_4)
            ToolsView.fromAlpha(vText)
        }
    }

    fun setQuestItemNoAnimation(item: QuestItem) {
        currentItem?.onFinish?.invoke()
        this.currentItem = item
        currentItem?.onStart?.invoke()

        if(globalImage is Long) {
            ImageLoader.load(globalImage as Long).into(vTitleImage)
        } else if(globalImage is String){
            ImageLoaderUrl(globalImage as String).into(vTitleImage)
        }

        vLabel.text = parseText(globalLabel)

        vText.text = parseText(item.text)
        ControllerLinks.makeLinkable(vText)

        vLabel.visibility = if (vLabel.text.isEmpty()) View.GONE else View.VISIBLE

        val buttons = arrayListOf(vButton_1, vButton_2, vButton_3, vButton_4)

        vButtonContainer.removeAllViews()

        var i = 0
        var n = 0

        while (n < item.buttons.size && i < buttons.size) {
            val vButton = buttons[i]
            val button = item.buttons[n]

            if (!button.visible.invoke()) {
                n++
                continue
            }

            vButton.text = parseText(button.text)
            vButton.setOnClickListener {
                button.action.invoke()
                if(button.toIndex != null) toScreen(button.toIndex!!)
            }
            vButton.isEnabled = button.enabled.invoke()
            vButtonContainer.addView(vButton)

            i++
            n++

        }

    }

    fun parseText(text: String): String {
        var result = text
        result = result.replace("%user_name%", ControllerApi.account.getName())

        return result
    }


}