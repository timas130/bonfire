package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteFandomRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomRemove
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomRemove
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class PAdminVoteFandomRemove {

    fun accept(m: MAdminVoteFandomRemove){

        Database.update("EFandomsAdminRemove.blockFandom", SqlQueryUpdate(TFandoms.NAME)
            .where(TFandoms.id, "=", m.targetFandom.id)
            .update(TFandoms.status, API.STATUS_BLOCKED))

        Database.update("EFandomsAdminRemove.blockPublications", SqlQueryUpdate(TPublications.NAME)
            .where(TPublications.fandom_id, "=", m.targetFandom.id)
            .where(TPublications.status, "<>", API.STATUS_DEEP_BLOCKED)
            .update(TPublications.status, API.STATUS_BLOCKED))

        Database.remove("EFandomsAdminRemove", SqlQueryRemove(TCollisions.NAME)
            .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
            .where(TCollisions.collision_id, "=", m.targetFandom.id))


        ControllerPublications.event(ApiEventAdminFandomRemove(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment, m.targetFandom.name), m.adminAccount.id)
        ControllerPublications.event(ApiEventFandomRemove(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetFandom.id, m.targetFandom.name, m.targetFandom.imageId, m.comment), m.adminAccount.id, m.targetFandom.id, 0)
    }

}
