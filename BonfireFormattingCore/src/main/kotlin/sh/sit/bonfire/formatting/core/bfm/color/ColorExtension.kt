package sh.sit.bonfire.formatting.core.bfm.color

import org.commonmark.parser.Parser

class ColorExtension : Parser.ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(ColorDelimiterProcessor())
    }
}
