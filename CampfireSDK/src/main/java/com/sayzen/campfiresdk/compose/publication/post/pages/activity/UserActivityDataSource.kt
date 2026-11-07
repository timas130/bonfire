package com.sayzen.campfiresdk.compose.publication.post.pages.activity

import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesGetRelayRaceFullInfo
import com.sayzen.campfiresdk.compose.BonfireDataSource
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceMemberStatusChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceRejected
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceReloaded
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter.sendSuspendExt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class UserActivityDataSource(data: UserActivity) : BonfireDataSource<UserActivity>(data) {
    private val _removed = MutableStateFlow(false)
    val removed = _removed.asStateFlow()

    init {
        subscriber
            .subscribe(EventActivitiesRemove::class) {
                if (it.userActivityId == this.data.id) _removed.value = true
            }
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
                        tag_2 = ControllerApi.currentTime()
                        currentAccount = ControllerApi.account.getAccount()
                    }
                }
            }
            .subscribe(EventActivitiesRelayRaceReloaded::class) {
                edit(it.userActivity.id) {
                    tag_2 = it.userActivity.tag_2
                    currentAccount = it.userActivity.currentAccount
                    myPostId = it.userActivity.myPostId
                    myMemberStatus = it.userActivity.myMemberStatus
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
