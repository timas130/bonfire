package com.dzen.campfire.server.executors.stickers

import com.dzen.campfire.api.requests.stickers.RStickersPackRename

class EStickersPackRename : RStickersPackRename(0, "") {

    override fun check() {
    }

    override fun execute(): Response {
        return Response()
    }
}