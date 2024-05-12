package com.sayzen.devsupandroidgoogle

import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.java.libs.debug.err

object ControllerOneTapSignIn {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private var serverClientId:String? = null
    private var token:String? = null
    private var photoUrl:String? = null

    fun init(serverClientId:String){
        this.serverClientId = serverClientId

        oneTapClient = Identity.getSignInClient(SupAndroid.activity!!)
        signInRequest = BeginSignInRequest.builder()
                //.setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                //        .setSupported(true)
                //        .build())
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(serverClientId)
                                //  .setFilterByAuthorizedAccounts(true)// Only show accounts previously used to sign in.
                                .build())
                .build()
    }

    fun getToken(onResult:(String?)->Unit){
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(SupAndroid.activity!!) { result ->
                    try {
                        ToolsIntent.startIntentForResult(result.pendingIntent.intentSender){code,data->
                            try {
                                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                                token = credential.googleIdToken
                                photoUrl = credential.profilePictureUri.toString()
                                onResult.invoke(token)
                            } catch (e: ApiException) {
                                err(e)
                            }
                        }
                    } catch (e: IntentSender.SendIntentException) {
                    }
                }
                .addOnFailureListener(SupAndroid.activity!!) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    err(e.localizedMessage)
                }
    }


    fun signOut() {
        oneTapClient.signOut()
    }

    fun containsToken() = token != null
    fun getGooglePhotoUrl() = photoUrl

}
