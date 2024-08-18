package sh.sit.bonfire.formatting.core.model.spans

class SpoilerSpan : Span() {
    override fun getTypeId(): Int = TYPE_SPOILER
    override fun toString(): String {
        return "SpoilerSpan() ${super.toString()}"
    }
}
