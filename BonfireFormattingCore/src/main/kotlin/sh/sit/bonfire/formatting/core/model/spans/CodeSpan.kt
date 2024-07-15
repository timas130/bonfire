package sh.sit.bonfire.formatting.core.model.spans

class CodeSpan : Span() {
    override fun getTypeId(): Int = TYPE_CODE
    override fun toString(): String {
        return "CodeSpan() ${super.toString()}"
    }
}
