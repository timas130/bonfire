package com.dzen.campfire.api.models.project

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class ProjectEvent : JsonParsable {
    var title = ""
    var description = ""
    var progressCurrent = 0
    var progressMax = 1

    override fun json(inp: Boolean, json: Json): Json {
        title = json.m(inp, "title", title)
        description = json.m(inp, "description", description)
        progressCurrent = json.m(inp, "progressCurrent", progressCurrent)
        progressMax = json.m(inp, "progressMax", progressMax)
        return json
    }
}
