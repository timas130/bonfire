package com.sayzen.campfiresdk.screens.quests.edit

import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.post.create.creators.SplashAdd
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.json.Json

class SQuestPartTextEditor(
    val oldText: String = "",
    val details: QuestDetails,
    val onEnter: (String) -> Unit
) : Screen(R.layout.screen_post_create_text) {
    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vInsertVariable: ViewIcon = findViewById(R.id.vInsertVariable)
    private val vSaveText: ViewIcon = findViewById(R.id.vSaveText)
    private val vLoadText: ViewIcon = findViewById(R.id.vLoadText)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_text))

        findViewById<LinearLayout>(R.id.vOptionsContainer).visibility = GONE
        findViewById<LinearLayout>(R.id.vQuestOptionsContainer).visibility = VISIBLE

        vField.hint = t(API_TRANSLATE.quests_edit_text_content_hint)
        vField.isSingleLine = false
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.setText(oldText)
        vField.addTextChangedListener(TextWatcherChanged { update() })
        ControllerMention.startFor(vField)

        vInsertVariable.setOnClickListener {
            val splash = SplashMenu()
            for (variable in details.variables) {
                splash.add(variable.devName) {
                    vField.text.insert(vField.selectionStart, "{${variable.devName}}")
                }
            }
            splash.asSheetShow()
        }

        vSaveText.setOnClickListener {
            val fieldSplash = SplashField()
                .setHint(t(API_TRANSLATE.quests_draft_name))
                .setMin(1)
                .setOnEnter(t(API_TRANSLATE.app_save)) { _, key ->
                    val json = ToolsStorage.getJson("quests_drafts") ?: Json()
                    json.put(key, vField.text.toString())
                    ToolsStorage.put("quests_drafts", json)
                }

            val items = ToolsStorage.getJson("quests_drafts")
                ?.takeIf { it.isNotEmpty() }
            if (items == null) {
                fieldSplash.asSheetShow()
                return@setOnClickListener
            }

            val menuSplash = SplashMenu()
            menuSplash.add(t(API_TRANSLATE.quests_draft_enter)) {
                menuSplash.hide()
                fieldSplash.asSheetShow()
            }
            items.forEach { name, _ ->
                menuSplash.add(name) {
                    items.put(name, vField.text.toString())
                }
            }
            menuSplash.asSheetShow()
        }

        vLoadText.setOnClickListener {
            val splash = SplashMenu()
            val items = ToolsStorage.getJson("quests_drafts")
                ?.takeIf { it.isNotEmpty() }
            if (items == null) {
                ToolsToast.show(t(API_TRANSLATE.quests_no_drafts))
                return@setOnClickListener
            }
            items.forEach { name, value ->
                splash.add(name) {
                    vField.setText(value as String)
                }
            }
            splash.asSheetShow()
        }

        vFab.setOnClickListener {
            if (oldText != vField.text.toString()) {
                onEnter(vField.text.toString())
            }
            Navigator.remove(this)
        }

        update()
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    override fun onBackPressed(): Boolean {
        if (oldText == vField.text.toString()) return false
        SplashAdd.showConfirmCancelDialog(this)
        return true
    }

    private fun update() {
        val s = vField.text
        ToolsView.setFabEnabledR(vFab, s.isNotEmpty() && s.length <= API.QUEST_TEXT_TEXT_MAX_L, R.color.green_700)
    }
}