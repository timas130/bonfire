package com.dzen.campfire.api.models.project

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class ProjectEvent : JsonParsable {
    var id = ""
    var title = ""
    var description = ""
    var progressCurrent = 0
    var progressMax = 1
    var url: String? = null

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        title = json.m(inp, "title", title)
        description = json.m(inp, "description", description)
        progressCurrent = json.m(inp, "progressCurrent", progressCurrent)
        progressMax = json.m(inp, "progressMax", progressMax)
        url = json.mNull(inp, "url", url, String::class)
        return json
    }
}
