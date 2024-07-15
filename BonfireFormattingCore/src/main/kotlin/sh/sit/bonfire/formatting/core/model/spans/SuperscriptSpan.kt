package sh.sit.bonfire.formatting.core.model.spans

class SuperscriptSpan : Span() {
    override fun getTypeId(): Int = TYPE_SUPERSCRIPT
    override fun toString(): String {
        return "SuperscriptSpan() ${super.toString()}"
    }
}
