package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsKarmaAdd
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import kotlin.math.roundToLong;


class EPublicationsKarmaAdd : RPublicationsKarmaAdd(0, false, 0, false) {

    private var publication: Publication? = null

    @Throws(ApiException::class)
    override fun check() {

        if(botToken != null) throw ApiException(API.ERROR_ACCESS)

        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)

        if (publication == null) throw ApiException(API.ERROR_GONE)

        if (publication!!.publicationType != API.PUBLICATION_TYPE_POST
                && publication!!.publicationType != API.PUBLICATION_TYPE_COMMENT
                && publication!!.publicationType != API.PUBLICATION_TYPE_MODERATION
                && publication!!.publicationType != API.PUBLICATION_TYPE_STICKERS_PACK
                && publication!!.publicationType != API.PUBLICATION_TYPE_QUEST
        ) throw ApiException(E_BAD_TYPE)

        if (publication!!.myKarma != 0L) throw ApiException(E_ALREADY_EXIST)
        if (apiAccount.id == publication!!.creator.id) throw ApiException(E_SELF_PUBLICATION)

        ControllerAccounts.checkAccountBanned(apiAccount.id)

        if (anon) {
            try {
                ControllerFandom.checkCan(apiAccount, API.LVL_ANONYMOUS)
            } catch (e: Exception) {
                anon = false
            }
        }

    }

    override fun execute(): Response {
        val doubleKarmaStart = 1692738000000L
        val doubleKarmaEnd = doubleKarmaStart + 1000L * 3600L * 24L * 4L
        val isGlobalDoubleKarma = System.currentTimeMillis() in doubleKarmaStart..doubleKarmaEnd

        val rubricKarmaCof = if (publication!!.publicationType == API.PUBLICATION_TYPE_POST && publication!!.tag_6 > 0) ControllerOptimizer.getRubricKarmaCof(publication!!.tag_6) else 100
        val fandomKarmaCof = if (publication!!.fandom.id > 0) ControllerOptimizer.getFandomKarmaCof(publication!!.fandom.id) else 100
        val questKarmaCof  = if (publication!!.publicationType == API.PUBLICATION_TYPE_QUEST) 150 else 100
        val karmaCof = ((fandomKarmaCof / 100f) * (rubricKarmaCof / 100f) * (questKarmaCof / 100f) * (if (isGlobalDoubleKarma) 2 else 1) * 100).roundToLong()

        val karmaForceD = ControllerKarma.getKarmaForce(apiAccount, up) * (karmaCof / 100f)
        val karmaForce = if (karmaForceD > 0) {
            if (karmaForceD < 100) 100
            else karmaForceD.toLong()
        } else {
            if (karmaForceD > -100) -100
            else karmaForceD.toLong()
        }

        val canChangeKarma = ControllerKarma.canChangeKarma(apiAccount.id, publication!!.creator.id)
        if (publication!!.fandom.languageId == -1L) publication!!.fandom.languageId = userLanguage

        if (karmaForce < 0 && !ControllerHaters.canKarmaDown(apiAccount.id)) throw ApiException(E_CANT_DOWN)
        if (karmaForce < 0) ControllerHaters.addDown(apiAccount.id)

        synchronized(App.accountProvider) {
            val added = ControllerKarma.addKarmaTransaction(apiAccount, karmaForce, karmaCof, canChangeKarma, publication!!.creator.id, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.id, anon)
            if (!added) throw ApiException(E_ALREADY_EXIST)
        }

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RATE)

        ControllerSubThread.inSub("EPublicationsKarmaAdd [1]") {
            ControllerKarma.onKarmaTransactionAdded(apiAccount, karmaForce, canChangeKarma, publication!!, anon)
            publication!!.karmaCount += karmaForce
            if (up && publication!!.publicationType == API.PUBLICATION_TYPE_COMMENT && publication!!.parentPublicationType == API.PUBLICATION_TYPE_POST) {
                val publicationParent = ControllerPublications.getPublication(publication!!.parentPublicationId, apiAccount.id)
                if (publicationParent!!.tag_1 != publication!!.id) {
                    if (publicationParent.tag_2 < publication!!.karmaCount
                            && publication!!.karmaCount >= publicationParent.karmaCount / 2
                            && publication!!.karmaCount > 0) {
                        ControllerPublications.setBestComment(publicationParent.id, publication!!.id, publication!!.karmaCount)
                    }
                } else {
                    Database.update("EPublicationsKarmaAdd", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publicationParent.id).update(TPublications.tag_2, publication!!.karmaCount))
                }
            }
        }

        return Response(karmaForce)
    }


}
