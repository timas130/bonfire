package sh.sit.bonfire.formatting.core.model

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import sh.sit.bonfire.formatting.core.model.spans.Span

class FormattedText : JsonParsable {
    var text = ""
    var spans: Array<Span> = emptyArray()

    override fun json(inp: Boolean, json: Json): Json {
        text = json.m(inp, "text", text)
        spans = json.m(inp, "spans", spans)
        return json
    }
}
