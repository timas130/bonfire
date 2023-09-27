package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.requests.project.RProjectGetLoadingPictures
import com.dzen.campfire.server.tables.TLoadingPictures
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectGetLoadingPictures : RProjectGetLoadingPictures() {
    override fun check() {
    }

    override fun execute(): Response {
        val v = Database.select(
            "EProjectGetLoadingPictures",
            SqlQuerySelect(
                TLoadingPictures.NAME,
                TLoadingPictures.start_time,
                TLoadingPictures.end_time,
                TLoadingPictures.image_id,
                TLoadingPictures.title_text,
                TLoadingPictures.subtitle_text
            ).sort(TLoadingPictures.order, true)
        )

        return Response(Array(v.rowsCount) {
            LoadingPicture(
                startTime = v.next(),
                endTime = v.next(),
                imageId = v.next(),
                titleTranslation = v.next(),
                subtitleTranslation = v.next(),
            )
        })
    }
}
