package sh.sit.bonfire.formatting.core.model.spans

class BoldSpan : Span() {
    override fun getTypeId(): Int = TYPE_BOLD
    override fun toString(): String {
        return "BoldSpan() ${super.toString()}"
    }
}
