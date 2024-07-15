package sh.sit.bonfire.formatting.core.model.spans

class ThematicBreakBlock : Span() {
    override fun getTypeId(): Int = TYPE_THEMATIC_BREAK
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "ThematicBreakSpan() ${super.toString()}"
    }
}
