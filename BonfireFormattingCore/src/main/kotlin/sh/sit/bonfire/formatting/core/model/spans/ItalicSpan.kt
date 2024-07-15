package sh.sit.bonfire.formatting.core.model.spans

class ItalicSpan : Span() {
    override fun getTypeId(): Int = TYPE_ITALIC
    override fun toString(): String {
        return "ItalicSpan() ${super.toString()}"
    }
}
