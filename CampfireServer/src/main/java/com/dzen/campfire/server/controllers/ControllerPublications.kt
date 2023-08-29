package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.publications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdmin
import com.dzen.campfire.api.models.publications.events_admins.PublicationEventAdmin
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandom
import com.dzen.campfire.api.models.publications.events_fandoms.PublicationEventFandom
import com.dzen.campfire.api.models.publications.events_moderators.ApiEventModer
import com.dzen.campfire.api.models.publications.events_moderators.PublicationEventModer
import com.dzen.campfire.api.models.publications.events_user.ApiEventUser
import com.dzen.campfire.api.models.publications.events_user.PublicationEventUser
import com.dzen.campfire.api.models.publications.moderations.Moderation
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.models.publications.post.PageText
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.tables.*
import com.sup.dev.java.classes.collections.AnyArray
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.text_format.TextFormatter
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.*


object ControllerPublications {

    fun getMaskText(publication: Publication): String {
        var text = ""
        when (publication) {
            is PublicationPost -> {
                for (p in publication.pages) {
                    if (p is PageText) {
                        text = p.text
                        break
                    }
                }
            }
            is PublicationComment -> {
                text =  publication.text
            }
            is PublicationChatMessage -> {
                text = publication.text
            }
            is QuestDetails -> {
                text = publication.title
            }
        }
        text = TextFormatter(text).parseNoTags()
        text = if (text.length < 25) text else text.substring(0, 25) + "..."
        return text
    }

    fun getMaskPageType(publication: Publication): Long {
        if (publication is PublicationPost) {
            if (publication.pages.isNotEmpty()) return publication.pages[0].getType()
        }
        if (publication is PublicationComment) {
            return publication.type
        }
        if (publication is PublicationChatMessage) {
            return publication.type
        }
        return 0
    }

