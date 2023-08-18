package com.sup.dev.android.libs.http

import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import org.json.JSONException
import java.net.*

object ToolsHttpCookieAndroid {

    private val COOKIES = "ToolsHttpCookieAndroid_COOKIES"
    private val NAME = "NAME"
    private val VALUE = "VALUE"
    private val VERSION = "VERSION"
    private val MAX_AGE = "MAX_AGE"
    private val URI_KEY = "URI"

    private val httpClientCookieStore = HttpClientCookieStore()

    fun init() {
        CookieHandler.setDefault(CookieManager(httpClientCookieStore, CookiePolicy.ACCEPT_ALL))
    }

    fun getStore() = httpClientCookieStore

    fun clear() {
        httpClientCookieStore.removeAll()
    }

    class HttpClientCookieStore : CookieStore {

        override fun add(uri: URI?, cookie: HttpCookie?) {
            if (cookie == null || uri == null) return
            try {
                remove(uri, cookie)
                val array = ToolsStorage.getJsonArray(COOKIES)?:JsonArray()
                array.put(Json()
                        .put(NAME, cookie.name)
                        .put(VALUE, cookie.value)
                        .put(VERSION, cookie.version)
                        .put(MAX_AGE, cookie.maxAge)
                        .put(URI_KEY, uri.toString())
                )
                ToolsStorage.put(COOKIES, array)
            } catch (e: JSONException) {
                err(e)
            }
        }

        override fun removeAll(): Boolean {
            try {
                if (ToolsStorage.contains(COOKIES)) {
                    ToolsStorage.clear(COOKIES)
                    return true
                } else {
                    return false
                }
            } catch (e: Exception) {
                err(e)
                return false
            }
        }

        override fun getCookies(): MutableList<HttpCookie> {
            try {

                val list = ArrayList<HttpCookie>()
                val array = ToolsStorage.getJsonArray(COOKIES)?:JsonArray()

                for (j in array.getJsons()) {
                    if (j == null) continue
                    val cookie = HttpCookie(j.get(NAME, ""), j.get(VALUE, ""))
                    cookie.version = j.getInt(VERSION, 0)
                    cookie.maxAge = j.getLong(MAX_AGE, 0)
                    list.add(cookie)
                }

                return list
            } catch (e: Exception) {
                err(e)
                return ArrayList()
            }
        }

        override fun getURIs(): MutableList<URI> {
            try {
                val list = ArrayList<URI>()
                val array = ToolsStorage.getJsonArray(COOKIES)?:JsonArray()

                for (j in array.getJsons()) {
                    if (j == null) continue
                    list.add(URI(j.getString(URI_KEY, "")))
                }

                return list
            } catch (e: Exception) {
                err(e)
                return ArrayList()
            }
        }

        override fun remove(uri: URI?, cookie: HttpCookie?): Boolean {
            try {
                val array = ToolsStorage.getJsonArray(COOKIES)?:JsonArray()
                val arrayNew = JsonArray()

                for (j in array.getJsons()) {
                    if (j == null) continue
                    if (URI(j.getString(URI_KEY, "")).host == uri?.host.toString()) {
                        val oldCookies = HttpCookie(j.get(NAME, ""), j.get(VALUE, ""))
                        if (cookie == null || cookie.name == oldCookies.name)
                            continue
                    }
                    arrayNew.put(j)
                }

                ToolsStorage.put(COOKIES, arrayNew)

                return true
            } catch (e: Exception) {
                err(e)
                return false
            }
        }

        override fun get(uri: URI?): MutableList<HttpCookie> {
            try {

                val list = ArrayList<HttpCookie>()
                val array = ToolsStorage.getJsonArray(COOKIES)?:JsonArray()

                for (j in array.getJsons()) {
                    if (j == null) continue
                    if (URI(j.getString(URI_KEY, "")).host != uri?.host ?: "null") continue
                    val cookie = HttpCookie(j.get(NAME, ""), j.get(VALUE, ""))
                    cookie.version = j.getInt(VERSION, 0)
                    cookie.maxAge = j.getLong(MAX_AGE, 0)
                    list.add(cookie)
                }

                return list
            } catch (e: Exception) {
                err(e)
                return ArrayList()
            }
        }


    }


}