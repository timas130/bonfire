package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.post.NotificationModerationPostTags
import com.dzen.campfire.api.models.publications.history.HistoryAdminChangeTags
import com.dzen.campfire.api.models.publications.history.HistoryChangeTags
import com.dzen.campfire.api.models.publications.history.HistoryPublish
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationPostTags
import com.dzen.campfire.api.models.publications.post.PageText
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostPublication
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate


class EPostPublication : RPostPublication(0, emptyArray(), "", false, 0, false, false, 0, 0, 0, false) {

    private var fandom: Fandom? = null
    private var publication: PublicationPost? = null
    private val tagsIds = ArrayList<Long>()

    override fun check() {
        if(userActivityNextUserId == apiAccount.id) userActivityNextUserId = 0
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.fandom.languageId == -1L) throw ApiException(API.ERROR_ACCESS)

        fandom = ControllerFandom.getFandom(publication!!.fandom.id)

        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(E_FANDOM_NOT_PUBLIC)

        if (publication!!.status != API.STATUS_DRAFT && publication!!.status != API.STATUS_PUBLIC && publication!!.status != API.STATUS_PENDING) throw ApiException(E_BAD_STATUS)
        if (publication!!.creator.id != apiAccount.id && publication!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if (publication!!.creator.id != apiAccount.id && pendingTime > 0) throw ApiException(API.ERROR_ACCESS)
        if (publication!!.creator.id != apiAccount.id) {
            comment = ControllerModeration.parseComment(comment, apiAccount.id)
            ControllerFandom.checkCan(apiAccount, publication!!.fandom.id, publication!!.fandom.languageId, API.LVL_MODERATOR_POST_TAGS)
        }
        if (publication!!.publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(E_BAD_TYPE)
        ControllerAccounts.checkAccountBanned(apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId)

        if (rubricId != 0L) {
            val rubric = ControllerRubrics.getRubric(rubricId)
            if (rubric == null || rubric.owner.id != apiAccount.id || rubric.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        }

        for (tagId in tags) {

            val tagV = ControllerPublications[tagId, TPublications.fandom_id, TPublications.language_id, TPublications.publication_type, TPublications.parent_publication_id, TPublications.status]

            val tagFandomId: Long = tagV.next()
            val tagLanguageId: Long = tagV.next()
            val tagPublicationType: Long = tagV.next()
            val tagParentPublicationId: Long = tagV.next()
            val tagStatus: Long = tagV.next()

            if (publication!!.fandom.id != tagFandomId
                    || publication!!.fandom.languageId != tagLanguageId
                    || tagPublicationType != API.PUBLICATION_TYPE_TAG
                    || tagParentPublicationId == 0L
                    || tagStatus != API.STATUS_PUBLIC)
                throw ApiException(E_BAD_TAGS)

            if (!tagsIds.contains(tagId)) tagsIds.add(tagId)
            if (!tagsIds.contains(tagParentPublicationId)) tagsIds.add(tagParentPublicationId)

        }


    }

    override fun execute(): Response {


        if (publication!!.creator.id != apiAccount.id) {

            val listNew = ArrayList<String>()
            val listRemoved = ArrayList<String>()
            val currentTags = ControllerPublications.getTags(apiAccount.id, publicationId)

            for (i in 0 until tags.size) {
                var founded = false
                for (n in 0 until currentTags.size)
                    if (tags[i] == currentTags[n].id) founded = true
                if (!founded) {
                    listNew.add((ControllerPublications.getPublication(tags[i], apiAccount.id)!! as PublicationTag).name)
                }
            }

            for (i in 0 until currentTags.size) {
                if (currentTags[i].parentPublicationId < 1) continue
                var b = false
                for (n in 0 until tags.size) b = b || currentTags[i].id == tags[n]
                if (!b) listRemoved.add(currentTags[i].name)
            }


            val moderationId = ControllerPublications.moderation(ModerationPostTags(publicationId, comment, listNew.toTypedArray(), listRemoved.toTypedArray()), apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.id)
            ControllerNotifications.push(publication!!.creator.id, NotificationModerationPostTags(apiAccount.id, apiAccount.name, apiAccount.sex, apiAccount.imageId, comment, moderationId))

            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_MODERATIONS_POST_TAGS)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_MODER_CHANGE_POST_TAGS)
            ControllerPublicationsHistory.put(publicationId, HistoryAdminChangeTags(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        }

        ControllerPublications.removeCollisions(publicationId, API.COLLISION_TAG)
        ControllerPublications.putCollisions(publicationId, ToolsMapper.asArray(tagsIds), API.COLLISION_TAG)

        ControllerAccounts.updatePostsCount(apiAccount.id, 1)

        if (publication!!.status == API.STATUS_DRAFT) {
            Database.update("EPostPublication", SqlQueryUpdate(TPublications.NAME)
                    .update(TPublications.status, if (pendingTime > 0 && pendingTime > System.currentTimeMillis()) API.STATUS_PENDING else API.STATUS_PUBLIC)
                    .update(TPublications.date_create, System.currentTimeMillis())
                    .update(TPublications.tag_4, pendingTime)
                    .update(TPublications.tag_6, rubricId)
                    .update(TPublications.closed, if (closed) 1 else 0)
                    .update(TPublications.nsfw, nsfw)
                    .update(TPublications.tag_3, if (pendingTime > 0L && notifyFollowers && publication!!.tag_3 == 0L) 2 else "${TPublications.NAME}.${TPublications.tag_3}")
                    .where(TPublications.id, "=", publicationId))

            if(multilingual) ControllerPost.setMultilingual(publication!!, true)

            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_POSTS_COUNT)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_FIRST_POST)

            ControllerPublications.watchComments(apiAccount.id, publicationId, true)
            ControllerPublicationsHistory.put(publicationId, HistoryPublish(apiAccount.id, apiAccount.imageId, apiAccount.name))

            ControllerAchievements.addAchievementWithCheck(
                ControllerViceroy.getViceroyId(publication!!.fandom.id, publication!!.fandom.languageId),
                API.ACHI_VICEROY_POSTS_COUNT
            )

            if (notifyFollowers && publication!!.tag_3 == 0L && pendingTime == 0L) ControllerPublications.notifyFollowers(apiAccount, publication!!.id)

            if (userActivityId > 0) {
                val activity = ControllerActivities.getActivity(userActivityId, apiAccount.id)
                if (activity != null && activity.currentAccount.id == apiAccount.id) {
                    ControllerActivities.setPost(publicationId, apiAccount.id, userActivityId)
                    if (userActivityNextUserId > 0) {
                        ControllerActivities.makeCurrentMember(apiAccount, userActivityNextUserId, activity)
                    } else {
                        ControllerActivities.recalculateMember(activity)
                    }
                    ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_POST)
                    ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RELAY_RACE_FIRST_POST)
                    ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RELAY_RACE_POSTS_COUNT)
                    ControllerAccounts.updateRelayRacePostsCount(apiAccount.id, 1)
                    if (activity.creatorId > 0) ControllerAccounts.updateRelayRaceMyRacePostsCount(activity.creatorId)
                    ControllerActivities.notifyFollowers(activity.id, publication!!.id, apiAccount.id)
                }
            }
        } else {
            if (publication!!.creator.id == apiAccount.id) ControllerPublicationsHistory.put(publicationId, HistoryChangeTags(apiAccount.id, apiAccount.imageId, apiAccount.name))
        }

        if (pendingTime == 0L) {
            for (page in publication!!.pages) {
                if (page !is PageText) continue
                ControllerPublications.parseMentions(
                    text = page.text,
                    publicationId = publication!!.id,
                    publicationType = publication!!.publicationType,
                    tag1 = 0,
                    tag2 = 0,
                    tag3 = 0,
                    fromAccount = apiAccount,
                    exclude = emptyArray()
                )
            }
        }

        return Response()
    }

}
