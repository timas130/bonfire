package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.requests.publications.RPublicationsReactionGetAccounts
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.views.SplashCards
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card

object ControllerReactions {

    fun showAccounts(publicationId:Long, reactionIndex:Long, view: View){

        val widgetCards = SplashCards()
        widgetCards.asPopupShow(view)

        ApiRequestsSupporter.execute(RPublicationsReactionGetAccounts(publicationId, reactionIndex)){r->
            if(r.accounts.isEmpty()){
                widgetCards.hide()
            }else {
                widgetCards.addCards(*Array<Card>(r.accounts.size){
                    val card = CardAccount(r.accounts[it])
                    card.setAvatarSize(ToolsView.dpToPx(32).toInt())
                    card.setShowLvl(false)
                    card
                })
            }
        }.onError {
            widgetCards.hide()
        }

    }

}