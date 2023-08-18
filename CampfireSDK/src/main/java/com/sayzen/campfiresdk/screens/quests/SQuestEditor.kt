package com.sayzen.campfiresdk.screens.quests

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.dzen.campfire.api.requests.quests.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardQuestDetails
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.models.events.quests.EventQuestPartChangedOrAdded
import com.sayzen.campfiresdk.screens.post.create.creators.CardMove
import com.sayzen.campfiresdk.screens.quests.edit.*
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardTitle
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashFieldTwo
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json

class SQuestEditor(
    private var questDetails: QuestDetails,
    parts: Array<QuestPart>,
) : SRecycler() {
    private val container = object : QuestPartContainer {
        override fun getDetails(): QuestDetails = questDetails

        override fun getParts(): Array<QuestPart> = adapter.get(CardQuestPart::class).map { it.part }.toTypedArray()
    }

    val eventBus = EventBus
        .subscribe(EventQuestChanged::class) {
            if (it.quest.id == questDetails.id) {
                questDetails = it.quest
                onDetailsUpdated()
            }
        }
        .subscribe(EventQuestPartChangedOrAdded::class) { ev ->
            ev.parts.forEach { part ->
                adapter
                    .find<CardQuestPart> { (it as? CardQuestPart)?.part?.id == part.id }
                    ?.let {
                        it.part = part
                        it.update()
                        it
                    }
                    ?: adapter.add(CardQuestPart.instance(part, container) { v, x, y ->
                        openQuestPartMenu(part, v, x, y)
                    })
            }
        }

    private val adapter = RecyclerCardAdapter()

    init {
        setScreenColorBackground()

        onDetailsUpdated()
        addTitleCards()

        vFab.visibility = VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            openNewQuestPart()
        }

        vRecycler.adapter = adapter

        parts.forEach {
            adapter.add(CardQuestPart.instance(it, container) { v, x, y ->
                openQuestPartMenu(it, v, x, y)
            })
        }

        addToolbarIcon(R.drawable.ic_play_arrow_white_24dp) {
            startQuest()
        }
    }

    private fun openQuestPartMenu(part: QuestPart, view: View, x: Float, y: Float) {
        val card = adapter.find<CardQuestPart> {
            (it as? CardQuestPart)?.part?.id == part.id
        }
        SplashMenu()
            .add(t(API_TRANSLATE.app_remove)) {
                ApiRequestsSupporter.executeProgressDialog(RQuestsRemovePart(questDetails.id, part.id)) { resp ->
                    if (card != null) adapter.remove(card)
                }
            }
            .add(t(API_TRANSLATE.app_move)) {
                if (card != null) startMove(card)
            }
            .asPopupShow(view, x, y)
    }

    private fun startMove(movingCard: CardQuestPart) {
        vFab.setImageResource(R.drawable.ic_clear_white_24dp)
        ToolsView.setFabColorR(vFab, R.color.red_700)
        vFab.setOnClickListener { stopMove() }

        val startPosition = adapter.indexOf(movingCard)
        for (i in adapter.size() downTo 3) {
            if (i == startPosition || i == startPosition + 1) continue
            val beforeCard = adapter.getOrNull(i) as? CardQuestPart?
            adapter.add(i, CardMove {
                movePage(movingCard, beforeCard)
                stopMove()
            })
        }
        for (card in adapter.get(CardQuestPart::class)) {
            card.editMode = false
        }
    }

    private fun stopMove() {
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        ToolsView.setFabColor(vFab, ToolsResources.getColorAttr(R.attr.colorSecondary))
        vFab.setOnClickListener { openNewQuestPart() }

        for (card in adapter.get(CardMove::class)) {
            adapter.remove(card)
        }
        for (card in adapter.get(CardQuestPart::class)) {
            card.editMode = true
        }
    }

    private fun movePage(argMovingCard: CardQuestPart, argBeforeCard: CardQuestPart?, cb: () -> Unit = {}) {
        val impl = { movingCard: CardQuestPart, beforeCard: CardQuestPart, cb: () -> Unit ->
            ApiRequestsSupporter.executeProgressDialog(RQuestsReorderPart(
                questId = questDetails.id,
                partId = movingCard.part.id,
                partIdBefore = beforeCard.part.id,
            )) { _ ->
                adapter.remove(movingCard)
                val targetIdx = adapter.indexOf(beforeCard)
                adapter.add(targetIdx, movingCard)
                cb()
            }
        }

        if (argBeforeCard == null) {
            val lastCard = adapter[adapter.size() - 2] as CardQuestPart
            impl(argMovingCard, lastCard) {
                impl(lastCard, argMovingCard) {
                    stopMove()
                    cb()
                }
            }
        } else {
            impl(argMovingCard, argBeforeCard) {
                stopMove()
                cb()
            }
        }
    }

    private fun checkQuest(): Boolean {
        val d = ToolsView.showProgressDialog(t(API_TRANSLATE.quests_edit_checking))

        val errors = mutableListOf<QuestException>()

        val parts = adapter.get(CardQuestPart::class).map { it.part }
        if (parts.isEmpty()) {
            d.hide()
            ToolsToast.show(t(API_TRANSLATE.quests_edit_error_10))
            return false
        }
        parts.forEach {
            it.checkValid(questDetails, parts, errors)
        }

        if (parts[0].type != API.QUEST_PART_TYPE_TEXT) {
            errors.add(QuestException(API_TRANSLATE.quests_edit_error_8, partId = -1))
        }

        if (errors.isNotEmpty()) {
            val alert = SplashAlert()
            alert.setTitle(t(API_TRANSLATE.quests_edit_errors))

            val sb = StringBuilder()
            for (error in errors.subList(0, 5.coerceAtMost(errors.size))) {
                val part = parts.find { it.id == error.partId }
                sb.append("*${part?.toSelectorString() ?: "---"}*\n")
                sb.append("${t(error.translate, *error.params)}\n\n")
            }

            d.hide()

            alert.setText(sb.toString())
            alert.setOnEnter(t(API_TRANSLATE.app_ok))
            alert.asSheetShow()

            return false
        }

        d.hide()
        return true
    }

    private fun startQuest() {
        if (!checkQuest()) return

        val parts = adapter.get(CardQuestPart::class).map { it.part }
        Navigator.to(SQuestPlayer(questDetails, parts, 0, SQuestPlayer.QuestState(dev = true)))
    }

    private fun onDetailsUpdated() {
        setTitle(questDetails.title)

        val cardDetails = CardQuestDetails(
            questDetails,
            onClick = { openDetailsEditor() },
            onPublish = { publishQuest() },
            showMore = true,
        )
        if (!adapter.isEmpty) adapter.replace(0, cardDetails)
        else adapter.add(cardDetails)
    }

    private fun addTitleCards() {
        adapter.add(CardQuestVariables(questDetails))
        adapter.add(CardTitle(t(API_TRANSLATE.quests_contents)))
    }

    private fun openDetailsEditor() {
        SplashFieldTwo()
            .setTitle(t(API_TRANSLATE.quests_edit_details))
            .setHint_1(t(API_TRANSLATE.quests_title))
            .setText_1(questDetails.title)
            .setMin_1(API.QUEST_TITLE_MIN_L)
            .setMax_1(API.QUEST_TITLE_MAX_L)
            .setLinesCount_1(1)

            .setHint_2(t(API_TRANSLATE.app_description))
            .setText_2(questDetails.description)
            .setMax_2(API.QUEST_DESCRIPTION_MAX_L)
            .setMultiLine_2()

            .setOnEnter(t(API_TRANSLATE.app_change)) { _, title, description ->
                editQuestDetails(questDetails) {
                    it.title = title
                    it.description = description
                }
            }
            .asSheetShow()
    }

    private fun publishQuest() {
        if (!checkQuest()) return
        if (questDetails.description.isBlank()) {
            ToolsToast.show(t(API_TRANSLATE.quests_edit_error_12))
            return
        }

        SplashAlert()
            .setText(t(API_TRANSLATE.quests_publish_q))
            .setOnEnter(t(API_TRANSLATE.quests_publish_q_absolutely)) {
                it.hide()
                ApiRequestsSupporter.executeProgressDialog(
                    RQuestsPublish(questDetails.id)
                ) { _ ->
                    questDetails.status = API.STATUS_PUBLIC
                    EventBus.post(EventPostStatusChange(questDetails.id, API.STATUS_PUBLIC))
                    Navigator.replace(SQuest(questDetails, 0))
                }.onApiError {
                    SplashAlert()
                        .setText(t(API_TRANSLATE.quests_edit_error_11))
                        .setOnEnter(t(API_TRANSLATE.app_ok))
                        .asSheetShow()
                }
            }
            .setOnCancel(t(API_TRANSLATE.quests_publish_q_not_yet)) {
                it.hide()
            }
            .asSheetShow()
    }

    private fun openNewQuestPart() {
        val splash = SplashMenu()
        val onDone = { part: QuestPart ->
            ApiRequestsSupporter.executeProgressDialog(RQuestsAddPart(questDetails.id, arrayOf(part))) { resp ->
                EventBus.post(EventQuestPartChangedOrAdded(resp.parts))
                Navigator.back()
            }.onApiError(RQuestsAddPart.BAD_PART) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_error_upload))
            }
        }

        splash
            .add(t(API_TRANSLATE.quests_part_text)) {
                Navigator.to(SQuestPartTextCreate(questDetails, container, QuestPartText()) { onDone(it) })
            }
            .add(t(API_TRANSLATE.quests_part_condition)) {
                Navigator.to(SQuestPartConditionCreate(questDetails, container, QuestPartCondition()) { onDone(it) })
            }
            .add(t(API_TRANSLATE.quests_part_action)) {
                Navigator.to(SQuestPartActionCreate(questDetails, container, QuestPartAction()) { onDone(it) })
            }
            .asSheetShow()
    }
}

