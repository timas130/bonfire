package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryPublish
import com.dzen.campfire.api.models.publications.post.PageText
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

object ControllerPending {

    fun start() {
        startAfterDelay()
    }

    private fun startAfterDelay() {
        ToolsThreads.thread(1000L * 60) { update() }
    }

    private fun update() {
        startAfterDelay()
        posts()
        ControllerActivities.checkForTimeouts()
        ControllerAlive.sendIfNeed()
    }

    private fun posts(){
        val v = Database.select("ControllerPending.posts select",
            SqlQuerySelect(
                TPublications.NAME,
                TPublications.id,
                TPublications.fandom_id,
                TPublications.language_id,
                TPublications.tag_3,
                TPublications.creator_id,
                TAccounts.NAME(TPublications.creator_id),
                TAccounts.IMAGE_ID(TPublications.creator_id)
            )
                .where(TPublications.status, "=", API.STATUS_PENDING)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                .where(TPublications.tag_4, "<", System.currentTimeMillis())
        )

        while (v.hasNext()){
            val id: Long = v.next()
            val fandomId: Long = v.next()
            val languageId: Long = v.next()
            val willNotify: Long = v.next()
            val creatorId: Long = v.next()
            val creatorName: String = v.next()
            val creatorImageId: Long = v.next()

            try {
                val pub = ControllerPublications.getPublication(id, 0)!! as PublicationPost

                for (page in pub.pages) {
                    if (page !is PageText) continue
                    ControllerPublications.parseMentions(
                        text = page.text,
                        publicationId = pub.id,
                        publicationType = pub.publicationType,
                        tag1 = 0,
                        tag2 = 0,
                        tag3 = 0,
                        fromAccount = ApiAccount(pub.creator),
                        exclude = emptyArray()
                    )
                }
            } catch (_: Exception) {}

            try {
                ControllerAccounts.checkAccountBanned(creatorId, fandomId, languageId)
                ControllerPost.publish(id, willNotify, creatorId)

                ControllerPublicationsHistory.put(
                    id,
                    HistoryPublish(creatorId, creatorImageId, creatorName)
                )
            } catch (e: ApiException) {
                Database.update("ControllerPending.posts update_2", SqlQueryUpdate(TPublications.NAME)
                        .where(TPublications.id, "=", id)
                        .update(TPublications.status, API.STATUS_DRAFT)
                        .update(TPublications.date_create, TPublications.tag_4)
                        .update(TPublications.tag_3, 0)
                        .update(TPublications.tag_4, 0)
                )
            }
        }
    }

}
