package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

class ColorSpan(
    var colors: Array<Int>,
) : Span() {
    constructor() : this(emptyArray())

    override fun getTypeId(): Int = TYPE_COLOR

    override fun json(inp: Boolean, json: Json): Json {
        colors = json.m(inp, "colors", colors, Array<Int>::class)
        return super.json(inp, json)
    }

    override fun toString(): String {
        return "ColorSpan(colors=${colors.joinToString(", ") { it.toString(16) }}) ${super.toString()}"
    }
}
