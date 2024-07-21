package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

class TaskItemMarker(var checked: Boolean) : Span() {
    constructor() : this(false)

    override fun json(inp: Boolean, json: Json): Json {
        checked = json.m(inp, "checked", checked)
        return super.json(inp, json)
    }

    override fun getTypeId(): Int = TYPE_TASK_ITEM_MARKER
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "TaskItemMarker() ${super.toString()}"
    }
}