    fun getModerationIdForPublication(publicationId: Long): Long? {
        return Database.select("ControllerPublications getModerationIdForPublication", SqlQuerySelect(TPublications.NAME, TPublications.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_MODERATION)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.tag_1, "=", API.MODERATION_TYPE_BLOCK)
                .where(TPublications.tag_4, "=", publicationId)
        ).nextMayNullOrNull()
    }

    fun clearReports(publicationId: Long) {
        Database.update("ControllerPublications.clearReports", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .update(TPublications.publication_reports_count, 0))

    }

    fun parseMentions(text: String, publicationId: Long, publicationType: Long, tag1: Long, tag2: Long, tag3: Long, fromAccount: ApiAccount, exclude: Array<Long>) {
        try {
            ControllerFandom.checkCan(fromAccount, API.LVL_CAN_MENTION)
        }catch (e:Exception){
            return
        }
        if(OptimizerEffects.get(fromAccount.id, API.EFFECT_INDEX_MENTION_LOCK) != null) return
        val textV = text.replace(API.LINK_PROFILE_NAME, API.LINK_SHORT_PROFILE)
        val names = ArrayList<String>()
        val ids = ArrayList<Long>()
        var i = 0
        while (i < textV.length) {
            if (textV[i] == API.LINK_SHORT_PROFILE[0]) {
                var ss = ""
                i++
                while (i < textV.length && ToolsText.isOnly(textV[i] + "", API.ACCOUNT_LOGIN_CHARS)) {
                    ss += textV[i]
                    i++
                }
                if (ss.length > 2) {
                    if (!names.contains(ss)) {
                        val id = Database.select("ControllerPublications.parseMentions", SqlQuerySelect(TAccounts.NAME, TAccounts.id).whereValue(TAccounts.name, "=", ss)).nextLongOrZero()
                        if (id > 0 && id != fromAccount.id && !exclude.contains(id)) {
                            if (! ControllerCollisions.checkCollisionExist(id, fromAccount.id, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)) {
                                names.add(ss)
                                ids.add(id)
                            }
                        }
                    }
                }
            }
            i++
        }

        if (ids.size > 0) {
            val n = NotificationMention(publicationId, publicationType, tag1, tag2, tag3, fromAccount.id, fromAccount.imageId, fromAccount.name, fromAccount.sex, textV)
            ControllerNotifications.push(ids.toTypedArray(), n)
        }

    }

    fun notifyFollowers(creatorId: Long, publicationId: Long) {
        val account = ControllerAccounts.getAccount(creatorId)!!
        notifyFollowers(account.id, account.name, account.imageId, account.sex, publicationId)
    }

    fun notifyFollowers(apiAccount: ApiAccount, publicationId: Long) {
        notifyFollowers(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publicationId)
    }

    fun notifyFollowers(creatorId: Long, creatorName: String, creatorImageId: Long, creatorSex: Long, publicationId: Long) {
        val collisions = ControllerCollisions.getCollisionsOwnerIds(creatorId, API.COLLISION_ACCOUNT_FOLLOW)
        val n = NotificationFollowsPublication(creatorImageId, publicationId, API.PUBLICATION_TYPE_POST, creatorId, creatorSex, creatorName)
        ControllerNotifications.push(collisions, n)

        Database.update("ControllerPublications.notifyFollowers", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publicationId).update(TPublications.tag_3, 1))
    }

    fun recountBestComment(publicationId: Long, removedId: Long? = null) {
        val publication = getPublication(publicationId, 1)
        if (publication == null) return

        if (removedId != null && publication.tag_1 != removedId) return

        val v = Database.select("ControllerPublications.recountBestComment", SqlQuerySelect(TPublications.NAME, TPublications.id, TPublications.karma_count)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
                .where(TPublications.parent_publication_id, "=", publicationId)
                .where(TPublications.karma_count, ">=", publication.karmaCount / 2)
                .where(TPublications.karma_count, ">", 0)
                .sort(TPublications.karma_count, false)
                .count(1)
        )
        if (v.isEmpty) {
            setBestComment(publicationId, 0, 0)
        } else {
            setBestComment(publicationId, v.next(), v.next())
        }
    }

    fun setBestComment(publicationId: Long, commentId: Long, karmaCount: Long) {
        Database.update("ControllerPublications.setBestComment", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .update(TPublications.tag_1, commentId)
                .update(TPublications.tag_2, karmaCount)
        )
    }

    fun loadSpecDataForPosts(accountId: Long, posts: Array<Publication>): Array<Publication> {
        loadBestCommentsForPosts(accountId, posts)
        loadRubricsForPosts(posts)
        loadUserActivity(accountId, posts)
        loadBlacklists(accountId, posts)
        return loadShadowBans(posts)
    }

    fun loadBlacklists(accountId: Long, pubs: Array<Publication>) {
        val v = Database.select("ControllerPublications.loadBlacklists",
            SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
                .where(TCollisions.owner_id, "=", accountId))
        if (v.isEmpty) return

        val ids = Array<Long>(v.rowsCount) { v.next() }

        for (pub in pubs) {
            if (ids.contains(pub.creator.id)) pub.blacklisted = true
        }
    }

    fun loadShadowBans(pubs: Array<Publication>): Array<Publication> {
        val v = Database.select(
            "ControllerPublications.loadShadowBans",
            SqlQuerySelect(TShadowBans.NAME, TShadowBans.account_id)
                .where(SqlWhere.WhereIN(TShadowBans.account_id, pubs.map { it.creator.id }.toTypedArray()))
        )

        val accounts = HashSet<Long>(v.rowsCount)
        while (v.hasNext()) {
            val accountId = v.next<Long>()
            accounts.add(accountId)
        }

        return pubs
            .filterNot { accounts.contains(it.creator.id) }
            .toTypedArray()
    }

    private fun loadUserActivity(accountId: Long, posts: Array<Publication>) {
        val list = ArrayList<PublicationPost>()
        val listIds = ArrayList<Long>()
        for (post in posts) {
            if (post is PublicationPost) {
                list.add(post)
                listIds.add(post.id)
            }
        }

        if (listIds.isEmpty()) return

        val v = Database.select("ControllerPublications.loadUserActivity",
                SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.tag_1, TActivitiesCollisions.activity_id)
                        .where(SqlWhere.WhereIN(TActivitiesCollisions.tag_1, listIds))
                        .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST)
        )

        while (v.hasNext()) {
            val id: Long = v.next()
            val userActivityId: Long = v.next()
            for (n in list) if (n.id == id) n.userActivity = ControllerActivities.getActivity(userActivityId, accountId)
        }

    }

    private fun loadRubricsForPosts(posts: Array<Publication>) {
        val list = ArrayList<PublicationPost>()
        val listIds = ArrayList<Long>()
        for (post in posts) if (post is PublicationPost && post.tag_6 > 0) {;list.add(post);listIds.add(post.tag_6); }

        if (listIds.isEmpty()) return

        val v = Database.select("ControllerPublications.loadRubricsForPosts", SqlQuerySelect(TRubrics.NAME, TRubrics.id, TRubrics.name).where(SqlWhere.WhereIN(TRubrics.id, listIds)))

        while (v.hasNext()) {
            val id: Long = v.next()
            val name: String = v.next()
            for (n in list)
                if (n.tag_6 == id) {
                    n.rubricId = id
                    n.rubricName = name
                }
        }

    }

    private fun loadBestCommentsForPosts(accountId: Long, posts: Array<Publication>) {
        val list = ArrayList<PublicationPost>()
        val listIds = ArrayList<Long>()
        for (post in posts) if (post is PublicationPost && post.tag_1 > 0) {;list.add(post);listIds.add(post.tag_1); }
        //for (post in posts) if (post is PublicationPost) {;post.tag_1 = 1006810;list.add(post);listIds.add(post.tag_1); }

        if (listIds.isEmpty()) return

        val publications = parseSelect(Database.select("ControllerPublications.loadSpecDataForPosts", instanceSelect(accountId).where(SqlWhere.WhereIN(TPublications.id, listIds))))

        for (i in publications) for (n in list) if (n.tag_1 == i.id) n.bestComment = i as PublicationComment?
    }

    fun watchComments(accountId: Long, publicationId: Long, watch: Boolean) {
        if (watch) {
            ControllerCollisions.putCollisionWithCheck(accountId, publicationId, API.COLLISION_COMMENTS_WATCH)
        } else {
            ControllerCollisions.removeCollisions(accountId, publicationId, API.COLLISION_COMMENTS_WATCH)
        }
    }

    fun event(event: ApiEventUser, accountId: Long) {
        val publicationEvent = PublicationEventUser(event)
        publicationEvent.tag_1 = event.getType()
        put(publicationEvent, accountId, 0, 0)
    }

    fun event(event: ApiEventModer, accountId: Long) {
        val publicationEvent = PublicationEventModer(event)
        publicationEvent.tag_1 = event.getType()
        put(publicationEvent, accountId, 0, 0)
    }

    fun event(event: ApiEventAdmin, accountId: Long) {
        val publicationEvent = PublicationEventAdmin(event)
        publicationEvent.tag_1 = event.getType()
        put(publicationEvent, accountId, 0, 0)
    }

    fun event(event: ApiEventFandom, accountId: Long, fandomId: Long, languageId: Long) {
        val publicationEvent = PublicationEventFandom(event)
        publicationEvent.tag_1 = event.getType()
        put(publicationEvent, accountId, fandomId, languageId)
    }

    fun moderation(event: Moderation, accountId: Long, fandomId: Long, languageId: Long, publicationId: Long): Long {
        val moderation = PublicationModeration()
        moderation.dateCreate = System.currentTimeMillis()
        moderation.publicationType = API.PUBLICATION_TYPE_MODERATION
        moderation.fandom.id = fandomId
        moderation.fandom.languageId = languageId
        moderation.moderation = event
        moderation.category = ControllerFandom.getCategory(fandomId)
        moderation.tag_1 = event.getType()
        moderation.tag_2 = 0
        moderation.tag_3 = 0
        moderation.tag_4 = publicationId
        moderation.creator = ControllerAccounts.instance(accountId, 0, 0, "", 0, 0, 0)
        val publicationId = put(moderation)
        watchComments(accountId, publicationId, true)
        return publicationId
    }

    fun getTags(accountId: Long, publicationId: Long): Array<PublicationTag> {
        val select = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
        select.where(TCollisions.collision_type, "=", API.COLLISION_TAG)
        select.where(TCollisions.owner_id, "=", publicationId)
        val v = Database.select("ControllerPublications.getTags select_1", select)
        var publicationsTagsMapped = arrayOfNulls<PublicationTag>(0)
        if (!v.isEmpty) {

            val tags = Array(v.rowsCount) { v.next<Long>() }
            val s = instanceSelect(accountId)
            s.where(SqlWhere.WhereIN(TPublications.id, tags))
            s.where(TPublications.status, "=", API.STATUS_PUBLIC)
            val publicationsTags = parseSelect(Database.select("ControllerPublications.getTags select_2", s))
            publicationsTagsMapped = arrayOfNulls(publicationsTags.size)
            for (i in publicationsTags.indices) publicationsTagsMapped[i] = publicationsTags[i] as PublicationTag
        }
        return ToolsMapper.asNonNull(publicationsTagsMapped)
    }

    fun put(publication: Publication, creatorId: Long, fandomId: Long, languageId: Long): Long {
        publication.fandom.id = fandomId
        publication.dateCreate = System.currentTimeMillis()
        publication.fandom.languageId = languageId
        publication.creator = ControllerAccounts.instance(creatorId, 0, 0, "", 0, 0, 0)
        publication.category = if (fandomId == 0L) 0L else ControllerFandom.getCategory(fandomId)
        return put(publication)
    }

    fun put(publication: Publication): Long {
        publication.id = Database.insert("ControllerPublications.put", TPublications.NAME,
                TPublications.publication_type, publication.publicationType,
                TPublications.fandom_id, publication.fandom.id,
                TPublications.language_id, publication.fandom.languageId,
                TPublications.date_create, publication.dateCreate,
                TPublications.creator_id, publication.creator.id,
                TPublications.parent_publication_id, publication.parentPublicationId,
                TPublications.publication_category, publication.category,
                TPublications.publication_json, publication.jsonDB(true, Json()),
                TPublications.status, API.STATUS_PUBLIC,
                TPublications.tag_1, publication.tag_1,
                TPublications.tag_2, publication.tag_2,
                TPublications.tag_3, publication.tag_3,
                TPublications.tag_4, publication.tag_4,
                TPublications.tag_s_1, publication.tag_s_1)
        return publication.id
    }

    fun remove(id: Long) {
        Database.remove("ControllerPublications.remove", SqlQueryRemove(TPublications.NAME)
                .where(TPublications.id, "=", id))
    }

    operator fun get(id: Long, vararg columns: String): AnyArray {
        return Database.select("ControllerPublications.get", SqlQuerySelect(TPublications.NAME, *columns)
                .where(TPublications.id, "=", id)).values
    }

    fun changeStatus(id: Long, status: Long) {
        Database.update("ControllerPublications.changeStatus", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", id)
                .update(TPublications.status, status))
    }

    fun getJson(id: Long): String? {
        return get(id, TPublications.publication_json).next<String>()
    }

    fun getCreatorId(id: Long): Long {
        return get(id, TPublications.creator_id).next()
    }

    fun getType(id: Long): Long {
        return get(id, TPublications.publication_type).next()
    }

    fun replaceJson(publicationId: Long, publication: Publication) {
        Database.update("ControllerPublications.replaceJson", SqlQueryUpdate(TPublications.NAME)
                .updateValue(TPublications.publication_json, publication.jsonDB(true, Json()))
                .where(TPublications.id, "=", publicationId))
    }

    fun checkExist(publicationId: Long, publicationType: Long): Boolean {
        return !Database.select("ControllerPublications.checkExist", SqlQuerySelect(TPublications.NAME, TPublications.id)
                .where(TPublications.id, "=", publicationId)
                .where(TPublications.publication_type, "=", publicationType)).isEmpty
    }

    fun checkType(id: Long, type: Long): Boolean {
        val v = get(id, TPublications.publication_type)
        return !v.isEmpty() && v.next<Any>() as Long == type
    }

    fun getPublication(publicationId: Long, accountId: Long): Publication? {
        val publications = parseSelect(Database.select("ControllerPublications.getPublication", instanceSelect(accountId)
                .where(TPublications.id, "=", publicationId)))
        return if (publications.size == 0) null else publications[0]
    }

    fun instanceSelect(accountId: Long): SqlQuerySelect {
        return SqlQuerySelect(TPublications.NAME,
                TPublications.publication_type,
                TPublications.id,
                TPublications.date_create,
                TPublications.parent_publication_id,
                TPublications.karma_count,
                if (accountId > 0) TPublications.my_karma(accountId) else 0,
                TPublications.closed,
                TPublications.publication_json,
                TPublications.publication_category,
                TPublications.status,
                TPublications.subpublications_count,
                TPublications.PARENT_PUBLICATION_TYPE,
                TPublications.publication_reports_count,
                TPublications.important,
                TPublications.tag_1,
                TPublications.tag_2,
                TPublications.tag_3,
                TPublications.tag_4,
                TPublications.tag_5,
                TPublications.tag_6,
                TPublications.tag_7,
                TPublications.tag_s_1,
                TPublications.creator_id,
                TPublications.CREATOR_LVL,
                TPublications.CREATOR_LAST_ONLINE_TIME,
                TPublications.CREATOR_NAME,
                TPublications.CREATOR_IMAGE_ID,
                TPublications.CREATOR_SEX,
                TPublications.CREATOR_KARMA_30,
                TPublications.fandom_id,
                TPublications.language_id,
                TPublications.FANDOM_NAME,
                TPublications.FANDOM_IMAGE_ID,
                TPublications.parent_fandom_closed,
                TPublications.FANDOM_KARMA_COF
        )
    }

    fun parseSelect(v: ResultRows) = Array(v.rowsCount) { parseSelectOne(v) }

    fun parseSelectOne(v: ResultRows): Publication {
        val publicationType = v.next<Long>()

        val publication = Publication.instance(publicationType)

        publication.publicationType = publicationType
        publication.id = v.next()
        publication.dateCreate = v.next()
        publication.parentPublicationId = v.next()
        publication.karmaCount = Sql.parseSum(v.next<Any>())
        publication.myKarma = v.next()
        publication.closed = (v.nextMayNull<Long>()?.toInt() ?: 0) != 0
        publication.jsonDB = Json(v.next<String>())
        publication.category = v.next()
        publication.status = v.next()
        publication.subPublicationsCount = v.next()
        publication.parentPublicationType = v.next()
        publication.reportsCount = v.next()
        publication.important = v.next()
        publication.tag_1 = v.next()
        publication.tag_2 = v.next()
        publication.tag_3 = v.next()
        publication.tag_4 = v.next()
        publication.tag_5 = v.next()
        publication.tag_6 = v.next()
        publication.tag_7 = v.next()
        publication.tag_s_1 = v.next()

        publication.creator = ControllerAccounts.instance(v)
        publication.fandom = Fandom(v.nextLongOrZero(), v.nextLongOrZero(), v.nextMayNull()
                ?: "", v.nextLongOrZero(), v.nextLongOrZero() == 1L, v.nextLongOrZero())

        publication.jsonDB(false, publication.jsonDB!!)

        return publication
    }

    fun getCount(fandomId: Long, languageId: Long, publicationType: Long): Long {
        val v = Database.select("ControllerPublications.getCount", SqlQuerySelect(TPublications.NAME, "COUNT(*)")
                .where(TPublications.fandom_id, "=", fandomId)
                .where(TPublications.language_id, "=", languageId)
                .where(TPublications.publication_type, "=", publicationType))
        return if (v.isEmpty) 0 else v.next()
    }

    //
    //  Collisions
    //

    fun removeCollisions(ownerId: Long, collisionType: Long) {
        removeCollisions(ownerId, 0, collisionType)
    }


    fun removeCollisions(ownerId: Long, collisionId: Long, collisionType: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.owner_id, "=", ownerId)
                .where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != 0L) remove.where(TCollisions.collision_id, "=", collisionId)
        Database.remove("ControllerPublications.removeCollisions", remove)
    }


    fun putCollisions(publicationId: Long, collisionIds: Array<Long>, collisionType: Long) {
        for (collisionId in collisionIds) putCollision(publicationId, collisionId, collisionType)

    }

    fun putCollisionWithCheck(publicationId: Long, collisionId: Long, collisionType: Long): Boolean {
        if (checkCollisionExist(publicationId, collisionId, collisionType)) return false
        putCollision(publicationId, collisionId, collisionType)
        return true
    }


    fun putCollision(publicationId: Long, collisionId: Long, collisionType: Long) {
        Database.insert("ControllerPublications putCollision", TCollisions.NAME,
                TCollisions.owner_id, publicationId,
                TCollisions.collision_type, collisionType,
                TCollisions.collision_id, collisionId,
                TCollisions.collision_date_create, System.currentTimeMillis())
    }


    fun updateOrCreateCollision(publicationId: Long, collisionId: Long, collisionType: Long, collisionDate: Long = System.currentTimeMillis()) {
        val collisionRecordId = getCollisionRecordId(publicationId, collisionId, collisionType)
        if (collisionRecordId == 0L)
            putCollision(publicationId, collisionId, collisionType)
        else
            updateCollision(collisionRecordId, publicationId, collisionId, collisionType, collisionDate)
    }


    fun updateCollision(id: Long, publicationId: Long, collisionId: Long, collisionType: Long, collisionDate: Long) {
        Database.update("ControllerPublications.updateCollision", SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.id, "=", id)
                .update(TCollisions.owner_id, publicationId)
                .update(TCollisions.collision_id, collisionId)
                .update(TCollisions.collision_type, collisionType)
                .update(TCollisions.collision_date_create, collisionDate))
    }

    fun getCollisionCount(publicationId: Long, collisionType: Long): Long {
        return Database.select("ControllerPublications.getCollisionCount_1", SqlQuerySelect(TCollisions.NAME, "COUNT(*)")
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType)).next()
    }

    fun getCollisionCount(publicationId: Long, collisionId: Long, collisionType: Long): Long {
        return Database.select("ControllerPublications.getCollisionCount_2", SqlQuerySelect(TCollisions.NAME, "COUNT(*)")
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId)).next()
    }

    fun getCollisionCountAnyPublicationId(collisionId: Long, collisionType: Long): Long {
        return Database.select("ControllerPublications.getCollisionCountAnyPublicationId", SqlQuerySelect(TCollisions.NAME, "COUNT(*)")
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId)).next()
    }

    fun getCollisionCountByCollisionId(collisionId: Long, collisionType: Long): Long {
        return Database.select("ControllerPublications.getCollisionCountByCollisionId", SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId)).next()
    }

    fun getCollisions(publicationId: Long, collisionType: Long): Array<Long> {
        val v = Database.select("ControllerPublications.getCollisions", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType))
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun checkCollisionExist(publicationId: Long, collisionId: Long, collisionType: Long): Boolean {
        return !Database.select("ControllerPublications.checkCollisionExist", SqlQuerySelect(TCollisions.NAME, TCollisions.id)
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId)).isEmpty
    }

    fun gtCollisionDate(publicationId: Long, collisionId: Long, collisionType: Long): Long {
        val select = Database.select("ControllerPublications.gtCollisionDate", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_date_create)
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId))
        return if (select.isEmpty) 0 else select.next()
    }

    fun getCollisionRecordId(publicationId: Long, collisionId: Long, collisionType: Long): Long {
        val v = Database.select("ControllerPublications.getCollisionRecordId", SqlQuerySelect(TCollisions.NAME, TCollisions.id)
                .where(TCollisions.owner_id, "=", publicationId)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_id, "=", collisionId))
        return if (v.isEmpty) 0 else v.next()
    }

}
