package com.sayzen.campfiresdk.screens.fandoms.search


import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.objects.FandomParam
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow


class SFandomsSearchParams(
        name: String,
        var categoryId: Long,
        var params1: Array<Long>,
        var params2: Array<Long>,
        var params3: Array<Long>,
        var params4: Array<Long>
) : Screen(R.layout.screen_fandoms_search_params) {

    private val vText: SettingsField = findViewById(R.id.vText)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vCategoriesContainer: LayoutFlow = findViewById(R.id.vCategoriesContainer)
    private val vCategoriesTitle: TextView = findViewById(R.id.vCategoriesTitle)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)

    private var onFinish: ((String, Long, Array<Long>, Array<Long>, Array<Long>, Array<Long>) -> Unit)? = null

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_search))
        vCategoriesTitle.text = t(API_TRANSLATE.app_category)
        vFab.setOnClickListener { onEnter() }
        ToolsView.onFieldEnterKey(vText.vField) { onEnter() }

        vText.setHint(t(API_TRANSLATE.app_name_s))
        vText.setText(name)

        if(SFandomsSearch.ROOT_CATEGORY_ID > 0) {
            categoryId = SFandomsSearch.ROOT_CATEGORY_ID
            vCategoriesContainer.visibility = View.GONE
            vCategoriesTitle.visibility = View.GONE
        }


        addCategory(0, t(API_TRANSLATE.app_all))
        for (g in CampfireConstants.CATEGORIES) addCategory(g.index, g.name)

        if (getSelectedCategory() == 0L) (vCategoriesContainer.getChildAt(0) as ViewChip).isChecked = true
        switchParams()
    }

    private fun addCategory(id: Long, name: String) {
        val v = ViewChip.instanceChoose(context, name, id)
        v.isChecked = id == categoryId
        v.setOnClickListener {
            for (i in 0 until vCategoriesContainer.childCount) {
                val vv = vCategoriesContainer.getChildAt(i) as ViewChip
                vv.isChecked = vv == v
            }
            params1 = emptyArray()
            params2 = emptyArray()
            params3 = emptyArray()
            params4 = emptyArray()
            switchParams()
        }
        vCategoriesContainer.addView(v)
    }

    override fun onResume() {
        super.onResume()
        vText.showKeyboard()
    }

    private fun onEnter() {
        Navigator.back()
        if (onFinish != null) onFinish!!.invoke(vText.getText().trim(),
                getSelectedCategory(),
                paramGet(1),
                paramGet(3),
                paramGet(5),
                paramGet(6)
        )
    }

    fun onFinish(onFinish: ((String, Long, Array<Long>, Array<Long>, Array<Long>, Array<Long>) -> Unit)): SFandomsSearchParams {
        this.onFinish = onFinish
        return this
    }

    private fun switchParams() {

        val category = getSelectedCategory()
        vContainer.removeAllViews()

        if(category != 0L) {
            if (CampfireConstants.getParamTitle(category, 1) != null) addParams(CampfireConstants.getParamTitle(category, 1)!!, CampfireConstants.getParams(category, 1)!!, params1)
            if (CampfireConstants.getParamTitle(category, 2) != null) addParams(CampfireConstants.getParamTitle(category, 2)!!, CampfireConstants.getParams(category, 2)!!, params2)
            if (CampfireConstants.getParamTitle(category, 3) != null) addParams(CampfireConstants.getParamTitle(category, 3)!!, CampfireConstants.getParams(category, 3)!!, params3)
            if (CampfireConstants.getParamTitle(category, 4) != null) addParams(CampfireConstants.getParamTitle(category, 4)!!, CampfireConstants.getParams(category, 4)!!, params4)
        }
    }

    private fun getSelectedCategory(): Long {
        for (i in 0 until vCategoriesContainer.childCount) {
            val v = vCategoriesContainer.getChildAt(i) as ViewChip
            if (v.isChecked) return v.tag as Long
        }
        return 0
    }

    //
    //  Infos
    //

    var clickLocked = false

    private fun addParams(title: String, params: Array<FandomParam>, selected: Array<Long> ) {
        val vTitle: TextView = ToolsView.inflate(R.layout.screen_fandoms_search_params_title)
        val vFlow: LayoutFlow = ToolsView.inflate(R.layout.screen_fandoms_search_params_flow)

        vTitle.text = title

        val vv = ViewChip.instanceChoose(context, t(API_TRANSLATE.app_all))
        vv.setOnCheckedChangeListener { _, _ -> onChipSelected(vFlow, true) }
        vv.isChecked = selected.isEmpty()
        vFlow.addView(vv)

        for (i in params) {
            val v = ViewChip.instanceChoose(context, i.name, i)
            v.setOnCheckedChangeListener { _, _ -> onChipSelected(vFlow, false) }
            for (genre in selected) if (genre == i.index) v.isChecked = true
            vFlow.addView(v)
        }

        vContainer.addView(vTitle)
        vContainer.addView(vFlow)

    }

    private fun onChipSelected(container: ViewGroup, isAll: Boolean) {
        if(clickLocked) return
        clickLocked = true
        if (isAll) {
            for (i in 1 until container.childCount) {
                val v = container.getChildAt(i) as ViewChip
                v.isChecked = false
            }
        } else {
            val v = container.getChildAt(0) as ViewChip
            v.isChecked = false
        }
        clickLocked = false
    }

    private fun paramGet(index: Int): Array<Long> {
        if(vContainer.childCount <= index) return emptyArray()
        val indexes = ArrayList<Long>()
        val container = vContainer.getChildAt(index) as ViewGroup
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i) as ViewChip
            if (i == 0) {
                if (v.isChecked) break
            } else {
                if (v.isChecked) indexes.add((v.tag as FandomParam).index)
            }
        }
        return indexes.toTypedArray()
    }


}