package com.sup.dev.java_pc.google

import com.sup.dev.java.classes.collections.Cash
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.http_api.HttpRequest
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset

object GoogleAuth {
    class GoogleAuthCreds(var clientId: String = "", var clientSecret: String = "") : JsonParsable {
        override fun json(inp: Boolean, json: Json): Json {
            clientId = json.m(inp, "client_id", clientId)
            clientSecret = json.m(inp, "secret", clientSecret)
            return json
        }
    }

    private lateinit var creds: Array<GoogleAuthCreds>
    private var cash: Cash<String, String> = Cash(10000)

    @Deprecated("use init(Array<GoogleAuth.GoogleAuthCreds>)")
    fun init(serverClientId: String, serverClientSecret: String) {
        creds = arrayOf(GoogleAuthCreds(serverClientId, serverClientSecret))
    }

    fun init(creds: Array<GoogleAuthCreds>) {
        this.creds = creds
    }

    fun getGoogleId(token: String, clientIndex: Int = 0): String? {
        var googleId = cash[token]
        if (googleId != null) return googleId

        googleId = if (token.startsWith("4/")) requestByIdServerAuthCode(token, clientIndex)
        else requestByIdToken(token)

        cash.put(token, googleId)
        return googleId
    }

    private fun requestByIdServerAuthCode(token: String, clientIndex: Int = 0): String? {
        try {
            val result = HttpRequest()
                    .setUrl("https://www.googleapis.com/oauth2/v4/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .setMethods(HttpRequest.Method.POST)
                    .setJson(Json()
                            .put("grant_type", "authorization_code")
                            .put("client_id", creds[clientIndex].clientId)
                            .put("client_secret", creds[clientIndex].clientSecret)
                            .put("code", token)
                    )
                    .makeNow()

            val json = Json(result.text)
            val idToken = json.get("id_token", "") ?: ""

            return requestByIdToken(idToken)
        } catch (e: Exception) {
            err(e)
            return null
        }

    }

    private fun requestByIdToken(token: String): String? {
        var inp: BufferedReader? = null
        try {
            inp = BufferedReader(InputStreamReader(URL("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=$token").openConnection().getInputStream(), Charset.forName("UTF-8")))
            var s = ""
            while (inp.ready()) s += inp.readLine()

            val json = Json(s)
            if (!json.containsKey("sub")) return null
            if (!json.containsKey("aud")) return null
            val googleId = json.getString("sub")
            val aud = json.getString("aud")
            if (!creds.any { it.clientId == aud }) {
                err("[GoogleAuth] aud mismatch! recv: $aud")
                return null
            }

            return googleId
        } catch (e: Exception) {
            err(e)
        } finally {
            if (inp != null) {
                try {
                    inp.close()
                } catch (e: IOException) {
                    err(e)
                }

            }
        }
        return null
    }

}
