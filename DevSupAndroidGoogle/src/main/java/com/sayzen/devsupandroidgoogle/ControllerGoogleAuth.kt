package com.sayzen.devsupandroidgoogle

import android.view.Gravity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.java.classes.callbacks.CallbacksList1
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

object ControllerGoogleAuth {

    private var serverClientId = ""

    var tokenPostExecutor: (String?, callback: (String?) -> Unit) -> Unit = { token, callback -> callback.invoke(token) }
    private var googleAccount: GoogleSignInAccount? = null
    private var onLoginFailed: () -> Unit = {}

    fun init(serverClientId: String, onLoginFailed: () -> Unit) {
        this.serverClientId = serverClientId
        this.onLoginFailed = onLoginFailed
    }

    fun getGooglePhotoUrl(): String? {
        if (googleAccount == null || googleAccount!!.photoUrl == null) return null
        return googleAccount!!.photoUrl!!.toString()
    }

    fun logout(callback: (() -> Unit)) {
        getGoogleApiClient { googleApiClient ->
            ToolsThreads.main {
                googleAccount = null
                if(googleApiClient == null){
                    callback.invoke()
                    return@main
                }
                Auth.GoogleSignInApi.signOut(googleApiClient)
                callback.invoke()
                googleApiClient.disconnect()
            }
        }
    }

    val callbacks = CallbacksList1<String?>()
    var lock = 0L

    fun getToken(onResult: (String?) -> Unit) {
        ToolsThreads.main {
            callbacks.add(onResult)
            if(lock > 0) return@main
            val myLock = System.currentTimeMillis()
            lock = myLock
            getGoogleIdToken{ token->
                if(lock == myLock){
                    lock = 0L
                    callbacks.invokeAndClear(token)
                }
            }
            ToolsThreads.main(30000){
                if(lock == myLock){
                    lock = 0L
                    callbacks.invokeAndClear(null)
                }
            }
        }
    }

    fun clearToken() {
        googleAccount = null
    }

    fun onLoginFailed() {
        onLoginFailed.invoke()
    }

    fun containsToken(): Boolean {
        return googleAccount != null
    }

    //
    //  Autch
    //


    fun getGoogleIdToken(tryCount: Int = 5, onResult: (String?) -> Unit) {
        if (googleAccount != null) {
            onResult.invoke(googleAccount!!.idToken)
            return
        }
        getGoogleSignInAccount { googleAccount ->
            ControllerGoogleAuth.googleAccount = googleAccount
            if ((googleAccount?.idToken == null || googleAccount.idToken!!.isEmpty()) && tryCount > 0) {
                ToolsThreads.main(200) { getGoogleIdToken(tryCount - 1, onResult) }
            } else {
                if (ToolsAndroid.isDebug()) {
                    if (googleAccount?.idToken == null || googleAccount.idToken!!.isEmpty()) {
                        err("GOOGLE DONT'T PROVIDE TOKEN [${googleAccount?.idToken}] [${googleAccount}]")
                    }
                }
                onResult.invoke(googleAccount?.idToken)
            }
        }
    }

    fun getGoogleSignInAccount(onComplete: (GoogleSignInAccount?) -> Unit) {
        getGoogleApiClient { googleApiClient ->


            if(googleApiClient == null){
                err("ERROR google client is null googleApiClient[$googleApiClient]")
                ToolsThreads.main { onComplete.invoke(null) }
                return@getGoogleApiClient
            }

            val googleSignInResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient).await(500, TimeUnit.MILLISECONDS)

            if (googleSignInResult.isSuccess && googleSignInResult.signInAccount != null) {
                ToolsThreads.main { onComplete.invoke(googleSignInResult.signInAccount) }
                googleApiClient.disconnect()
            } else {

                ToolsIntent.startIntentForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient)) { resultCode, intent ->
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent!!)

                    if (result == null || result.signInAccount?.idToken == null) {
                        err("ERROR google result is null result[$result] idToken[${result?.signInAccount?.idToken}]")
                        onComplete.invoke(null)
                    } else
                        onComplete.invoke(result.signInAccount)

                    googleApiClient.disconnect()
                }
            }
        }
    }

    private fun getGoogleApiClient(onConnect: (GoogleApiClient?) -> Unit) {

        ToolsThreads.thread {

            var timeout = 10000
            while (SupAndroid.activity == null && timeout > 0){
                ToolsThreads.sleep(100)
                timeout -= 100
            }

           ToolsThreads.main{

               if(SupAndroid.activity == null || SupAndroid.activityIsDestroy){
                   err("ERROR google activity is null activity[${SupAndroid.activity}] activityIsDestroy[${SupAndroid.activityIsDestroy}]")
                   onConnect.invoke(null)
                   return@main
               }

               val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                       .requestIdToken(serverClientId)
                       .build()

               val client = GoogleApiClient.Builder(SupAndroid.activity!!)
                       .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                       .setGravityForPopups(Gravity.BOTTOM or Gravity.CENTER)
                       .build()

               ToolsThreads.thread {
                   client.blockingConnect()
                   onConnect.invoke(client)
               }
           }
        }

    }


}