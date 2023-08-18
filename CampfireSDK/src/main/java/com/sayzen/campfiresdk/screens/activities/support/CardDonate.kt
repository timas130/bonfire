package com.sayzen.campfiresdk.screens.activities.support

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.project.Donate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class CardDonate(
        val donate: Donate,
        val avatarClickable:Boolean = true,
        val onClick: (Donate) -> Unit = {  SProfile.instance(donate.account, Navigator.TO)}
) : Card(R.layout.screen_donates_card) {

    private val xAccount = XAccount().setAccount(donate.account).setOnChanged { update() }
    private var flash = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vCounter: TextView = view.findViewById(R.id.vCounter)

        xAccount.setView(vAvatar)
        if (!donate.isSum && !donate.isDraft) {
            vAvatar.setSubtitle(donate.comment)
            ControllerLinks.makeLinkable(vAvatar.vSubtitle)
        } else if (donate.isDraft) {
            vAvatar.setSubtitle(""+ToolsDate.dateToString(donate.dateCreate)+" " + donate.comment)
            ControllerLinks.makeLinkable(vAvatar.vSubtitle)
        }
        vCounter.text = "${ToolsText.numToStringRoundAndTrim(donate.sum / 100.0, 2)} \u20BD"

        view.setOnClickListener {
            onClick.invoke(donate)
        }

        vAvatar.isClickable = avatarClickable
       // vAvatar.vAvatar.isClickable = avatarClickable
       // vAvatar.vSubtitle.isClickable = avatarClickable
       // vAvatar.vTitle.isClickable = avatarClickable

        updateFlash()

    }


    fun updateFlash() {
        val view: View = getView()?:return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (animationFlash != null) view.foreground = ColorDrawable(animationFlash!!.color)
            else view.foreground = ColorDrawable(0x00000000)
        }

        if (flash) {
            flash = false
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus_dark)), ToolsResources.getColor(R.color.focus_dark), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash?.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    {
                        animationFlash?.update()
                        ToolsThreads.main { updateFlash() }
                    },
                    {
                        ToolsThreads.main {
                            animationFlash = null
                            updateFlash()
                        }
                    })
        }
    }

    fun flash() {
        flash = true
        updateFlash()
    }


}