package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t

class CardPublicationUnknown(
        publication: Publication
) : CardPublication(R.layout.card_publication_unknown, publication) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vSystemMessage:TextView = view.findViewById(R.id.vSystemMessage)
        vSystemMessage.text = t(API_TRANSLATE.error_unknown)
    }

    override fun notifyItem() {

    }

    override fun updateComments() {
    }

    override fun updateFandom() {
    }

    override fun updateAccount() {
    }

    override fun updateKarma() {
    }

    override fun updateReports() {

    }

    override fun updateReactions() {

    }

}
