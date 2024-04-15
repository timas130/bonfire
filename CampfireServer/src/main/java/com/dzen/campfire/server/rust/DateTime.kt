package com.dzen.campfire.server.rust

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

data class DateTime(val inner: DateTime) {
    val millis: Long
        get() = inner.millis

    constructor(millis: Long) : this(DateTime(millis))

    companion object {
        @JvmStatic
        val adapter = object : Adapter<com.dzen.campfire.server.rust.DateTime> {
            override fun fromJson(
                reader: JsonReader,
                customScalarAdapters: CustomScalarAdapters
            ): com.dzen.campfire.server.rust.DateTime {
                return com.dzen.campfire.server.rust.DateTime(
                    DateTime(ISODateTimeFormat.dateTime().parseDateTime(reader.nextString()!!))
                )
            }

            override fun toJson(
                writer: JsonWriter,
                customScalarAdapters: CustomScalarAdapters,
                value: com.dzen.campfire.server.rust.DateTime
            ) {
                writer.value(ISODateTimeFormat.dateTime().print(value.inner))
            }
        }
    }
}
