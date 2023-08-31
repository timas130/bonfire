package com.dzen.campfire.server.rust

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

object RustAchievements {
    class LevelRecountReport : JsonParsable {
        var userId: Long = 0
        var totalLevel: Long = 0
        var achievements: HashMap<Long, AchievementRecountReport> = hashMapOf()

        override fun json(inp: Boolean, json: Json): Json {
            userId = json.m(inp, "user_id", userId)
            totalLevel = json.m(inp, "total_level", totalLevel)
            if (inp) {
                val out = Json()
                for ((k, v) in achievements) {
                    out.put(k.toString(), v)
                }
                json.put("achievements", out)
            } else {
                val achievementsJson = json.getJson("achievements")!!
                achievements.clear()
                for (k in achievementsJson.getKeys()) {
                    achievements[(k as String).toLong()] =
                        achievementsJson.getJsonParsable(k, AchievementRecountReport::class)!!
                }
            }
            return json
        }
    }

    class AchievementRecountReport : JsonParsable {
        var id: Long = 0
        var count: Long = 0
        var target: Long? = null
        var level: Long = 0

        override fun json(inp: Boolean, json: Json): Json {
            id = json.m(inp, "id", id)
            count = json.m(inp, "count", count)
            if (inp) {
                if (target == null) {
                    json.put("target", null as String?)
                } else {
                    json.put("target", target!!)
                }
            } else {
                target = json.getLongNull("target")
            }
            level = json.m(inp, "level", level)
            return json
        }
    }

    fun getForUser(userId: Long): LevelRecountReport {
        return LevelRecountReport().apply {
            json(false, ControllerRust.get("/user/$userId/recount-level"))
        }
    }
}
