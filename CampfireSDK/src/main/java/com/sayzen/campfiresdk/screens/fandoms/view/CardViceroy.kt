package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminViceroyAssign
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminViceroyRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsViceroyGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.SplashMenu

class CardViceroy(
        val fandomId: Long,
        val languageId: Long
) : Card(R.layout.screen_fandom_card_viceroy) {

    var date = 0L
    var loading = true
    var error = false
    var xAccount: XAccount? = null


    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vRetry: TextView = view.findViewById(R.id.vRetry)
        val vLoading: TextView = view.findViewById(R.id.vLoading)
        val vEmpty: TextView = view.findViewById(R.id.vEmpty)

        vMenu.setOnClickListener { showMenu(vMenu) }
        vRetry.setOnClickListener { load() }

        vEmpty.text = t(API_TRANSLATE.fandom_viceroy_empty)
        vTitle.text = t(API_TRANSLATE.app_viceroy)
        vLoading.text = t(API_TRANSLATE.app_loading)
        vRetry.text = t(API_TRANSLATE.app_retry)

        vRetry.visibility = if (error) View.VISIBLE else View.GONE
        vLoading.visibility = if (loading) View.VISIBLE else View.GONE
        vMenu.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_VICEROY) && !error && !loading) View.VISIBLE else View.GONE
        vAvatar.visibility = if (!error && !loading && xAccount != null) View.VISIBLE else View.GONE
        vEmpty.visibility = if (!error && !loading && xAccount == null) View.VISIBLE else View.GONE

        if (xAccount != null) {
            xAccount!!.setView(vAvatar)
            vAvatar.setSubtitle("${((ControllerApi.currentTime() - date) / (1000L * 60 * 60 * 24)) + 1} ${t(API_TRANSLATE.app_d)}")
        }
    }

    fun setParams(account:Account?, date:Long){
        loading = false
        this.date = date
        if (account != null) xAccount = XAccount().setAccount(account).setOnChanged { update() }
        update()
    }

    private fun load(tryCount: Int = 5) {
        error = false
        loading = true
        if (tryCount <= 0) {
            error = true
            loading = false
            update()
            return
        }
        update()
        RFandomsViceroyGet(fandomId, languageId)
                .onComplete {
                    setParams(it.account, it.date)
                    update()
                }
                .onError { load(tryCount - 1) }
                .send(api)
    }



    private fun reload(){
        xAccount = null
        load()
    }

    private fun showMenu(v:View){
        SplashMenu()
                .add(t(API_TRANSLATE.fandom_viceroy_assign)){
                    assignViceroy()
                }.textColorRes(R.color.red_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandom_viceroy_remove)){
                    removeViceroy()
                }.condition(xAccount != null).textColorRes(R.color.red_700).textColorRes(R.color.white)
                .asPopupShow(v)
    }

    private fun assignViceroy(){
        Navigator.to(SAccountSearch(true, true) { account ->
            ControllerApi.moderation(t(API_TRANSLATE.fandom_viceroy_assign_text), t(API_TRANSLATE.app_assign), {RFandomsAdminViceroyAssign(fandomId, languageId, account.id, it)}){
                ToolsToast.show(t(API_TRANSLATE.app_done))
                reload()
            }
        })
    }

    private fun removeViceroy(){
        ControllerApi.moderation(t(API_TRANSLATE.fandom_viceroy_remove_text), t(API_TRANSLATE.app_remove), {RFandomsAdminViceroyRemove(fandomId, languageId, it)}){
            ToolsToast.show(t(API_TRANSLATE.app_done))
            reload()
        }
    }


}
