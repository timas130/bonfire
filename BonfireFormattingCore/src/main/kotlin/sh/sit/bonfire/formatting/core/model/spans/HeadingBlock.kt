package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

class HeadingBlock(var level: Int) : Span() {
    constructor() : this(0)

    override fun getTypeId(): Int = TYPE_HEADING
    override fun isBlockSpan(): Boolean = true

    override fun json(inp: Boolean, json: Json): Json {
        level = json.m(inp, "level", level)
        return super.json(inp, json)
    }

    override fun toString(): String {
        return "HeadingSpan(level=$level) ${super.toString()}"
    }
}
