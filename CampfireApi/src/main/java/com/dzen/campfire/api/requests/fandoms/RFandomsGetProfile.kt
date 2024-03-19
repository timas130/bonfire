package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsGetProfile(
        var fandomId: Long,
        var languageId: Long
) : Request<RFandomsGetProfile.Response>() {

    init {
        cashAvailable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        languageId = json.m(inp, "languageId", languageId)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var viceroyAccount: Account? = null
        var viceroyDate = 0L
        var pinnedPost: PublicationPost? = null
        var subscriptionType = 0L
        var notifyImportant = false
        var imageTitle = ImageRef()
        @Deprecated("use ImageRefs")
        var imageTitleId = 0L
        var imageTitleGif = ImageRef()
        @Deprecated("use ImageRefs")
        var imageTitleGifId = 0L


        constructor(json: Json) {
            json(false, json)
        }

        constructor(viceroyAccount:Account?,
                    viceroyDate:Long,
                    pinnedPost: PublicationPost?,
                    subscriptionType: Long,
                    notifyImportant: Boolean,
                    imageTitle: Long,
                    imageTitleGif: Long,
        ) {
            this.viceroyAccount = viceroyAccount
            this.viceroyDate = viceroyDate
            this.pinnedPost = pinnedPost
            this.subscriptionType = subscriptionType
            this.notifyImportant = notifyImportant
            this.imageTitle = ImageRef(imageTitle)
            this.imageTitleGif = ImageRef(imageTitleGif)
        }

        override fun json(inp: Boolean, json: Json) {
            viceroyAccount = json.mNull(inp, "viceroyAccount", viceroyAccount, Account::class)
            viceroyDate = json.m(inp, "viceroyDate", viceroyDate)
            pinnedPost = json.mNull(inp, "pinnedPost", pinnedPost, PublicationPost::class)
            subscriptionType = json.m(inp, "subscriptionType", subscriptionType)
            notifyImportant = json.m(inp, "notifyImportant", notifyImportant)
            imageTitle = json.m(inp, "imageTitle", imageTitle)
            imageTitleId = json.m(inp, "imageTitleId", imageTitleId)
            imageTitleGif = json.m(inp, "imageTitleGif", imageTitleGif)
            imageTitleGifId = json.m(inp, "imageTitleGifId", imageTitleGifId)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            viceroyAccount?.fillImageRefs(receiver)
            pinnedPost?.fillImageRefs(receiver)
        }
    }


}
