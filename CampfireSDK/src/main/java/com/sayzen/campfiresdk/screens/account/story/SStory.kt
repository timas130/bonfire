package com.sayzen.campfiresdk.screens.account.story

import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsGetStory
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewText

class SStory(
        accountName: String,
        r : RAccountsGetStory.Response
) : Screen(R.layout.screen_account_story){

    companion object {

        fun instance(accountId: Long, accountName: String, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RAccountsGetStory(accountId)) { r ->
                SStory(accountName, r)
            }
        }
    }

    private val vKarmaTotal:TextView = findViewById(R.id.vKarmaTotal)
    private val vKarmaTotalPlus:TextView = findViewById(R.id.vKarmaTotalPlus)
    private val vKarmaTotalMinus:TextView = findViewById(R.id.vKarmaTotalMinus)
    private val vRatesTotal:TextView = findViewById(R.id.vRatesTotal)
    private val vRatesTotalPlus:TextView = findViewById(R.id.vRatesTotalPlus)
    private val vRatesTotalMinus:TextView = findViewById(R.id.vRatesTotalMinus)
    private val vTitleKarma:TextView = findViewById(R.id.vTitleKarma)
    private val vTitlePublications:TextView = findViewById(R.id.vTitlePublications)

    private val vPosts:TextView = findViewById(R.id.vPosts)
    private val vComments:TextView = findViewById(R.id.vComments)
    private val vMessages:TextView = findViewById(R.id.vMessages)
    private val vBestPost:ViewText = findViewById(R.id.vBestPost)
    private val vBestComment:ViewText = findViewById(R.id.vBestComment)

    private val vTextKarmaTotal: TextView = findViewById(R.id.vTextKarmaTotal)
    private val vTextKarmaTotalPlus: TextView = findViewById(R.id.vTextKarmaTotalPlus)
    private val vTextKarmaTotalMinus: TextView = findViewById(R.id.vTextKarmaTotalMinus)
    private val vTextRatesTotal: TextView = findViewById(R.id.vTextRatesTotal)
    private val vTextRatesTotalPlus: TextView = findViewById(R.id.vTextRatesTotalPlus)
    private val vTextRatesTotalMinus: TextView = findViewById(R.id.vTextRatesTotalMinus)
    private val vTextPosts: TextView = findViewById(R.id.vTextPosts)
    private val vTextComments: TextView = findViewById(R.id.vTextComments)
    private val vTextMessages: TextView = findViewById(R.id.vTextMessages)
    private val vTextBestPost: TextView = findViewById(R.id.vTextBestPost)
    private val vTextBestComment: TextView = findViewById(R.id.vTextBestComment)

    init {
        disableNavigation()
        disableShadows()

        setTitle(accountName + " " + t(API_TRANSLATE.profile_story))
        vTextKarmaTotal.text = t(API_TRANSLATE.profile_story_karma_total)
        vTextKarmaTotalPlus.text =  t(API_TRANSLATE.profile_story_karma_total_plus)
        vTextKarmaTotalMinus.text =  t(API_TRANSLATE.profile_story_karma_total_minus)
        vTextRatesTotal.text =  t(API_TRANSLATE.profile_story_rates_total)
        vTextRatesTotalPlus.text =  t(API_TRANSLATE.profile_story_rates_total_plus)
        vTextRatesTotalMinus.text =  t(API_TRANSLATE.profile_story_rates_total_minus)
        vTextPosts.text =  t(API_TRANSLATE.profile_story_posts)
        vTextComments.text =  t(API_TRANSLATE.profile_story_comments)
        vTextMessages.text =  t(API_TRANSLATE.profile_story_messages)
        vTextBestPost.text =  t(API_TRANSLATE.profile_story_best_post)
        vTextBestComment.text =  t(API_TRANSLATE.profile_story_best_comment)
        vTitleKarma.text =  t(API_TRANSLATE.app_karma)
        vTitlePublications.text =  t(API_TRANSLATE.app_publications)

        vKarmaTotal.text = "${(r.totalKarmaPlus + r.totalKarmaMinus)/100}"
        vKarmaTotalPlus.text = "${r.totalKarmaPlus/100}"
        vKarmaTotalMinus.text = "${r.totalKarmaMinus/100}"
        vKarmaTotal.setTextColor(ToolsResources.getColor(if(r.totalKarmaPlus + r.totalKarmaMinus < 0)R.color.red_700 else R.color.green_700))

        vRatesTotal.text = "${(r.totalRatesPlus + r.totalRatesMinus)/100}"
        vRatesTotalPlus.text = "${r.totalRatesPlus/100}"
        vRatesTotalMinus.text = "${r.totalRatesMinus/100}"
        vRatesTotal.setTextColor(ToolsResources.getColor(if(r.totalRatesPlus + r.totalRatesMinus < 0)R.color.red_700 else R.color.green_700))


        vPosts.text = "${r.totalPosts}"
        vComments.text = "${r.totalComments}"
        vMessages.text = "${r.totalMessages}"
        vBestPost.text = if(r.bestPost == 0L) "-" else ControllerLinks.linkToPost(r.bestPost)
        vBestComment.text = if(r.bestComment == 0L) "-" else ControllerLinks.linkToComment(r.bestComment, r.bestCommentUnitType, r.bestCommentUnitId)

        ControllerLinks.makeLinkable(vBestPost)
        ControllerLinks.makeLinkable(vBestComment)
    }


}
