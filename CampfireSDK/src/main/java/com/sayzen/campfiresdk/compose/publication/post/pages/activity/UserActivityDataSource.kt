package com.sayzen.campfiresdk.compose.publication.post.pages.activity

import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesGetRelayRaceFullInfo
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceMemberStatusChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceRejected
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt

open class UserActivityDataSource(data: UserActivity) : BonfireDataSource<UserActivity>(data) {
    init {
        subscriber
            .subscribe(EventActivitiesRelayRaceRejected::class) {
                edit(it.userActivityId) {
                    tag_2 = it.currentOwnerTime
                    myMemberStatus = 0
                    currentAccount = it.currentAccount
                }
            }
            .subscribe(EventActivitiesRelayRaceMemberStatusChanged::class) {
                edit(it.userActivity) {
                    myMemberStatus = it.memberStatus
                    if (it.myIsCurrentMember) {
                        tag_2 = System.currentTimeMillis()
                        currentAccount = ControllerApi.account.getAccount()
                    }
                }
            }
            .subscribe(EventActivitiesChanged::class) {
                edit(it.userActivity.id) {
                    name = it.userActivity.name
                    description = it.userActivity.description
                }
            }
    }

    suspend fun reload() {
        try {
            val resp = RActivitiesGetRelayRaceFullInfo(data.id)
                .sendSuspendExt()
            _flow.emit(resp.userActivity)
        } catch (_: Exception) {
        }
    }

    private fun edit(id: Long, editor: UserActivity.() -> Unit) {
        edit(data.id == id, editor)
    }
}
