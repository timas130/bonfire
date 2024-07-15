package sh.sit.bonfire.formatting.core.model.spans

class SubscriptSpan : Span() {
    override fun getTypeId(): Int = TYPE_SUBSCRIPT
    override fun toString(): String {
        return "SubscriptSpan() ${super.toString()}"
    }
}
