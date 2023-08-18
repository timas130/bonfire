package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.requests.stickers.RStickersPackChangeAvatar

class EStickersPackChangeAvatar : RStickersPackChangeAvatar(0, null) {

    override fun check() {
    }

    override fun execute(): Response {
        return Response()
    }
}