package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.google.firebase.auth.FirebaseAuth
import com.sayzen.devsupandroidgoogle.ControllerGoogleAuth
import com.sup.dev.android.tools.ToolsStorage

object ControllerApiLogin {


    val LOGIN_NONE = 0
    val LOGIN_GOOGLE = 1
    val LOGIN_EMAIL = 2

    fun setLoginType(type:Int){
        ToolsStorage.put("ControllerApiLogin.login_type", type)
    }

    fun getLoginType() = ToolsStorage.getInt("ControllerApiLogin.login_type", LOGIN_NONE)

    fun isLoginNone() = getLoginType() == LOGIN_NONE
    fun isLoginGoogle() = getLoginType() == LOGIN_GOOGLE
    fun isLoginEmail() = getLoginType() == LOGIN_EMAIL


    fun getLoginToken(callbackSource: (String?) -> Unit){
        if(isLoginGoogle()) {
            getLoginToken_Google(callbackSource)
        } else if(isLoginEmail()){
            getLoginToken_Email(callbackSource)
        } else {
            callbackSource.invoke(null)
        }
    }

    fun clear(){
        setLoginType(LOGIN_NONE)
        auth.signOut()
    }

    //
    //  Google
    //

    private fun getLoginToken_Google(callbackSource: (String?) -> Unit){
        ControllerGoogleAuth.getToken {
            ControllerGoogleAuth.tokenPostExecutor.invoke(it) { token ->
                callbackSource.invoke(token)
            }
        }
    }

    //
    //  Email
    //

    private val auth = FirebaseAuth.getInstance()
    private fun getLoginToken_Email(callbackSource: (String?) -> Unit){
        auth.currentUser?.getIdToken(false)
            ?.addOnSuccessListener {
                callbackSource(API.LOGIN_EMAIL2_PREFIX + API.LOGIN_SPLITTER + it.token)
            }
            ?.addOnFailureListener {
                it.printStackTrace()
                callbackSource(null)
            }
    }
}