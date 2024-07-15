package sh.sit.bonfire.formatting.core.bfm.underline

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class UnderlineExtension : ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(UnderlineDelimiterProcessor())
    }
}
