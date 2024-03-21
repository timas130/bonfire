package com.dzen.campfire.api.requests.comments

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RCommentsCreate(
    var publicationId: Long,
    var text: String,
    var imageArray: Array<ByteArray>?,
    var gif: ByteArray?,
    var parentCommentId: Long,
    var watchPost: Boolean,
    var quoteId: Long,
    var stickerId: Long,
    var newFormatting: Boolean,
) : Request<RCommentsCreate.Response>() {
    companion object {
        const val E_BAD_TEXT_SIZE = "E_BAD_TEXT_SIZE"
        const val E_PARENT_COMMENT_DONT_EXIST = "E_PARENT_COMMENT_DONT_EXIST"
        const val E_BAD_PUBLICATION_TYPE = "E_BAD_PUBLICATION_TYPE"
        const val E_BAD_PUBLICATION_STATUS = "E_BAD_PUBLICATION_STATUS"
        const val E_BAD_PARENT_COMMENT_TYPE = "E_BAD_PARENT_COMMENT_TYPE"
        const val E_BAD_PARENT_COMMENT_TARGET_PUBLICATION_ID = "E_BAD_PARENT_COMMENT_TARGET_PUBLICATION_ID"
        const val E_BAD_IMAGE = "E_BAD_IMAGE"
        const val E_BAD_GIF = "E_BAD_GIF"
        const val E_BAD_DATA = "E_BAD_DATA"
    }

    init {
        addDataOutput(gif)
        if (imageArray != null) for (i in imageArray!!) addDataOutput(i)
    }

    override fun updateDataOutput() {
        gif = dataOutput[0]
        if (dataOutput.size > 1) {
            imageArray = Array(dataOutput.size - 1) { dataOutput[it + 1]!! }
        }
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        publicationId = json.m(inp, "unitId", publicationId)
        text = json.m(inp, "text", text)
        parentCommentId = json.m(inp, "parentCommentId", parentCommentId)
        watchPost = json.m(inp, "watchPost", watchPost)
        quoteId = json.m(inp, "quoteId", quoteId)
        stickerId = json.m(inp, "stickerId", stickerId)
        newFormatting = json.m(inp, "newFormatting", newFormatting)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var comment = PublicationComment()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(comment: PublicationComment) {
            this.comment = comment
        }

        override fun json(inp: Boolean, json: Json) {
            comment = json.m(inp, "comment", comment, PublicationComment::class)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            comment.fillImageRefs(receiver)
        }
    }

}
