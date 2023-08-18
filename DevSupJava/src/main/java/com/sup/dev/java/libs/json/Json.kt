package com.sup.dev.java.libs.json

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsClass
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsText
import com.support.json.json_simple.JSONArray
import com.support.json.json_simple.JSONObject
import com.support.json.json_simple.JSONValue
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class Json {

    var jsonObject: JSONObject? = null

    constructor() {
        jsonObject = JSONObject()
    }

    constructor(bytes: ByteArray) {
        set(bytes)
    }

    constructor(json: String) {
        set(json)
    }

    constructor(jsonObject: JSONObject) {
        this.jsonObject = jsonObject
    }

    //
    //  Methods
    //

    fun clear() {
        jsonObject!!.clear()
    }

    fun set(bytes: ByteArray) {
        set(String(bytes))
    }

    fun set(json: String) {
        if(json.isEmpty()) jsonObject = JSONObject()
        else jsonObject = (JSONValue.parseWithException(json) as JSONObject?) ?: JSONObject()
        if (jsonObject == null)
            throw IllegalArgumentException("Can't parse to Json: $json")
    }

    fun containsKey(key: String): Boolean {
        return jsonObject!!.containsKey(key)
    }

    override fun toString(): String {
        return jsonObject!!.toJSONString()
    }

    fun toPrintString(spaceArg:String=""): String {

        var s = ""
        var close = ""
        val space = "$spaceArg "

        s += "{"
        for(k in getKeys()){
            val v = get<Any?>(k.toString())
            if(v is Json)  close += "\n$space$k = ${v.toPrintString(space)}"
            else if(v is JsonArray)  close += "\n$space$k = ${v.toPrintString(space)}"
            else s += "\n$space$k = $v"

        }

        s += close
        s += "\n$spaceArg}"

        return s
    }

    fun toBytes(): ByteArray {
        return toString().toByteArray()
    }

    fun getKeys() = jsonObject?.keys?: emptySet<Any>()

    //
    //  Magic
    //

    fun <K : Any> m(inp: Boolean, key: String, value: K) = m(inp, key, value, value::class as KClass<K>)

    fun <K : Any> m(inp: Boolean, key: String, value: K, c: KClass<K>) = mNull(inp, key, value, c)
            ?: value

    fun <K : Any> mNull(inp: Boolean, key: String, value: K?, c: KClass<K>): K? {

        if (inp) {
            if (c == Array<Boolean>::class) put(key, value as Array<Boolean>?)
            else if (c == ByteArray::class) put(key, value as ByteArray?)
            else if (c == Json::class) put(key, value as Json?)
            else if (c == Array<String>::class) put(key, value as Array<String>?)
            else if (c == Array<Long>::class) put(key, value as Array<Long>?)
            else if (c == Array<Int>::class) put(key, value as Array<Int>?)
            else if (c == Array<Double>::class) put(key, value as Array<Double>?)
            else if (c == Array<Float>::class) put(key, value as Array<Float>?)
            else if (c == Array<out JsonArray>::class) put(key, value as Array<JsonArray>?)
            else if (c == Array<out Json>::class) put(key, value as Array<Json>?)
            else if (ToolsClass.instanceOf(c, JsonParsable::class)) put(key, value as JsonParsable?)
            else if (ToolsClass.instanceOf(c, Array<JsonParsable>::class)) put(key, value as Array<JsonParsable>?)
            else jsonObject!![key] = value
            return value
        }

        if (c == Byte::class) return getByteNull(key, value as Byte?) as K?
        if (c == Int::class) return getIntNull(key, value as Int?) as K?
        if (c == Float::class) return getFloatNull(key, value as Float?) as K?
        if (c == Long::class) return getLongNull(key, value as Long?) as K?
        if (c == Double::class) return getDoubleNull(key, value as Double?) as K?
        if (c == String::class) return getStringNull(key, value as String?) as K?
        if (c == Json::class) return getJson(key, value as Json?) as K?
        if (c == Array<Boolean>::class) return getBooleans(key, value as Array<Boolean>?) as K?
        if (c == ByteArray::class) return getBytes(key, value as ByteArray?) as K?
        if (c == Array<String?>::class) return getStrings(key, value as Array<String?>?) as K?
        if (c == Array<Long>::class) return getLongs(key, value as Array<Long>?) as K?
        if (c == Array<Int>::class) return getInts(key, value as Array<Int>?) as K?
        if (c == Array<Double>::class) return getDoubles(key, value as Array<Double>?) as K?
        if (c == Array<Float>::class) return getFloats(key, value as Array<Float>?) as K?
        if (c == Array<JsonArray?>::class) return getJsonArrays(key, value as Array<JsonArray?>?) as K?
        if (c == Array<Json?>::class) return getJsons(key, value as Array<Json?>?) as K?
        if (ToolsClass.instanceOf(c, JsonParsable::class)) {
            val jsonParsable = getJsonParsable(key, c as KClass<JsonParsable>)
            return if (jsonParsable == null) value else jsonParsable as K?
        }
        if (ToolsClass.instanceOf(c, Array<JsonParsable>::class)) {
            val jsonParsables = getJsonParsables(key, c as KClass<JsonParsable>)
            return if (jsonParsables == null) value else jsonParsables as K?
        }

        return get<K>(key)

    }


    //
    //  Get
    //

    operator fun <K> get(key: String): K? {
        return get<K>(key, null)
    }

    operator fun <K> get(key: String, def: K?): K? {
        val o = jsonObject!![key] ?: return def
        return o as K
    }

    fun getBoolean(key: String, def: Boolean = false) = getBooleanNull(key, def)!!
    fun getBooleanNull(key: String, def: Boolean? = null) = get(key, def)

    fun getByte(key: String, def: Byte = 0.toByte()) = getByteNull(key, def)!!
    fun getByteNull(key: String, def: Byte? = null) = get(key, def?.toLong())?.toByte()

    fun getInt(key: String, def: Int = 0) = getIntNull(key, def)!!
    fun getIntNull(key: String, def: Int? = null): Int? {
        val get = get<Any>(key, null)
        return if (get == null) def else ToolsMapper.toInt(get)
    }

    fun getLong(key: String, def: Long = 0) = getLongNull(key, def)!!
    fun getLongNull(key: String, def: Long? = null): Long? {
        val get = get<Any>(key, null)
        return if (get == null) def else ToolsMapper.asLong(get)
    }

    fun getFloat(key: String, def: Float = 0f) = getFloatNull(key, def)!!
    fun getFloatNull(key: String, def: Float? = null): Float? {
        val get = get<Any>(key, null)
        return if (get == null) def else ToolsMapper.asFloat(get)
    }

    fun getDouble(key: String, def: Double = 0.0) = getDoubleNull(key, def)!!
    fun getDoubleNull(key: String, def: Double? = null): Double? {
        val get = get<Any>(key, null)
        return if (get == null) def else ToolsMapper.asDouble(get)
    }

    fun getString(key: String, def: String = "") = getStringNull(key, def)!!
    fun getStringNull(key: String, def: String? = null) = get<Any>(key, null)?.toString() ?: def

    fun getJson(key: String, def: Json? = null): Json? {
        val o = jsonObject!![key]
        if (o == null)
            return def
        else if (o is String)
            return Json(o)
        else if (o is JSONArray) {
            val array = o as JSONArray?
            return if (array!!.size == 0) null else Json(array[0] as String)
        } else return o as? Json ?: Json(o as JSONObject)
    }

    fun getJsonArray(key: String): JsonArray? {
        val o = jsonObject!![key]
        return if (o == null)
            null
        else if (o is String)
            JsonArray((o as String?)!!)
        else o as? JsonArray ?: JsonArray((o as JSONArray?)!!)
    }

    fun <K : JsonParsable> getJsonParsable(key: String, cc: KClass<K>, def: K? = null): K? {
        val c = cc.java
        val json = getJson(key) ?: return def

        val cPolimorf = ToolsClass.thisInstanceOfInterface_getSuperclass(c, JsonPolimorf::class.java)
        if (cPolimorf != null) {
            for (m in cPolimorf.methods)
                if (m.returnType == cPolimorf && m.parameterTypes.size == 1 && m.parameterTypes[0] == Json::class.java)
                    return m.invoke(null, json) as K
            throw RuntimeException("When you implementation [JsonPolimorf], you must declare static method with return type [$c] and param [Json]")

        } else {
            try {
                val constructor = c.getConstructor()
                val k = constructor.newInstance()
                (k as K).json(false, json)
                return k
            } catch (e: Exception) {
                err("Check that JsonParsable class have empty constructor")
                throw RuntimeException(e)
            }
        }
    }

    fun getBooleans(key: String, def: Array<Boolean>? = null): Array<Boolean>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getBooleans()
    }

    fun getBytes(key: String, def: ByteArray? = null): ByteArray? {
        val s = get<String>(key) ?: return def
        return ToolsText.hexToBytes(s)
    }

    fun getInts(key: String, def: Array<Int>? = null): Array<Int>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getInts()
    }

    fun getLongs(key: String, def: Array<Long>? = null): Array<Long>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getLongs()
    }

    fun getFloats(key: String, def: Array<Float>? = null): Array<Float>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getFloats()
    }

    fun getDoubles(key: String, def: Array<Double>? = null): Array<Double>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getDoubles()
    }

    fun getStrings(key: String, def: Array<String?>? = null): Array<String?>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getStrings()
    }

    fun getJsons(key: String, def: Array<Json?>? = null): Array<Json?>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getJsons()
    }

    fun getJsonArrays(key: String, def: Array<JsonArray?>? = null): Array<JsonArray?>? {
        val jsonArray = getJsonArray(key) ?: return def
        return jsonArray.getJsonsArrays()
    }

    fun <K : JsonParsable> getJsonParsables(key: String, cc: KClass<K>): Array<K>? {
        val c = cc.java.componentType?:cc.java

        try {
            val jsonArray = getJsonArray(key)
            if (jsonArray != null) {
                val jsons = jsonArray.getJsons()
                val array = java.lang.reflect.Array.newInstance(c, jsons.size) as Array<K>

                val cPolimorf = ToolsClass.thisInstanceOfInterface_getSuperclass(c, JsonPolimorf::class.java)
                if (cPolimorf != null) {
                    var b = false
                    for (m in cPolimorf.methods) {
                        if (m.returnType == cPolimorf && m.parameterTypes.size == 1 && m.parameterTypes[0] == Json::class.java) {
                            b = true
                            for (i in jsons.indices)
                                array[i] = m.invoke(null, jsons[i]) as K
                            break
                        }
                    }

                    if (!b) throw RuntimeException("When you implementation [JsonPolimorf], you must declare static method with return type [$c] and param [Json]")
                } else {
                    val constructor = c.getConstructor()
                    for (i in jsons.indices) {
                        val k = constructor.newInstance()
                        if (jsons[i] != null) {
                            (k as K).json(false, jsons[i] as Json)
                            array[i] = k
                        }
                    }
                }

                return array
            }
            return null

        } catch (e: Exception) {
            err("Check the JsonParsable subclass has empty constructor!")
            throw RuntimeException(e)
        }
    }


    //
    //  Put
    //

    fun put(key: String, x: Boolean): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: Byte): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: Int): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: Long): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: Float): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: Double): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, x: String?): Json {
        jsonObject!![key] = x
        return this
    }

    fun put(key: String, json: Json?): Json {
        jsonObject!![key] = json
        return this
    }

    fun put(key: String, jsonArray: JsonArray): Json {
        jsonObject!![key] = jsonArray.getJSONArray()
        return this
    }

    fun put(key: String, x: JsonParsable?): Json {
        put(key, x?.json(true, Json()))
        return this
    }

    fun put(key: String, x: Array<Boolean>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x) jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: ByteArray?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        jsonObject!![key] = ToolsText.bytesToHex(x)
        return this
    }

    fun put(key: String, x: Array<Int>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<Long>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<Float>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<Double>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<String>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<JsonArray>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i.getJSONArray())
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<Json>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i)
        jsonObject!![key] = jsonArray
        return this
    }

    fun put(key: String, x: Array<JsonParsable>?): Json {
        if (x == null) {
            jsonObject!![key] = null
            return this
        }
        val jsonArray = JSONArray()
        for (i in x)
            jsonArray.add(i.json(true, Json()))
        jsonObject!![key] = jsonArray
        return this
    }

    //
    //  Etc
    //

    fun isEmpty() = jsonObject?.isEmpty() ?: true

    fun isNotEmpty() = jsonObject?.isNotEmpty() ?: false

    fun forEach(action: (key: String, value: Any?) -> Unit) {
        for (item in jsonObject ?: emptyMap()) {
            action(item.key as String, item.value)
        }
    }

    fun toMap(): Map<Any?, Any?> = jsonObject?.toMap() ?: emptyMap()
}
