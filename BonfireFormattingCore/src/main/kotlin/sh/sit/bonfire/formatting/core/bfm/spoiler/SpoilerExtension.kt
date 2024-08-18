package sh.sit.bonfire.formatting.core.bfm.spoiler

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class SpoilerExtension : ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(SpoilerDelimiterProcessor())
    }
}
