package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json

// this span only numbers/marks the list items.
// all items are indented by ListSpan
class ListItemBlock(
    var ordered: Boolean,
    var startNumber: Int,
) : Span() {
    constructor() : this(false, 0)

    override fun getTypeId(): Int = TYPE_LIST_ITEM
    override fun isBlockSpan(): Boolean = true

    override fun json(inp: Boolean, json: Json): Json {
        ordered = json.m(inp, "ordered", ordered)
        startNumber = json.m(inp, "startNumber", startNumber)
        return super.json(inp, json)
    }

    override fun toString(): String {
        return "ListItemSpan(ordered=$ordered, startNumber=$startNumber) ${super.toString()}"
    }
}