fun editQuestDetails(questDetails: QuestDetails, modify: (QuestDetails) -> Unit) {
    val newDetails = QuestDetails()
    // i'm sorry
    questDetails.jsonDB = questDetails.jsonDB(true, Json())
    newDetails.json(false, questDetails.json(true, Json()))
    modify(newDetails)
    newDetails.jsonDB = newDetails.jsonDB(true, Json())
    ApiRequestsSupporter.executeProgressDialog(RQuestsModify(newDetails)) { resp ->
        ToolsToast.show(t(API_TRANSLATE.app_done))
        EventBus.post(EventQuestChanged(resp.quest))
    }.onApiError(RQuestsModify.E_INVALID_VARS) {
        if (newDetails.variables.size > API.QUEST_VARIABLES_MAX)
            ToolsToast.show(t(API_TRANSLATE.quests_variable_too_many))
        else
            ToolsToast.show(t(API_TRANSLATE.quests_variable_too_long))
    }.onApiError(RQuestsModify.E_INVALID_NAME) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_name))
    }.onApiError(RQuestsModify.E_INVALID_DESCRIPTION) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_description))
    }.onApiError(RQuestsModify.E_NOT_DRAFT) {
        ToolsToast.show(t(API_TRANSLATE.quests_edit_error_not_draft))
    }
}
