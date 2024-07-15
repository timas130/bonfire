package sh.sit.bonfire.formatting.core.bfm.mention

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class MentionExtension : ParserExtension {
    override fun extend(parser: Parser.Builder) {
        parser.postProcessor(MentionPostProcessor())
    }
}
