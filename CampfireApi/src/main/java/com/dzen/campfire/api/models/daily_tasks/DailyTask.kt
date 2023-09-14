package com.dzen.campfire.api.models.daily_tasks

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

// the better choice would be to create an abstract class
// and a separate type for each daily task type, but that
// makes this overly complicated.
// the source of truth for this is rust-bonfire/src/models/daily_task.rs
class DailyTask : JsonParsable {
    var type = DailyTaskType.Unknown
    var amount = 0L
    var fandomId = 0L
    var maxLevel = 0L
    var pageType = 0L

    override fun json(inp: Boolean, json: Json): Json {
        if (inp) {
            json.put("type", type.toString())
        } else {
            type = kotlin.runCatching {
                DailyTaskType.valueOf(json.getString("type"))
            }.getOrNull() ?: DailyTaskType.Unknown
        }
        amount = json.m(inp, "amount", amount)
        fandomId = json.m(inp, "fandom_id", fandomId)
        maxLevel = json.m(inp, "max_level", maxLevel)
        pageType = json.m(inp, "page_type", pageType)
        return json
    }
}
