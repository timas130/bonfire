package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.project.RProjectDonatesCreateDraft
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.http_api.HttpRequest
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class SDonateMake : Screen(R.layout.screen_donate_make){

    private val vSum: EditText = findViewById(R.id.vSum)
    private val vButton: Button = findViewById(R.id.vButton)
    private val vComment: EditText = findViewById(R.id.vComment)
    private val vIcon_yandex: ViewIcon = findViewById(R.id.vIcon_yandex)
    private val vIcon_card: ViewIcon = findViewById(R.id.vIcon_card)
    private val vIcon_phone: ViewIcon = findViewById(R.id.vIcon_phone)
    private val vDonateContainer: View = findViewById(R.id.vDonateContainer)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vMobileAlert: ViewText = findViewById(R.id.vMobileAlert)
    private val vImage_1: ImageView = findViewById(R.id.vImage_1)
    private val vImage_2: ImageView = findViewById(R.id.vImage_2)
    private val vText_1: TextView = findViewById(R.id.vText_1)
    private val vText_2: TextView = findViewById(R.id.vText_2)
    private val vText_3: TextView = findViewById(R.id.vText_3)
    private val vMessage: TextView = findViewById(R.id.vMessage)

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.activities_support_card_action_2))

        vText_1.text = t(API_TRANSLATE.activities_support_donate_text_1)
        vText_2.text = t(API_TRANSLATE.activities_support_donate_text_2)
        vText_3.text = t(API_TRANSLATE.activities_support_donate_text_3)
        vSum.hint = t(API_TRANSLATE.activities_support_sum)
        vMobileAlert.text = t(API_TRANSLATE.activities_support_mobile_alert)
        vComment.hint = t(API_TRANSLATE.activities_support_comment_hint)
        vButton.text = t(API_TRANSLATE.activities_support_card_action)
        vMessage.text = t(API_TRANSLATE.activities_support_message)

        vButton.setOnClickListener {
            donate()
        }
        vSum.addTextChangedListener(TextWatcherChanged {
            updateEnabled()
        })
        vComment.addTextChangedListener(TextWatcherChanged {
            updateEnabled()
        })
        vSum.setText("10")
        vIcon_yandex.setOnClickListener { setSelected(vIcon_yandex) }
        vIcon_card.setOnClickListener { setSelected(vIcon_card) }
        vIcon_phone.setOnClickListener { setSelected(vIcon_phone) }

        updateEnabled()

        vMessageContainer.visibility = View.GONE

        ControllerLinks.makeLinkable(vMobileAlert)

        ImageLoader.load(API_RESOURCES.ICON_YANDEX_DENGI).into(vIcon_yandex)
        ImageLoader.load(API_RESOURCES.ICON_BANK_CARD).into(vIcon_card)
        ImageLoader.load(API_RESOURCES.ICON_PHONE).into(vIcon_phone)

        setSelected(vIcon_card)

        ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_25).noHolder().into(vImage_2)
        ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_25).noHolder().intoBitmap { vImage_1.setImageBitmap(ToolsBitmap.mirror(it!!)) }
    }



    private fun setSelected(v: ViewIcon) {
        vIcon_yandex.isIconSelected = v == vIcon_yandex
        vIcon_card.isIconSelected = v == vIcon_card
        vIcon_phone.isIconSelected = v == vIcon_phone

        vMobileAlert.visibility = if (vIcon_phone.isIconSelected) View.VISIBLE else View.GONE
    }

    private fun updateEnabled() {
        vButton.isEnabled = ToolsMapper.isIntCastable(vSum.text.toString()) && vComment.text.length <= API.DONATE_COMMENT_MAX_L
    }


    private fun donate() {
        val sum = vSum.text.toString().toInt()
        ApiRequestsSupporter.executeProgressDialog(RProjectDonatesCreateDraft(vComment.text.toString(), (sum * 100).toLong())) { r ->
            donateNow(r.donateId, sum)
        }
    }

    private fun donateNow(donateId: Long, sum: Int) {

        val type = if (vIcon_yandex.isIconSelected) "PC" else if (vIcon_card.isIconSelected) "AC" else "MC"

        val url = HttpRequest()
                .setUrl("https://money.yandex.ru/quickpay/confirm.xml")
                .param("receiver", "410011747883287")
                .param("quickpay-form", "donate")
                .param("paymentType", type)
                .param("sum", "$sum")
                .param("label", "${ControllerApi.account.getId()}-${donateId}-${sum}")
                .param("comment", t(API_TRANSLATE.activities_support_comment_user, ControllerApi.account.getName()))
                .param("targets", t(API_TRANSLATE.activities_support_comment))
                .param("formcomment", t(API_TRANSLATE.activities_support_comment))
                .param("short-dest", t(API_TRANSLATE.activities_support_comment))
                .makeUrl()

        ToolsIntent.startWeb(url) {
            ToolsToast.show(t(API_TRANSLATE.error_app_not_found))
        }
        ToolsThreads.main(2000) {
            vDonateContainer.visibility = View.GONE
            vMessageContainer.visibility = View.VISIBLE
        }
    }

}