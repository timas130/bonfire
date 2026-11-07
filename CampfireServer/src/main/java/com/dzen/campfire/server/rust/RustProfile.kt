package com.dzen.campfire.server.rust

import com.dzen.campfire.api.models.account.AccountBadge
import com.dzen.campfire.api.models.account.AccountCustomization
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.server.AccountCustomizationQuery
import com.dzen.campfire.server.CanSeeNsfwQuery
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.sup.dev.java.classes.collections.Cache

object RustProfile {
    private const val ACCOUNT_CUSTOMIZATION_TTL = 60_000L

    private class CachedCustomization(val customization: AccountCustomization, val loadedAt: Long)

    private val accountCustomizationCache = Cache<Long, CachedCustomization>(10000)

    fun getAccountCustomization(userId: Long): AccountCustomization {
        val cached = accountCustomizationCache[userId]
        if (cached != null && System.currentTimeMillis() - cached.loadedAt < ACCOUNT_CUSTOMIZATION_TTL) return cached.customization

        val raw = try {
            ControllerRust.apollo.query(AccountCustomizationQuery(userId.toInt()))
                .executeExt()
                .internalAccountCustomization
        } catch (e: Exception) {
            if (cached == null) throw e
            accountCustomizationCache.put(userId, CachedCustomization(cached.customization, System.currentTimeMillis()))
            return cached.customization
        }

        val customization = AccountCustomization().apply {
            nicknameColor = raw.nicknameColor?.int
            activeBadge = raw.activeBadge?.let { badge ->
                AccountBadge().apply {
                    id = badge.id.toLong()
                    miniImage = ImageRef(badge.miniImage.i.toLong())
                }
            }
        }

        accountCustomizationCache.put(userId, CachedCustomization(customization, System.currentTimeMillis()))

        return customization
    }

    fun canSeeNsfw(userId: Long): Boolean? {
        return ControllerRust.apollo.query(CanSeeNsfwQuery(userId.toString()))
            .executeExt()
            .userById
            ?.canNsfw
    }
}
