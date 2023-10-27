package com.dzen.campfire.api.models.daily_tasks

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

// the source of truth for this is
// rust-bonfire/src/routes/daily_task/task.rs
class DailyTaskInfo : JsonParsable {
    var task = DailyTask()
    var progress = 0L
    var totalLevels = 0L
    var levelMultiplier = 0F
    var comboMultiplier = 0F
    var possibleReward = 0L
    var fandomName: String? = ""
    var total = 1L

    override fun json(inp: Boolean, json: Json): Json {
        task = json.m(inp, "task", task)
        progress = json.m(inp, "progress", progress)
        totalLevels = json.m(inp, "total_levels", totalLevels)
        levelMultiplier = json.m(inp, "level_multiplier", levelMultiplier)
        comboMultiplier = json.m(inp, "combo_multiplier", comboMultiplier)
        possibleReward = json.m(inp, "possible_reward", possibleReward)
        fandomName = json.mNull(inp, "fandom_name", fandomName, String::class)
        total = json.m(inp, "total", total)
        return json
    }
}
