package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

class CodeBlock(var language: String) : Span() {
    constructor() : this("")

    override fun json(inp: Boolean, json: Json): Json {
        language = json.m(inp, "language", language)
        return super.json(inp, json)
    }

    override fun getTypeId(): Int = TYPE_CODE_BLOCK
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "CodeBlock(language='$language') ${super.toString()}"
    }
}
