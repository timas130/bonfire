package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException

object ControllerModeration {
    fun parseComment(comment:String):String{
        val com = ControllerCensor.cens(comment)
        if (comment.length < API.MODERATION_COMMENT_MIN_L || comment.length > API.MODERATION_COMMENT_MAX_L) throw ApiException(API.ERROR_BAD_COMMENT)
        return com
    }
}
