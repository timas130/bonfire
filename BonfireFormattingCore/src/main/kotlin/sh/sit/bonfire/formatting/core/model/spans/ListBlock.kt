package sh.sit.bonfire.formatting.core.model.spans

// this span only indents the contents.
// numbering/marking is handled by ListItemSpan
class ListBlock : Span() {
    override fun getTypeId(): Int = TYPE_LIST
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "ListSpan() ${super.toString()}"
    }
}
