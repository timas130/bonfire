package sh.sit.bonfire.auth

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

data class DateTime(val inner: DateTime) {
    val millis: Long
        get() = inner.millis

    companion object {
        @JvmStatic
        val adapter = object : Adapter<sh.sit.bonfire.auth.DateTime> {
            override fun fromJson(
                reader: JsonReader,
                customScalarAdapters: CustomScalarAdapters
            ): sh.sit.bonfire.auth.DateTime {
                return sh.sit.bonfire.auth.DateTime(
                    DateTime(ISODateTimeFormat.dateTime().parseDateTime(reader.nextString()!!))
                )
            }

            override fun toJson(
                writer: JsonWriter,
                customScalarAdapters: CustomScalarAdapters,
                value: sh.sit.bonfire.auth.DateTime
            ) {
                writer.value(ISODateTimeFormat.dateTime().print(value.inner))
            }
        }
    }
}
