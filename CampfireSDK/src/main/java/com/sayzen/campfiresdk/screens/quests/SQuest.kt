package com.sayzen.campfiresdk.screens.quests

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGet
import com.dzen.campfire.api.requests.quests.RQuestsSaveState
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardQuestDetails
import com.sayzen.campfiresdk.models.cards.quests.CardQuestInfo
import com.sayzen.campfiresdk.models.cards.quests.CardQuestStart
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.AdapterComments
import com.sayzen.campfiresdk.support.adapters.XPublication
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.json.Json

class SQuest(
    val details: QuestDetails,
    commentId: Long,
) : Screen(R.layout.screen_quest_view) {
    companion object {
        fun instance(id: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RQuestsGet(id)) { r ->
                SQuest(r.questDetails, commentId)
            }
        }

        fun instance(id: Long, action: NavigationAction) {
            instance(id, 0, action)
        }
    }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vShare: ViewIcon = findViewById(R.id.vShare)
    private val vMenu: ViewIcon = findViewById(R.id.vMenu)

    private val adapter: AdapterComments = AdapterComments(details.id, commentId, vRecycler)
    private val detailsCard: CardQuestDetails = CardQuestDetails(details, onClick = {})

    private val xPublication = XPublication(
        details,
        onChangedAccount = { cardInfo.updateAccount() },
        onChangedFandom = {},
        onChangedKarma = { cardInfo.updateKarma() },
        onChangedComments = {
            cardInfo.updateComments()
            adapter.loadBottom()
        },
        onChangedReports = { cardInfo.updateReports() },
        onChangedImportance = {},
        onRemove = { Navigator.remove(this) },
        onChangedReactions = {},
    )
    private val cardInfo: CardQuestInfo = CardQuestInfo(xPublication)

    init {
        setTitle(t(API_TRANSLATE.quest))

        vRecycler.layoutManager = LinearLayoutManager(context)
        ToolsView.setRecyclerAnimation(vRecycler)

        adapter.add(detailsCard)
        adapter.add(CardQuestStart(details))
        adapter.add(cardInfo)
        adapter.setCommentButton(vFab)

        vRecycler.adapter = adapter

        vShare.setOnClickListener {
            ControllerApi.sharePost(details.id, API.PUBLICATION_TYPE_QUEST)
        }
        ToolsView.setOnClickCoordinates(vMenu) { view, x, y ->
            SplashMenu()
                .add(t(API_TRANSLATE.app_remove)) { removeSelf() }
                    .condition(ControllerApi.isCurrentAccount(details.creator.id))
                .add(t(API_TRANSLATE.app_to_drafts)) { toDrafts() }
                    .condition(details.isPublic && ControllerApi.isCurrentAccount(details.creator.id))
                .add(t(API_TRANSLATE.quests_clear)) { clearProgress() }
                    .condition(details.isPublic)
                .add(t(API_TRANSLATE.app_report)) { ControllerPublications.report(details) }
                    .condition(!ControllerApi.isCurrentAccount(details.creator.id))
                .add(t(API_TRANSLATE.app_copy_link)) {
                    ToolsAndroid.setToClipboard(ControllerLinks.linkToQuest(details.id))
                    ToolsToast.show(t(API_TRANSLATE.app_copied))
                }
                    .condition(details.isPublic)

                .groupCondition(details.isPublic)
                .spoiler(t(API_TRANSLATE.app_moderator))
                    .add(t(API_TRANSLATE.app_clear_reports)) { ControllerPublications.clearReports(details) }
                        .backgroundRes(R.color.blue_700)
                        .textColorRes(R.color.white)
                        .condition(
                            ControllerApi.can(API.LVL_QUEST_MODERATOR) &&
                            details.reportsCount > 0 &&
                            !ControllerApi.isCurrentAccount(details.creator.id)
                        )
                    .add(t(API_TRANSLATE.publication_menu_moderator_to_drafts)) { ControllerPost.moderatorToDrafts(details.id) }
                        .backgroundRes(R.color.blue_700)
                        .textColorRes(R.color.white)
                        .condition(
                            ControllerApi.can(API.LVL_QUEST_MODERATOR) &&
                            !ControllerApi.isCurrentAccount(details.id)
                        )

                .asPopupShow(view, x, y)
        }
    }

    private fun removeSelf() {
        ControllerApi.removePublication(
            details.id,
            t(API_TRANSLATE.quests_remove_q),
            t(API_TRANSLATE.error_gone)
        ) {
            Navigator.back()
        }
    }

    private fun toDrafts() {
        ControllerPublications.toDrafts(details.id) {
            Navigator.replace(SQuestDrafts())
        }
    }

    private fun clearProgress() {
        SplashAlert()
            .setText(t(API_TRANSLATE.quests_clear_q))
            .setOnEnter(t(API_TRANSLATE.app_yes)) {
                ApiRequestsSupporter.executeProgressDialog(RQuestsSaveState(
                    questId = details.id,
                    stateVariables = Json(),
                    stateIndex = 0,
                )) { _ ->
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }.onError {
                    ToolsToast.show(t(API_TRANSLATE.app_error))
                }
            }
            .setOnCancel(t(API_TRANSLATE.app_no))
            .asSheetShow()
    }
}
