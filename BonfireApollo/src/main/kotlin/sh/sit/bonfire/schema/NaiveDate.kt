package sh.sit.bonfire.schema

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

class NaiveDate(val date: LocalDate) {
    override fun toString(): String {
        return ISODateTimeFormat.date().print(date)
    }

    companion object {
        @JvmStatic
        val adapter = object : Adapter<NaiveDate> {
            override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): NaiveDate {
                return NaiveDate(ISODateTimeFormat.dateParser().parseLocalDate(reader.nextString()!!))
            }

            override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: NaiveDate) {
                writer.value(value.toString())
            }
        }
    }
}
