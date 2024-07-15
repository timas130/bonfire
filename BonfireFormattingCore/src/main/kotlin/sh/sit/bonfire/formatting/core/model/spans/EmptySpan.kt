package sh.sit.bonfire.formatting.core.model.spans

class EmptySpan : Span() {
    override fun getTypeId(): Int = TYPE_EMPTY

    override fun toString(): String {
        return "EmptySpan() ${super.toString()}"
    }
}
