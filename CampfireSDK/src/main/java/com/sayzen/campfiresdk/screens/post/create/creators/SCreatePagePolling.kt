package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPagePolling
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.views.SplashSearchAccount
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsTitle
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsText

import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

class SCreatePagePolling(
        private val requestPutPage: (page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page) -> Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PagePolling?
) : Screen(R.layout.screen_post_create_polling) {

    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vOptionsLabel: TextView = findViewById(R.id.vOptionsLabel)
    private val vPageTitle: EditText = findViewById(R.id.vPgeTitle)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)
    private val vCreate: Button = findViewById(R.id.vCreate)
    private val vAdd: ViewButton = findViewById(R.id.vAdd)
    private val vLvl: SettingsField = findViewById(R.id.vLvl)
    private val vKarma: SettingsField = findViewById(R.id.vKarma)
    private val vDays: SettingsField = findViewById(R.id.vDays)
    private val vTitleLimit: SettingsTitle = findViewById(R.id.vTitleLimit)
    private val vBlackListTitle: SettingsTitle = findViewById(R.id.vBlackListTitle)
    private val vBlackListUsers: LinearLayout = findViewById(R.id.vBlackListUsers)
    private val vBlackListAdd: ViewButton = findViewById(R.id.vBlackListAdd)

    private val blackList: MutableList<Account> = mutableListOf()

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.post_page_polling))
        vTitleLimit.setTitle(t(API_TRANSLATE.app_limitations))
        vAdd.text = t(API_TRANSLATE.app_add)

        vOptionsLabel.text = t(API_TRANSLATE.post_page_polling_options)
        vCreate.text = t(API_TRANSLATE.app_create)
        vLvl.setHint(t(API_TRANSLATE.post_page_polling_lvl_title))
        vKarma.setHint(t(API_TRANSLATE.post_page_polling_karma_title))
        vDays.setHint(t(API_TRANSLATE.post_page_polling_days_title))

        vTitle.text = "${t(API_TRANSLATE.app_naming)} (${t(API_TRANSLATE.app_not_required)})"
        vPageTitle.addTextChangedListener(TextWatcherChanged { update() })
        if (oldPage != null) {
            vPageTitle.setText(oldPage.title)
            for (o in oldPage.options) addItem(o)
            if (!SplashAlert.check("ALERT_POLLING_CHANGE"))
                ToolsThreads.main(100) {
                    SplashAlert()
                            .setTopTitleText(t(API_TRANSLATE.app_attention))
                            .setCancelable(false)
                            .setTitleImageBackgroundRes(R.color.blue_700)
                            .setText(t(API_TRANSLATE.post_page_polling_change_alert))
                            .setChecker("ALERT_POLLING_CHANGE")
                            .setOnEnter(t(API_TRANSLATE.app_got_it))
                            .asSheetShow()
                }
            vCreate.text = t(API_TRANSLATE.app_change)
            if (oldPage.minLevel > 0) vLvl.setText("${oldPage.minLevel / 100}")
            if (oldPage.minKarma > 0) vKarma.setText("${oldPage.minKarma / 100}")
            if (oldPage.minDays > 0) vDays.setText(oldPage.minDays.toString())
        } else {
            addItem("")
            addItem("")
        }
        vLvl.addOnTextChanged { update() }
        vKarma.addOnTextChanged { update() }
        vDays.addOnTextChanged { update() }

        vLvl.setErrorChecker {
            try {
                if (it.isEmpty()) return@setErrorChecker null
                if (!ToolsText.isOnly(it, ToolsText.NUMBERS_S+".")) return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
                val x = it.toFloat()
                return@setErrorChecker if (x >= 1) null else t(API_TRANSLATE.error_incorrect_value)
            } catch (e: Exception) {
                return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
            }
        }

        vKarma.setErrorChecker {
            try {
                if (it.isEmpty()) return@setErrorChecker null
                if (!ToolsText.isOnly(it, ToolsText.NUMBERS_S)) return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
                val x = it.toInt()
                return@setErrorChecker if (x >= 0) null else t(API_TRANSLATE.error_incorrect_value)
            } catch (e: Exception) {
                return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
            }
        }

        vDays.setErrorChecker {
            try {
                if (it.isEmpty()) return@setErrorChecker null
                if (!ToolsText.isOnly(it, ToolsText.NUMBERS_S)) return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
                return@setErrorChecker if (it.toInt() >= 0) null else t(API_TRANSLATE.error_incorrect_value)
            } catch (e: Exception) {
                return@setErrorChecker t(API_TRANSLATE.error_incorrect_value)
            }
        }

        vBlackListTitle.setTitle(t(API_TRANSLATE.post_page_polling_blacklist))
        vBlackListAdd.text = t(API_TRANSLATE.app_add)
        vBlackListAdd.setOnClickListener {
            Navigator.to(SAccountSearch(showMyAccount = true) { account ->
                if (blackList.find { it.id == account.id } == null) {
                    addBlacklistItem(XAccount().setAccount(account))
                }
                if (vBlackListUsers.childCount >= API.PAGE_POLLING_BLACKLIST_MAX) {
                    vBlackListAdd.visibility = INVISIBLE
                }
            })
        }
        vBlackListAdd.visibility = if (vBlackListUsers.childCount >= API.PAGE_POLLING_BLACKLIST_MAX)
            INVISIBLE else VISIBLE

        if (oldPage != null) {
            for (account in oldPage.blacklist) {
                addBlacklistItem(XAccount().setAccount(account))
            }
        }

        vAdd.setOnClickListener { addItem("") }
        vCreate.setOnClickListener { onEnter() }
        update()
    }

    override fun onResume() {
        super.onResume()
        vPageTitle.requestFocus()
    }

    override fun onBackPressed(): Boolean {
        onCancel()
        return true
    }

    private fun addBlacklistItem(account: XAccount) {
        val vRow: LinearLayout = ToolsView.inflate(R.layout.screen_post_create_polling_blacklist_item)
        val vAvatar: ViewAvatarTitle = vRow.findViewById(R.id.vAvatar)
        val vRemove: ViewIcon = vRow.findViewById(R.id.vRemove)

        account.setView(vAvatar)

        vRemove.setOnClickListener {
            blackList.remove(account.getAccount())
            vBlackListUsers.removeView(vRow)
            vBlackListAdd.visibility = View.VISIBLE
        }

        vBlackListUsers.addView(vRow)
        blackList.add(account.getAccount())
    }

    private fun addItem(text: String) {
        val vItem: View = ToolsView.inflate(R.layout.screen_post_create_polling_item)
        val vText: EditText = vItem.findViewById(R.id.vText)
        val vRemove: View = vItem.findViewById(R.id.vRemove)

        vText.setText(text)
        vText.addTextChangedListener(TextWatcherChanged {
            vText.error = if (vText.text.length > API.PAGE_POLLING_OPTION_MAX_TEXT) " " else null
            update()
        })
        vText.requestFocus()
        vRemove.setOnClickListener {
            if (vText.text.isNotEmpty()) {
                SplashAlert()
                        .setText(t(API_TRANSLATE.post_page_polling_remove_confirm))
                        .setOnCancel(t(API_TRANSLATE.app_cancel))
                        .setOnEnter(t(API_TRANSLATE.app_remove)) {
                            vContainer.removeView(vItem)
                            update()
                        }
                        .asSheetShow()
            } else {
                vContainer.removeView(vItem)
                update()
            }
        }
        vContainer.addView(vItem, vContainer.childCount)

        update()
    }

    private fun update() {
        vAdd.isEnabled = vContainer.childCount <= API.PAGE_POLLING_OPTION_MAX_COUNT

        val options = getOptions()

        var enabled = options.isNotEmpty()

        for (s in options)
            if (s.isEmpty() || s.length > API.PAGE_POLLING_OPTION_MAX_TEXT) {
                enabled = false
                break
            }

        vCreate.isEnabled = enabled && vPageTitle.text.length <= API.PAGE_POLLING_TITLE_MAX && !vLvl.isError() && !vKarma.isError() && !vDays.isError()
    }

    private fun onEnter() {
        val page = PagePolling()
        page.title = vPageTitle.text.toString()
        page.options = getOptions()
        page.minLevel = if (vLvl.getText().isEmpty()) 0L else (vLvl.getText().toFloat() * 100).toLong()
        page.minKarma = if (vKarma.getText().isEmpty()) 0L else vKarma.getText().toLong() * 100
        page.minDays = if (vDays.getText().isEmpty()) 0L else vDays.getText().toLong()
        page.blacklist = blackList.toTypedArray()

        Navigator.back()
        val w = ToolsView.showProgressDialog()

        if (card == null)
            requestPutPage.invoke(page, this, w, { page1 -> CardPagePolling(null, page1 as PagePolling) }) {}
        else
            requestChangePage.invoke(page, card, null, w) {}
    }

    private fun onCancel() {
        if (notChanged()) Navigator.back()
        else SplashAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val options = getOptions()

        return if (oldPage == null) {
            options.size == 2 && options[0].isEmpty() && options[1].isEmpty() &&
                    vPageTitle.text.isEmpty() && blackList.isEmpty()
        } else {
            if (vPageTitle.text.toString() != oldPage.title) return false
            if (options.size != oldPage.options.size) return false
            for (i in options.indices)
                if (options[i] != oldPage.options[i]) return false
            if (blackList.size != oldPage.blacklist.size) return false
            for (i in blackList.indices)
                if (blackList[i] != oldPage.blacklist[i]) return false
            return true
        }
    }

    private fun getOptions() = Array(vContainer.childCount) {
        vContainer.getChildAt(it).findViewById<EditText>(R.id.vText).text.toString()
    }

}