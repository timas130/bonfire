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
import com.sayzen.campfiresdk.models.objects.Rule
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView

class SRulesUser(
        noNavigationMode: Boolean = false
) : Screen(R.layout.screen_other_rules_user) {

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)

    init {
        setTitle(t(API_TRANSLATE.about_rules_user))
        disableShadows()
        disableNavigation()
        if (noNavigationMode) {
            activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)

            disableNavigation()
            vCopyLink.visibility = View.GONE
        }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_USER.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        addTitle(null, API_TRANSLATE.rules_users_info)
        for (i in CampfireConstants.RULES_USER.indices) addCard(i + 1, CampfireConstants.RULES_USER[i])
    }

    private fun addTitle(num: Int?, text: Long) {
        val view: View = ToolsView.inflate(R.layout.view_card_rules)
        val vText: TextView = view.findViewById(R.id.vText)
        val vNum: TextView = view.findViewById(R.id.vNum)
        vText.setTextIsSelectable(true)
        vText.text = t(text)
        vNum.text = "$num"
        vNum.visibility = if (num == null) View.GONE else View.VISIBLE
        vContainer.addView(view)
    }

    private fun addCard(num: Int?, rule:Rule) {
        val view: View = ToolsView.inflate(R.layout.view_card_rules)
        val vText: TextView = view.findViewById(R.id.vText)
        val vNum: TextView = view.findViewById(R.id.vNum)
        vText.setTextIsSelectable(true)
        vText.text = t(rule.text)
        vNum.text = "$num"
        vNum.visibility = if (num == null) View.GONE else View.VISIBLE
        vContainer.addView(view)
    }
}
