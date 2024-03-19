package com.dzen.campfire.server.rust

import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.ChangeNameMutation
import com.dzen.campfire.server.ShortMeQuery
import com.dzen.campfire.server.fragment.ShortUser
import com.dzen.campfire.server.rust.ControllerRust.executeExt

object RustAuth {
    fun getByToken(accessToken: String): ShortUser? {
        return try {
            ControllerRust.apollo
                .query(ShortMeQuery())
                .addHttpHeader("Authorization", "Bearer $accessToken")
                .executeExt()
                .me
                .shortUser
        } catch (e: ApiException) {
            null
        }
    }

    fun changeName(userId: Long, newName: String): ShortUser {
        return ControllerRust.apollo
            .mutation(ChangeNameMutation(userId.toString(), newName))
            .executeExt()
            .internalChangeName
            .shortUser
    }
}
