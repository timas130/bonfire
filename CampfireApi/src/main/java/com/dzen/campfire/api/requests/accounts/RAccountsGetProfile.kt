package com.dzen.campfire.api.requests.accounts

import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RAccountsGetProfile(var accountId: Long, var accountName: String) : Request<RAccountsGetProfile.Response>() {

    override fun jsonSub(inp: Boolean, json: Json) {
        accountId = json.m(inp, "accountId", accountId)
        accountName = json.m(inp, "accountName", accountName)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response(
        var dateCreate: Long = 0L,
        var pinnedPost: PublicationPost? = null,
        var banDate: Long = 0L,
        var status: String = "",
        var note: String = "",
        var isFollow: Boolean = false,
        var followsYou: Boolean = false,
        var followsCount: Long = 0L,
        var followersCount: Long = 0L,
        var age: Long = 0L,
        var description: String = "",
        var links: AccountLinks = AccountLinks(),
        @Deprecated("use ImageRefs")
        var titleImageId: Long = 0L,
        @Deprecated("use ImageRefs")
        var titleImageGifId: Long = 0L,
        var bansCount: Long = 0L,
        var warnsCount: Long = 0L,
        var karmaTotal: Long = 0L,
        var rates: Long = 0L,
        var ratesPositive: Long = 0L,
        var ratesNegative: Long = 0L,
        var moderationFandomsCount: Long = 0L,
        var subscribedFandomsCount: Long = 0L,
        var stickersCount: Long = 0L,
        var blackAccountsCount: Long = 0L,
        var blackFandomsCount: Long = 0L,
    ) : Request.Response() {
        var titleImageGif: ImageRef = ImageRef()
        var titleImage: ImageRef = ImageRef()

        constructor(json: Json) : this() {
            json(false, json)
        }

        override fun json(inp: Boolean, json: Json) {
            dateCreate = json.m(inp, "dateCreate", dateCreate)
            banDate = json.m(inp, "banDate", banDate)
            titleImage = json.m(inp, "titleImage", titleImage)
            titleImageId = json.m(inp, "titleImageId", titleImageId)
            titleImageGif = json.m(inp, "titleImageGif", titleImageGif)
            titleImageGifId = json.m(inp, "titleImageGifId", titleImageGifId)
            isFollow = json.m(inp, "isFollow", isFollow)
            followsYou = json.m(inp, "followsYou", followsYou)
            followsCount = json.m(inp, "followsCount", followsCount)
            followersCount = json.m(inp, "followersCount", followersCount)
            status = json.m(inp, "status", status)
            age = json.m(inp, "age", age)
            description = json.m(inp, "description", description)
            links = json.m(inp, "links", links, AccountLinks::class)
            note = json.m(inp, "note", note)
            pinnedPost = json.mNull(inp, "pinnedPost", pinnedPost, PublicationPost::class)
            bansCount = json.m(inp, "bansCount", bansCount)
            warnsCount = json.m(inp, "warnsCount", warnsCount)
            karmaTotal = json.m(inp, "karmaTotal", karmaTotal)
            rates = json.m(inp, "rates", rates)
            ratesPositive = json.m(inp, "ratesPositive", ratesPositive)
            ratesNegative = json.m(inp, "ratesNegative", ratesNegative)
            moderationFandomsCount = json.m(inp, "moderationFandomsCount", moderationFandomsCount)
            subscribedFandomsCount = json.m(inp, "subscribedFandomsCount", subscribedFandomsCount)
            stickersCount = json.m(inp, "stickersCount", stickersCount)
            blackAccountsCount = json.m(inp, "blackAccountsCount", blackAccountsCount)
            blackFandomsCount = json.m(inp, "blackFandomsCount", blackFandomsCount)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            pinnedPost?.fillImageRefs(receiver)
            receiver.add(titleImage, titleImageId)
            receiver.add(titleImageGif, titleImageGifId)
        }
    }


}
