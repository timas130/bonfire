package com.dzen.campfire.server.rust

import com.dzen.campfire.server.CheckImageMutation
import com.dzen.campfire.server.rust.ControllerRust.executeExt
import com.dzen.campfire.server.type.UploadType

object RustImages {
    fun check(userId: Long, key: String, uploadTypes: List<UploadType>): CheckImageMutation.CheckImage {
        return ControllerRust.apollo
            .mutation(CheckImageMutation(userId.toString(), key, uploadTypes))
            .executeExt()
            .checkImage
    }
}
