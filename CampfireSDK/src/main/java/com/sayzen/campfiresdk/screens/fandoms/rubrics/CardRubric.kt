package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.Rubric
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerRubrics
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeName
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeOwner
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricRemove
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardRubric(val rubric: Rubric) : Card(R.layout.card_rubric) {

    var xAccount = XAccount().setAccount(rubric.owner).setOnChanged { update() }
    val xFandom = XFandom().setFandom(rubric.fandom).setOnChanged { update() }
    var onClick: ((Rubric) -> Unit)? = null
    var showFandom = false
    var canCreatePost = false

    val eventBus = EventBus
            .subscribe(EventRubricChangeName::class) {
                if (rubric.id == it.rubricId) {
                    rubric.name = it.rubricName
                    update()
                }
            }
            .subscribe(EventRubricChangeOwner::class) {
                if (rubric.id == it.rubricId) {
                    rubric.owner = it.owner
                    xAccount = XAccount().setAccount(rubric.owner).setOnChanged { update() }
                    update()
                }
            }
            .subscribe(EventRubricRemove::class) {
                if (rubric.id == it.rubricId) {
                    adapter.remove(this)
                }
            }

    override fun bindView(view: View) {

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRate: TextView = view.findViewById(R.id.vRate)
        val vWaitForPost: View = view.findViewById(R.id.vWaitForPost)
        val vCreate: View = view.findViewById(R.id.vCreate)
        val vTouch: View = view.findViewById(R.id.vTouch)

        vCreate.visibility = if (ControllerApi.isCurrentAccount(xAccount.getId()) && canCreatePost) View.VISIBLE else View.GONE
        vCreate.setOnClickListener { SPostCreate.instance(xFandom.getId(), xFandom.getLanguageId(), xFandom.getName(), xFandom.getImageId(), SPostCreate.PostParams().setRubric(rubric), Navigator.TO) }

        if (showFandom) xFandom.setView(vAvatar) else xAccount.setView(vAvatar)
        vAvatar.setTitle(rubric.name)
        vAvatar.setSubtitle(rubric.owner.name)
        vAvatar.vAvatar.isClickable = onClick == null
        vAvatar.isClickable = false

        vRate.text = ToolsText.numToStringRound(rubric.karmaCof / 100.0, 2)
        vWaitForPost.visibility = if (rubric.isWaitForPost) View.VISIBLE else View.INVISIBLE

        vTouch.setOnClickListener {
            if (onClick == null) {
                Navigator.to(SRubricPosts(rubric))
            } else {
                onClick?.invoke(rubric)
            }
        }

        vTouch.setOnLongClickListener {
            ControllerRubrics.instanceMenu(rubric).asSheetShow()
            true
        }
    }


}