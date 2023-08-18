package com.sayzen.campfiresdk.screens.translates

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter

class STranslates : Screen(R.layout.screen_translates) {

    val vRecycler:RecyclerView = findViewById(R.id.vRecycler)
    val vMenu:View = findViewById(R.id.vMenu)
    val adapter = RecyclerCardAdapter()
    val cards = ArrayList<CardTranslate>()
    val cardMenu = CardTranslateMenu(this)

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.app_translates))

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        adapter.add(cardMenu)

        vMenu.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.translates_label_history)){ Navigator.to(STranslatesHistory(getFromLanguage(), "")) }
                    .add(t(API_TRANSLATE.translates_label_history_all)){ Navigator.to(STranslatesHistory(0, "")) }
                    .add(t(API_TRANSLATE.app_copy_link)){
                        ToolsAndroid.setToClipboard(API.LINK_TRANSLATES.asWeb())
                        ToolsToast.show(t(API_TRANSLATE.app_copied))
                    }
                    .asPopupShow(vMenu)
        }
    }

    override fun onFirstShow() {
        super.onFirstShow()
        reload()
    }

    fun reload(){
        adapter.remove(CardTranslate::class)
        cards.clear()
        ControllerTranslate.loadLanguage(getTargetLanguage(),
                {
                    for (i in API_TRANSLATE.map.values) cards.add(CardTranslate(i.key, this))
                    update()
                },
                {
                    ToolsToast.show(t(API_TRANSLATE.error_unknown))
                    Navigator.remove(this)
                })
    }

    fun update(){
        adapter.remove(CardTranslate::class)
        for(c in cards){
            update_1(c)
        }
        cardMenu.isEmpty = adapter.get(CardTranslate::class).isEmpty()
        cardMenu.update()
    }

    fun update_1(c:CardTranslate){
        val t = cardMenu.searchText.toLowerCase()
        if(t.isEmpty()) update_2(c)
        else{
            if(c.key.toLowerCase().contains(t))update_2(c)
            else{
                val baseText = ControllerTranslate.t(getTargetLanguage(), c.key)
                if(baseText != null && baseText.toLowerCase().contains(t))update_2(c)
                else {
                    val translateText = ControllerTranslate.t(getFromLanguage(), c.key)
                    if(translateText != null && translateText.toLowerCase().contains(t))update_2(c)
                }
            }
        }
    }

    fun update_2(c:CardTranslate){
        if(getOnlyWithoutTranslate()){
            val translateText = ControllerTranslate.t(getTargetLanguage(), c.key)
            if(translateText != null) return
        }
        adapter.add(c)
    }

    fun setTargetLanguage(id:Long){
        ToolsStorage.put("STranslates.baseLanguageId", id)
    }

    fun setFromLanguage(id:Long){
        ToolsStorage.put("STranslates.myLanguageId", id)
    }

    fun setOnlyWithoutTranslates(b:Boolean){
        ToolsStorage.put("STranslates.onlyWithoutTranslates", b)
    }

    fun getTargetLanguage():Long{
        return ToolsStorage.getLong("STranslates.baseLanguageId", if(ToolsAndroid.getLanguageCode().toLowerCase() == "ru" || ToolsAndroid.getLanguageCode().toLowerCase() == "uk") API.LANGUAGE_RU else API.LANGUAGE_EN)
    }

    fun getFromLanguage():Long{
        return ToolsStorage.getLong("STranslates.myLanguageId", ControllerApi.getLanguageId())
    }

    fun getOnlyWithoutTranslate():Boolean{
        return ToolsStorage.getBoolean("STranslates.onlyWithoutTranslates", false)
    }
}
