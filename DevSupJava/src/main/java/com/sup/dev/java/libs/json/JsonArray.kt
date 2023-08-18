package com.sup.dev.java.libs.json

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsText
import com.support.json.json_simple.JSONArray
import com.support.json.json_simple.JSONObject
import com.support.json.json_simple.JSONValue
import java.lang.Exception


class JsonArray {

    private val jsonArray: JSONArray

    constructor() {
        jsonArray = JSONArray()
    }

    constructor(s: String) {
        var jsonArray = JSONArray()
        try {
            val parse = JSONValue.parseWithException(s)
            jsonArray = if (parse == null) JSONArray() else parse as JSONArray
        }catch (e:Exception){
            err(e)
        }
        this.jsonArray = jsonArray
    }

    constructor(s: JSONArray) {
        jsonArray = s
    }

    fun getJSONArray() = jsonArray

    //
    //  Methods
    //

    override fun toString(): String {
        return jsonArray.toJSONString()
    }

    fun size(): Int {
        return jsonArray.size
    }

    //
    //  Getters
    //

    fun getBoolean(i: Int): Boolean {
        return jsonArray[i] as Boolean
    }

    fun getByte(i: Int): Byte {
        return jsonArray[i] as Byte
    }

    fun getInt(i: Int): Int {
        return jsonArray[i] as Int
    }

    fun getLong(i: Int): Long {
        return jsonArray[i] as Long
    }

    fun getFloat(i: Int): Float {
        return jsonArray[i] as Float
    }

    fun getDouble(i: Int): Double {
        return jsonArray[i] as Double
    }

    fun getJson(i: Int): Json {
        return try {
            Json(jsonArray[i] as JSONObject)
        } catch (e: ClassCastException) {
            Json(jsonArray[i] as String)
        }
    }

    fun getJsonArray(i: Int): JsonArray {
        return JsonArray(jsonArray[i] as String)
    }

    fun getString(i: Int): String {
        return jsonArray[i] as String
    }

    fun getBytes(i: Int): ByteArray {
        return ToolsText.hexToBytes(jsonArray[i] as String)
    }


    fun getInts(): Array<Int> {
        return Array(jsonArray.size) { (jsonArray[it] as Long).toInt() }
    }

    fun getLongs(): Array<Long> {
        return Array(jsonArray.size) { jsonArray[it] as Long }
    }

    fun getFloats(): Array<Float> {
        return Array(jsonArray.size) { jsonArray[it] as Float }
    }

    fun getDoubles(): Array<Double> {
        return Array(jsonArray.size) { jsonArray[it] as Double }
    }

    fun getBooleans(): Array<Boolean> {
        return Array(jsonArray.size) { jsonArray[it] as Boolean }
    }

    fun getStrings(): Array<String?> {
        return Array(jsonArray.size) { jsonArray[it] as String? }
    }

    fun getJsons(): Array<Json?> {
        val array = arrayOfNulls<Json>(jsonArray.size)
        for (i in jsonArray.indices)
            when {
                jsonArray[i] == null -> array[i] = null
                jsonArray[i] is JSONObject -> array[i] = Json(jsonArray[i] as JSONObject)
                jsonArray[i] is Json -> array[i] = jsonArray[i] as Json
                else -> array[i] = Json(jsonArray[i] as String)
            }
        return array
    }

    fun getJsonsArrays(): Array<JsonArray?> {
        return Array(jsonArray.size) { JsonArray(jsonArray[it] as String) }
    }


    //
    //  Put
    //

    fun put(vararg x: Boolean): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun put(x: ByteArray): JsonArray {
        jsonArray.add(ToolsText.bytesToHex(x))
        return this
    }

    fun put(vararg x: Int): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun put(vararg x: Long): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun put(vararg x: Float): JsonArray {
        for (i in x)
            jsonArray.add(i)
        return this
    }

    fun put(vararg x: Double): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun put(vararg x: String): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun put(vararg x: JsonArray): JsonArray {
        for (i in x) jsonArray.add(i.toString())
        return this
    }

    fun put(vararg x: Json): JsonArray {
        for (i in x) jsonArray.add(i.toString())
        return this
    }

    fun put(x: Collection<Any>): JsonArray {
        for (i in x) jsonArray.add(i)
        return this
    }

    fun toPrintString(spaceArg:String=""):String{
        var s = ""
        var close = ""
        val space = "$spaceArg "

        s += "["
        for(v in jsonArray){
            if(v is Json)  close += "\n$space${v.toPrintString(space)}"
            else if(v is JsonArray)  close += "\n$space${v.toPrintString(space)}"
            else s += "\n$space$v"
        }

        s += close
        s += "\n$spaceArg]"

        return s
    }


}
