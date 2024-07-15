package sh.sit.bonfire.formatting.core.bfm.marked

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class MarkedExtension : ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(MarkedDelimiterProcessor())
    }
}
