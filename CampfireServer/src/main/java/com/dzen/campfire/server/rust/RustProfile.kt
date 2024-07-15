package com.dzen.campfire.server.rust

import com.dzen.campfire.api.models.account.AccountBadge
import com.dzen.campfire.api.models.account.AccountCustomization
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.server.AccountCustomizationQuery
import com.dzen.campfire.server.CanSeeNsfwQuery
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.sup.dev.java.classes.collections.Cache

object RustProfile {
    private val accountCustomizationCache = Cache<Long, AccountCustomization>(10000)

    fun getAccountCustomization(userId: Long): AccountCustomization {
        accountCustomizationCache[userId]?.let { return it }

        val raw = ControllerRust.apollo.query(AccountCustomizationQuery(userId.toInt()))
            .executeExt()
            .internalAccountCustomization

        val customization = AccountCustomization().apply {
            nicknameColor = raw.nicknameColor?.int
            activeBadge = raw.activeBadge?.let { badge ->
                AccountBadge().apply {
                    id = badge.id.toLong()
                    miniImage = ImageRef(badge.miniImage.i.toLong())
                }
            }
        }

        accountCustomizationCache.put(userId, customization)

        return customization
    }

    fun canSeeNsfw(userId: Long): Boolean? {
        return ControllerRust.apollo.query(CanSeeNsfwQuery(userId.toString()))
            .executeExt()
            .userById
            ?.canNsfw
    }
}
