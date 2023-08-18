package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestEffect
import com.dzen.campfire.api.models.quests.QuestEffectBox
import com.dzen.campfire.api.models.quests.QuestEffectBoxReset
import com.dzen.campfire.api.models.quests.QuestEffectVibrate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsSeek
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.views.ViewButton

class SplashQuestEffect(
    private val effect: QuestEffect? = null,
) : Splash(R.layout.splash_quest_effect) {
    companion object {
        private val boxes = listOf(
            API.LINK_BOX_WITH_FIREWORKS,
            API.LINK_BOX_WITH_SUMMER,
            API.LINK_BOX_WITH_AUTUMN,
            API.LINK_BOX_WITH_WINTER,
            API.LINK_BOX_WITH_BOMB,
            API.LINK_BOX_WITH_SNOW,
            API.LINK_BOX_WITH_BOX,
            API.LINK_BOX_WITH_MAGIC,
            API.LINK_BOX_WITH_MAGIC_X2,
            API.LINK_BOX_WITH_GOOSE,
            API.LINK_BOX_WITH_CONFETTI,
        )
    }

    private val vEffectType: SettingsSelection = findViewById(R.id.vEffectType)
    private val vBoxType: SettingsSelection = findViewById(R.id.vBoxType)
    private val vVibrateTimes: SettingsSeek = findViewById(R.id.vVibrateTimes)
    private val vVibrateLength: SettingsSeek = findViewById(R.id.vVibrateLength)
    private val vVibrateDelayStart: SettingsSeek = findViewById(R.id.vVibrateDelayStart)
    private val vVibrateDelayBetween: SettingsSeek = findViewById(R.id.vVibrateDelayBetween)
    private val vCancel: ViewButton = findViewById(R.id.vCancel)
    private val vEnter: ViewButton = findViewById(R.id.vEnter)

    init {
        setTitle(t(API_TRANSLATE.quests_effect))

        vEffectType.setTitle(t(API_TRANSLATE.quests_edit_text_effect_title))
        vEffectType.add(t(API_TRANSLATE.quests_effect_box))
        vEffectType.add(t(API_TRANSLATE.quests_effect_box_reset))
        vEffectType.add(t(API_TRANSLATE.quests_effect_vibrate))
        vEffectType.setCurrentIndex(when (effect?.type) {
            1L -> 0
            2L -> 1
            3L -> 2
            else -> 0
        })
        vEffectType.onSelected {
            update()
        }

        vBoxType.setTitle(t(API_TRANSLATE.quests_edit_text_effect_box))
        boxes.forEach {
            vBoxType.add(t(API_TRANSLATE.fromBox(it)))
        }
        vBoxType.setCurrentIndex((effect as? QuestEffectBox)?.let { boxes.indexOf(it.box) } ?: 0)

        //#region vibration
        vVibrateTimes.setTitle(t(
            API_TRANSLATE.quests_edit_text_effect_vibrate_times,
            (effect as? QuestEffectVibrate)?.times ?: 2
        ))
        vVibrateTimes.setMinProgress(1)
        vVibrateTimes.setMaxProgress(API.QUEST_EFFECT_VIBRATE_COUNT_MAX.toInt() + 1)
        vVibrateTimes.progress = ((effect as? QuestEffectVibrate)?.times ?: 2)
            .takeUnless { it == 0 } ?: 6
        vVibrateTimes.setOnInstantProgressChanged {
            vVibrateTimes.setTitle(t(
                API_TRANSLATE.quests_edit_text_effect_vibrate_times,
                if (it == API.QUEST_EFFECT_VIBRATE_COUNT_MAX.toInt() + 1) "∞" else it.toString()
            ))
        }

        vVibrateLength.setTitle(t(
            API_TRANSLATE.quests_edit_text_effect_vibrate_length,
            (effect as? QuestEffectVibrate)?.length ?: 100
        ))
        vVibrateLength.setMinProgress(1)
        vVibrateLength.setMaxProgress(API.QUEST_EFFECT_VIBRATE_LENGTH_MAX.toInt())
        vVibrateLength.progress = (effect as? QuestEffectVibrate)?.length ?: 100
        vVibrateLength.setOnInstantProgressChanged {
            vVibrateLength.setTitle(t(
                API_TRANSLATE.quests_edit_text_effect_vibrate_length,
                it
            ))
        }

        vVibrateDelayStart.setTitle(t(
            API_TRANSLATE.quests_edit_text_effect_vibrate_delay_start,
            (effect as? QuestEffectVibrate)?.delayStart ?: 0
        ))
        vVibrateDelayStart.setMinProgress(0)
        vVibrateDelayStart.setMaxProgress(API.QUEST_EFFECT_VIBRATE_DELAY_START_MAX.toInt())
        vVibrateDelayStart.progress = (effect as? QuestEffectVibrate)?.delayStart ?: 0
        vVibrateDelayStart.setOnInstantProgressChanged {
            vVibrateDelayStart.setTitle(t(
                API_TRANSLATE.quests_edit_text_effect_vibrate_delay_start,
                it
            ))
        }

        vVibrateDelayBetween.setTitle(t(
            API_TRANSLATE.quests_edit_text_effect_vibrate_delay_between,
            (effect as? QuestEffectVibrate)?.delayBetween ?: 200
        ))
        vVibrateDelayBetween.setMinProgress(API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MIN.toInt())
        vVibrateDelayBetween.setMaxProgress(API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MAX.toInt())
        vVibrateDelayBetween.progress = (effect as? QuestEffectVibrate)?.delayBetween ?: 200
        vVibrateDelayBetween.setOnInstantProgressChanged {
            vVibrateDelayBetween.setTitle(t(
                API_TRANSLATE.quests_edit_text_effect_vibrate_delay_between,
                it
            ))
        }
        //#endregion vibration

        vCancel.text = t(API_TRANSLATE.app_cancel)
        vCancel.setOnClickListener {
            hide()
        }

        vEnter.text = t(if (effect == null) API_TRANSLATE.app_add else API_TRANSLATE.app_change)
        vEnter.setOnClickListener {
            submit()
        }

        vEffectType.setLineVisible(false)
        vBoxType.setLineVisible(false)
        vVibrateTimes.setLineVisible(false)
        vVibrateLength.setLineVisible(false)
        vVibrateDelayStart.setLineVisible(false)
        vVibrateDelayBetween.setLineVisible(false)

        update()
    }

    private fun update() {
        vBoxType.visibility = View.GONE
        vVibrateTimes.visibility = View.GONE
        vVibrateLength.visibility = View.GONE
        vVibrateDelayStart.visibility = View.GONE
        vVibrateDelayBetween.visibility = View.GONE
        when (vEffectType.getCurrentIndex()) {
            0 -> { // box
                vBoxType.visibility = View.VISIBLE
            }
            1 -> {}
            2 -> {
                vVibrateTimes.visibility = View.VISIBLE
                vVibrateLength.visibility = View.VISIBLE
                vVibrateDelayStart.visibility = View.VISIBLE
                vVibrateDelayBetween.visibility = View.VISIBLE
            }
        }
    }

    private var onDone: (QuestEffect) -> Unit = {}
    fun setOnDone(onDone: (QuestEffect) -> Unit): SplashQuestEffect {
        this.onDone = onDone
        return this
    }

    private fun done(effect: QuestEffect) {
        onDone(effect)
        hide()
    }

    private fun submit() {
        when (vEffectType.getCurrentIndex()) {
            0 -> { // box
                val effect = QuestEffectBox()
                effect.box = boxes.getOrNull(vBoxType.getCurrentIndex()) ?: run {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_text_effect_error_1))
                    return
                }
                done(effect)
            }
            1 -> { // box reset
                done(QuestEffectBoxReset())
            }
            2 -> { // vibration
                val effect = QuestEffectVibrate()
                effect.times =
                    if (vVibrateTimes.progress == API.QUEST_EFFECT_VIBRATE_COUNT_MAX.toInt() + 1) 0
                    else vVibrateTimes.progress
                effect.length = vVibrateLength.progress
                effect.delayStart = vVibrateDelayStart.progress
                effect.delayBetween = vVibrateDelayBetween.progress

                if (effect.times < 0 || effect.times > API.QUEST_EFFECT_VIBRATE_COUNT_MAX) {
                    ToolsToast.show(t(API_TRANSLATE.app_error))
                    return
                }
                if (effect.length < 1 || effect.length > API.QUEST_EFFECT_VIBRATE_LENGTH_MAX) {
                    ToolsToast.show(t(API_TRANSLATE.app_error))
                    return
                }
                if (effect.delayStart < 0 || effect.delayStart > API.QUEST_EFFECT_VIBRATE_DELAY_START_MAX) {
                    ToolsToast.show(t(API_TRANSLATE.app_error))
                    return
                }
                val min = if (effect.times == 0) API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_INF_MIN
                          else API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MIN
                if (effect.delayBetween < min || effect.delayBetween > API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MAX) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_text_effect_error_1, min))
                    return
                }

                done(effect)
            }
            else -> {
                ToolsToast.show(t(API_TRANSLATE.app_error))
            }
        }
    }
}