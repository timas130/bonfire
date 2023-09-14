package com.dzen.campfire.screens.intro

import android.view.View
import android.widget.Button
import com.dzen.campfire.R
import com.google.firebase.auth.FirebaseAuth
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerApiLogin
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.android.views.views.ViewButton

class SIntroEmail : Screen(R.layout.screen_intro_email){
    private val vEmail: SettingsField = findViewById(R.id.vEmail)
    private val vPass: SettingsField = findViewById(R.id.vPass)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vRegistration: Button = findViewById(R.id.vRegistration)
    private val vLogo: View = findViewById(R.id.vLogo)
    private val vForgotPassword: ViewButton = findViewById(R.id.vForgotPassword)

    var bestHeight = 0

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        disableNavigation()

        vEmail.setHint(R.string.app_email)
        vPass.setHint(R.string.app_password)
        vEnter.setText(R.string.app_login)
        vRegistration.setText(R.string.app_registration)
        vForgotPassword.setText(R.string.reset_password)

        vEmail.addOnTextChanged { updateEnterEnabled() }
        vPass.addOnTextChanged { updateEnterEnabled() }

        vEnter.setOnClickListener { enter() }
        vRegistration.setOnClickListener {
            ControllerTranslate.loadLanguage(
                languageId = ControllerApi.getLanguageId(),
                onLoaded = { Navigator.to(SIntroEmailRegistration(), Navigator.Animation.ALPHA) },
                onError = { ToolsToast.show(R.string.connection_error) }
            )
        }

        vForgotPassword.setOnClickListener {
            val email = vEmail.getText()
            if (email.isBlank()) {
                ToolsToast.show(R.string.enter_email)
                return@setOnClickListener
            }

            val loading = SplashProgressTransparent()
            loading.asSplashShow()

            fbAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    SplashAlert()
                        .setText(R.string.recover_email_sent)
                        .setOnEnter(android.R.string.ok)
                        .asSheetShow()
                }
                .addOnFailureListener {
                    SplashAlert()
                        .setText(it.localizedMessage)
                        .setOnEnter(android.R.string.ok)
                        .asSheetShow()
                }
                .addOnCompleteListener {
                    loading.hide()
                }
        }

        updateEnterEnabled()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if(height > 3000) return
        if(height > bestHeight) bestHeight = height
        vLogo.visibility = if(height < bestHeight) View.GONE else View.VISIBLE
    }

    private fun updateEnterEnabled() {
        vEnter.isEnabled = vEmail.getText().length >= 3 && vPass.getText().length >= 6
    }

    private val fbAuth by lazy {
        FirebaseAuth.getInstance().apply {
            useAppLanguage()
        }
    }

    private fun enter() {
        val password = vPass.getText()
        val email = vEmail.getText()

        val progress = ToolsView.showProgressDialog()
        fbAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                if (!authResult.user!!.isEmailVerified) {
                    Navigator.replace(SIntroEmailVerify(false))
                    return@addOnSuccessListener
                }
                authResult.user!!.getIdToken(true)
                    .addOnSuccessListener {
                        ControllerApiLogin.setLoginType(ControllerApiLogin.LOGIN_EMAIL)
                        Navigator.set(SIntroConnection())
                    }
                    .addOnFailureListener {
                        ToolsToast.show(it.localizedMessage ?: it.message)
                    }
                    .addOnCompleteListener {
                        progress.hide()
                    }
            }
            .addOnFailureListener {
                ToolsToast.show(it.localizedMessage ?: it.message)
                progress.hide()
            }
    }

}
