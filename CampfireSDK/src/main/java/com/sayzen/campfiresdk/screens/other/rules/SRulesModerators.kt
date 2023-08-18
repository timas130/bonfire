package com.sayzen.campfiresdk.screens.other.rules

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.Translate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView

class SRulesModerators : Screen(R.layout.screen_other_rules_moderators) {

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.about_rules_moderator))
        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_MODER.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        addCard(null, API_TRANSLATE.rules_moderators_info)
        for (i in CampfireConstants.RULES_MODER.indices) addCard(i+1, CampfireConstants.RULES_MODER[i])
    }

    private fun addCard(num:Int?, text: Translate) {
        val view: View = ToolsView.inflate(R.layout.view_card_rules)
        val vLabelForbidden: TextView = view.findViewById(R.id.vLabelForbidden)
        val vLabelAllowed: TextView = view.findViewById(R.id.vLabelAllowed)
        val vText: TextView = view.findViewById(R.id.vText)
        val vNum: TextView = view.findViewById(R.id.vNum)
        val vForbiddenContainer: View = view.findViewById(R.id.vForbiddenContainer)
        val vAllowedContainer: View = view.findViewById(R.id.vAllowedContainer)
        vLabelForbidden.text = t(API_TRANSLATE.app_forbidden)
        vLabelAllowed.text = t(API_TRANSLATE.app_allowed)
        vForbiddenContainer.visibility = View.GONE
        vAllowedContainer.visibility = View.GONE
        vText.setTextIsSelectable(true)
        vText.setText(t(text))
        vNum.text = "$num"
        vNum.visibility = if(num == null) View.GONE else View.VISIBLE
        vContainer.addView(view)
    }

}