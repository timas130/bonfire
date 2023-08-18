package com.sayzen.campfiresdk.screens.quests

import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.dzen.campfire.api.requests.quests.RQuestsSaveState
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.animations.*
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsMath

class SQuestPlayer(
    private val details: QuestDetails,
    private val parts: List<QuestPart>,
    private val index: Int,
    private val state: QuestState,
) : Screen(R.layout.screen_user_quest) {
    data class QuestState(
        val variables: HashMap<Long, String> = hashMapOf(),
        val dev: Boolean = false,
        var savedAge: Int = 0,
    ) {
        // Behold, the Campfire64 architecture!

        // returns Long | String | Boolean
        private fun getVariableValue(details: QuestDetails, id: Long): Any {
            val v = details.variablesMap!![id]!!
            return when (v.type) {
                API.QUEST_TYPE_TEXT -> variables[id]!!
                API.QUEST_TYPE_NUMBER -> variables[id]!!.toLong()
                API.QUEST_TYPE_BOOL -> variables[id]!! == "1"
                else -> {
                    // halt and catch fire
                    throw IllegalStateException()
                }
            }
        }

        // returns Long | String | Boolean
        private fun getValue(details: QuestDetails, value: QuestConditionValue): Any {
            return when (value.type) {
                API.QUEST_CONDITION_VALUE_LITERAL_LONG -> value.value
                API.QUEST_CONDITION_VALUE_LITERAL_TEXT -> value.sValue
                API.QUEST_CONDITION_VALUE_LITERAL_BOOL -> value.value == 1L
                API.QUEST_CONDITION_VALUE_VAR -> getVariableValue(details, value.value)
                else -> 0L
            }
        }

        fun conditionFulfilled(details: QuestDetails, cond: QuestPartCondition): Boolean {
            val leftValue = getValue(details, cond.leftValue)
            val rightValue = getValue(details, cond.rightValue)

            return when (cond.cond) {
                API.QUEST_CONDITION_LESS -> (leftValue as Long) < (rightValue as Long)
                API.QUEST_CONDITION_LEQ -> (leftValue as Long) <= (rightValue as Long)
                API.QUEST_CONDITION_EQ -> leftValue == rightValue
                API.QUEST_CONDITION_NEQ -> leftValue != rightValue
                API.QUEST_CONDITION_GEQ -> (leftValue as Long) >= (rightValue as Long)
                API.QUEST_CONDITION_GREATER -> (leftValue as Long) > (rightValue as Long)
                else -> {
                    // halt and catch fire
                    throw IllegalStateException()
                }
            }
        }

        fun executeAction(details: QuestDetails, action: QuestPartAction) {
            when (action.actionType) {
                API.QUEST_ACTION_SET_LITERAL -> {
                    variables[action.varId] = action.sArg
                }
                API.QUEST_ACTION_SET_RANDOM -> {
                    if (details.variablesMap!![action.varId]!!.type == API.QUEST_TYPE_BOOL) {
                        action.lArg1 = 0
                        action.lArg2 = 1
                    }
                    variables[action.varId] = ToolsMath.randomLong(action.lArg1, action.lArg2).toString()
                }
                API.QUEST_ACTION_SET_ANOTHER -> {
                    variables[action.varId] = variables[action.lArg1]!!
                }
                API.QUEST_ACTION_ADD_LITERAL -> {
                    when (details.variablesMap!![action.varId]!!.type) {
                        API.QUEST_TYPE_BOOL -> {
                            variables[action.varId] = if (variables[action.varId]!! != "1") "1" else "0"
                        }
                        API.QUEST_TYPE_TEXT -> {
                            variables[action.varId] += action.sArg
                        }
                        API.QUEST_TYPE_NUMBER -> {
                            variables[action.varId] = (
                                variables[action.varId]!!.toLong() +
                                action.sArg.toLong()
                            ).toString()
                        }
                    }
                }
                API.QUEST_ACTION_ADD_ANOTHER -> {
                    when (details.variablesMap!![action.varId]!!.type) {
                        API.QUEST_TYPE_TEXT -> {
                            variables[action.varId] += variables[action.lArg1]!!
                        }
                        API.QUEST_TYPE_NUMBER -> {
                            variables[action.varId] = (
                                variables[action.varId]!!.toLong() +
                                variables[action.lArg1]!!.toLong()
                            ).toString()
                        }
                    }
                }
                API.QUEST_ACTION_SUB_ANOTHER -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() -
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_SET_ARANDOM -> {
                    variables[action.varId] = ToolsMath.randomLong(
                        variables[action.lArg1]!!.toLong(),
                        variables[action.lArg2]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_MULTIPLY -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() *
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_DIVIDE -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() /
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_BIT_AND -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() and
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_BIT_OR -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() or
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
            }
        }
    }

    private val part = parts[index]

    private val vImageWrapper: LayoutCorned = findViewById(R.id.vImageWrapper)
    private val vTitleImage: ImageView = findViewById(R.id.vTitleImage)
    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vText: ViewText = findViewById(R.id.vText)
    private val vInputContainer: LinearLayout = findViewById(R.id.vInputContainer)
    private val vButtonContainer: LinearLayout = findViewById(R.id.vButtonContainer)
    private val vSaveState: ViewIcon = findViewById(R.id.vSaveState)
    private val vDebugButton: ViewIcon = findViewById(R.id.vDebugButton)

    private val vibrateHandler = Handler(Looper.getMainLooper())
    private var vibrateRemaining = 0
    private var vibrateRunnable: Runnable? = null
    private var vibrationEnabled = true

    init {
        disableNavigation()
        disableShadows()

        setTitle(details.title)

        if (part !is QuestPartText) {
            throw AssertionError()
        }

        if (part.imageId > 0) {
            vImageWrapper.visibility = VISIBLE
            if (part.gifId > 0) {
                ImageLoader.loadGif(part.imageId, part.gifId, vTitleImage)
            } else {
                ImageLoader.load(part.imageId).into(vTitleImage)
            }
        } else {
            vImageWrapper.visibility = GONE
        }

        vTitle.text = part.title
        
        var text = part.text
        for (pair in state.variables) {
            val id = pair.key
            val value = pair.value
            text = text.replace("{${details.variablesMap!![id]?.devName}}", value, ignoreCase = true)
            text = text.replace("{$id}", value, ignoreCase = true)
        }
        
        vText.text = text
        ControllerLinks.makeLinkable(vText)

        part.inputs.forEach {
            val field = if (it.type == API.QUEST_TYPE_BOOL) SettingsCheckBox(context)
            else SettingsField(context)

            if (field.layoutParams == null) {
                field.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }
            val params = field.layoutParams as LayoutParams
            params.marginStart = ToolsView.dpToPx(16).toInt()
            params.marginEnd = ToolsView.dpToPx(16).toInt()
            params.bottomMargin = ToolsView.dpToPx(8).toInt()

            if (field is SettingsField) {
                field.setHint(it.hint)
                field.setInputType(
                    when (it.type) {
                        API.QUEST_TYPE_TEXT -> InputType.TYPE_CLASS_TEXT
                        API.QUEST_TYPE_NUMBER -> InputType.TYPE_CLASS_NUMBER
                        else -> InputType.TYPE_CLASS_TEXT
                    } or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                )
                field.setText(it.defaultValue)
            } else if (field is SettingsCheckBox) {
                field.setTitle(it.hint)
                field.setChecked(it.defaultValue == "1")
            }

            vInputContainer.addView(field)
        }

        part.buttons.forEachIndexed { idx, it ->
            val button = Button(context, null, R.style.Button)
            button.gravity = Gravity.CENTER
            button.text = it.label
            button.textSize = 18f
            button.setTextColor(when (it.color) {
                // QUEST_BUTTON_COLOR_DEFAULT -> else
                API.QUEST_BUTTON_COLOR_RED -> ToolsResources.getColor(R.color.red_500)
                API.QUEST_BUTTON_COLOR_ORANGE -> ToolsResources.getColor(R.color.orange_500)
                API.QUEST_BUTTON_COLOR_YELLOW -> ToolsResources.getColor(R.color.yellow_500)
                API.QUEST_BUTTON_COLOR_GREEN -> ToolsResources.getColor(R.color.green_500)
                API.QUEST_BUTTON_COLOR_AQUA -> ToolsResources.getColor(R.color.blue_400)
                API.QUEST_BUTTON_COLOR_BLUE -> ToolsResources.getColor(R.color.blue_700)
                API.QUEST_BUTTON_COLOR_PURPLE -> ToolsResources.getColor(R.color.purple_500)
                API.QUEST_BUTTON_COLOR_PINK -> ToolsResources.getColor(R.color.pink_400)
                API.QUEST_BUTTON_COLOR_WHITE -> ToolsResources.getColor(R.color.white)
                else -> ToolsResources.getColorAttr(context, R.attr.colorSecondary)
            })
            val layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, 0, 0, 24)
            layoutParams.gravity = Gravity.CENTER
            button.layoutParams = layoutParams
            button.setOnClickListener {
                println("[Quests] button $idx pressed")
                pressButton(idx)
            }

            vButtonContainer.addView(button)
        }

        part.effects.forEach { effect ->
            when (effect) {
                is QuestEffectVibrate -> {
                    vibrateRemaining = effect.times.takeIf { it != 0 } ?: Int.MAX_VALUE
                    vibrateRunnable = getVibrationRunnable(effect)
                    vibrateHandler.postDelayed(vibrateRunnable!!, effect.delayStart.toLong())
                }
                is QuestEffectBox -> {
                    when (effect.box.link) {
                        API.LINK_BOX_WITH_FIREWORKS.link -> ControllerScreenAnimations.fireworks(clear = false)
                        API.LINK_BOX_WITH_SUMMER.link -> ControllerScreenAnimations.addAnimation(DrawAnimationSummer())
                        API.LINK_BOX_WITH_AUTUMN.link -> ControllerScreenAnimations.addAnimation(DrawAnimationAutumn())
                        API.LINK_BOX_WITH_WINTER.link -> ControllerScreenAnimations.addAnimation(DrawAnimationWinter())
                        API.LINK_BOX_WITH_BOMB.link -> ControllerScreenAnimations.addAnimation(DrawAnimationBomb())
                        API.LINK_BOX_WITH_SNOW.link -> ControllerScreenAnimations.addAnimation(DrawAnimationSnow(100))
                        API.LINK_BOX_WITH_MAGIC_X2.link -> ControllerScreenAnimations.addAnimation(DrawAnimationMagic(2f))
                        API.LINK_BOX_WITH_MAGIC.link -> ControllerScreenAnimations.addAnimation(DrawAnimationMagic())
                        API.LINK_BOX_WITH_GOOSE.link -> ControllerScreenAnimations.addAnimation(DrawAnimationGoose())
                        API.LINK_BOX_WITH_CONFETTI.link -> ControllerScreenAnimations.addAnimation(DrawAnimationConfetti())
                        API.LINK_BOX_WITH_BOX.link -> ControllerScreenAnimations.box(1, clear = false)
                    }
                }
                is QuestEffectBoxReset -> {
                    ControllerScreenAnimations.clearAnimation()
                }
            }
        }

        if (state.dev) vSaveState.visibility = GONE
        updateSaveButtonIcon()
        vSaveState.setOnClickListener {
            val splash = ToolsView.showProgressDialog()
            saveState {
                splash.hide()
            }
        }

        if (state.dev) vDebugButton.setImageResource(R.drawable.baseline_bug_report_24)
        vDebugButton.setOnClickListener {
            Navigator.replace(SQuestDebug(details, state, parts, index))
        }
    }

    private fun getVibrationRunnable(effect: QuestEffectVibrate) = Runnable {
        if (vibrationEnabled) ToolsVibration.vibrate(effect.length.toLong())
        if (--vibrateRemaining > 0) {
            vibrateHandler.postDelayed(vibrateRunnable!!, (effect.delayBetween + effect.length).toLong())
        }
    }

    override fun onDestroy() {
        vibrateRunnable?.let { vibrateHandler.removeCallbacks(it) }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        vibrationEnabled = false
    }

    override fun onResume() {
        super.onResume()
        vibrationEnabled = true
    }

    private fun endQuest() {
        SplashAlert()
            .setTitle(t(API_TRANSLATE.quests_end))
            .setText(t(API_TRANSLATE.quests_end_d))
            .setOnEnter(t(API_TRANSLATE.quests_play_again)) {
                it.hide()
                Navigator.replace(SQuestPlayer(
                    details,
                    parts,
                    index = 0,
                    QuestState(dev = state.dev),
                ))
            }
            .setOnCancel(t(API_TRANSLATE.app_back)) {
                it.hide()
                Navigator.back()
            }
            .asSheetShow()
    }

    private fun updateVariables(): Boolean {
        if (part !is QuestPartText) {
            // halt and catch fire
            return false
        }
        part.inputs.forEachIndexed { idx, it ->
            when (val view = vInputContainer.getChildAt(idx)) {
                is SettingsField -> {
                    if (it.type == API.QUEST_TYPE_NUMBER) {
                        view.getText().toLongOrNull() ?: return false
                    }
                    state.variables[it.varId] = view.getText()
                }
                is SettingsCheckBox -> {
                    state.variables[it.varId] = if (view.isChecked()) "1" else "0"
                }
                else -> {
                    // halt and catch fire
                    return false
                }
            }
        }
        return true
    }

    private fun pressButton(index: Int) {
        if (part !is QuestPartText) {
            // halt and catch fire
            return
        }
        if (! updateVariables()) {
            println("[Quests] failed to updateVariables()")
            return
        }

        val button = part.buttons[index]
        jumpTo(button.jumpToId)
    }

    private fun updateSaveButtonIcon() {
        if (state.savedAge <= 0) vSaveState.setImageResource(R.drawable.ic_check_white_24dp)
        else vSaveState.setImageResource(R.drawable.baseline_save_24)
    }

    private fun saveState(cb: (ok: Boolean) -> Unit = { _ -> }) {
        RQuestsSaveState(
            details.id,
            Json().apply {
                for (item in state.variables) {
                    put(item.key.toString(), item.value)
                }
            },
            index,
        )
            .onError {
                ToolsToast.show(t(API_TRANSLATE.quests_error_save))
                cb(false)
            }
            .onComplete {
                cb(true)
                state.savedAge = 0
                updateSaveButtonIcon()
            }
            .send(api)
    }

    fun jumpTo(toId: Long, fromIndex: Int = this.index, depth: Int = 0) {
		if (depth > API.QUEST_MAX_DEPTH) {
			ToolsToast.show(t(API_TRANSLATE.quests_error_depth))
			return
		}

		println("[Quests] jumping to $toId (depth $depth)")
        if (toId == -2L) { // next part
            if (parts.size <= fromIndex + 1) {
                endQuest()
            } else {
                jumpTo(parts[fromIndex + 1].id, fromIndex, depth + 1)
            }
            return
        }
        if (toId == -1L) {
            endQuest()
            return
        }

        val partIdx = parts
            .indexOfFirst { it.id == toId }
            .takeIf { it != -1 } ?: run {
				println("[Quests] failed to find part w/ id $toId")
				return
			}
        when (val part = parts[partIdx]) {
            is QuestPartText -> {
                state.savedAge++
                val player = SQuestPlayer(
                    details,
                    parts,
                    partIdx,
                    state
                )
                Navigator.replace(player)
            }
            is QuestPartCondition -> {
                val fulfilled = try {
                    state.conditionFulfilled(details, part)
                } catch (e: Exception) {
                    e.printStackTrace()
                    SplashAlert()
                        .setText(t(API_TRANSLATE.quests_error_uninit))
                        .setOnCancel(t(API_TRANSLATE.app_ok))
                        .asSheetShow()
                    return
                }
                if (fulfilled) {
                    jumpTo(part.trueJumpId, partIdx, depth + 1)
                } else {
                    jumpTo(part.falseJumpId, partIdx, depth + 1)
                }
            }
            is QuestPartAction -> {
                try {
                    state.executeAction(details, part)
                } catch (e: Exception) {
                    e.printStackTrace()
                    SplashAlert()
                        .setText(t(API_TRANSLATE.quests_error_uninit))
                        .setOnCancel(t(API_TRANSLATE.app_ok))
                        .asSheetShow()
                    return
                }
                jumpTo(part.jumpId, partIdx, depth + 1)
            }
        }
    }
}
