package com.dzen.campfire.server.controllers

import com.dzen.campfire.server.app.App
import com.sup.dev.java.libs.http_api.HttpRequest
import com.sup.dev.java.libs.json.Json
import java.net.URLEncoder

object ControllerCaptcha {
    fun verify(client: String): Boolean {
        val resp = Json(HttpRequest().setUrl("https://hcaptcha.com/siteverify").setPOST()
            .setBody("response=${URLEncoder.encode(client, "utf-8")}&secret=${App.hcaptchaSecret}")
            .makeNow().text)
        return resp.getBoolean("success")
    }
}