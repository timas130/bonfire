package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

class LinkSpan(var link: String) : Span() {
    constructor() : this("")

    override fun json(inp: Boolean, json: Json): Json {
        link = json.m(inp, "link", link)
        return super.json(inp, json)
    }

    override fun getTypeId(): Int = TYPE_LINK

    override fun toString(): String {
        return "LinkSpan(link='$link') ${super.toString()}"
    }
}
