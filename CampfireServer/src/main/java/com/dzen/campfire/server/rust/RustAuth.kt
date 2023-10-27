package com.dzen.campfire.server.rust

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object RustAuth {
    @Serializable
    private data class ShortMeResponse(
        val me: AuthUser,
    )

    @Serializable
    data class AuthUser(
        val id: String,
        val username: String,
    )

    fun getByToken(accessToken: String): AuthUser? {
        try {
            val resp = ControllerRust.query<ShortMeResponse>(
                """
                    query ShortMe {
                        me {
                            id
                            username
                        }
                    }
                """.trimIndent(),
                buildJsonObject {},
                accessToken,
            )
            return resp.me
        } catch (e: Exception) {
            return null
        }
    }

    @Serializable
    private data class ChangeNameResponse(
        val internalChangeName: AuthUser,
    )

    fun changeName(userId: Long, newName: String): AuthUser {
        val resp = ControllerRust.queryService<ChangeNameResponse>(
            """
                mutation ChangeNameMutation(${"$"}userId: ID!, ${"$"}newName: String!) {
                    internalChangeName(userId: ${"$"}userId, newName: ${"$"}newName) {
                        id
                        username
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("userId", userId.toString())
                put("newName", newName)
            },
        )
        return resp.internalChangeName
    }
}
