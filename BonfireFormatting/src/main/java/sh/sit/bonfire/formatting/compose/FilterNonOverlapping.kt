package sh.sit.bonfire.formatting.compose

import sh.sit.bonfire.formatting.core.model.spans.Span

internal fun List<Span>.filterNonOverlapping(): List<Span> {
    val result = mutableListOf<Span>()

    var prev: Span? = null
    for (block in asReversed()) {
        if (prev != null && block.start >= prev.start && block.end <= prev.end) {
            continue
        }

        result.add(block)
        prev = block
    }

    return result.asReversed()
}
