package com.sayzen.campfiresdk.controllers

import android.widget.EditText
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.views.SplashSearch
import com.sayzen.campfiresdk.views.SplashSearchAccount
import com.sayzen.campfiresdk.views.SplashSearchFandom
import com.sup.dev.android.tools.ToolsView

object ControllerMention {

    private val wAccountSearch = SplashSearchAccount { field, account -> setMentionAccount(field, account.name) }.makeCompanion() as SplashSearchAccount
    private val wFandomSearch = SplashSearchFandom { field, fandom -> setMentionFandom(field, fandom.id) }.makeCompanion() as SplashSearchFandom

    fun startFor(vField: EditText, addSpace: Boolean = true) {
        var firstTime = true
        ToolsView.onSelectionChanged(vField) {
            if (firstTime) {
                firstTime = false
                return@onSelectionChanged
            }
            updateMention(vField, addSpace)
        }
    }

    fun updateMention(vField: EditText, addSpace: Boolean = true): Boolean {
        val filed = Field(vField, addSpace)
        if (updateMentionAccount(filed)) return true
        if (updateMentionFandom(filed)) return true
        return false
    }

    fun hide() {
        wAccountSearch.hide()
        wFandomSearch.hide()
    }

    //
    //  Account
    //

    private fun searchForAccount(field: Field): SearchResult? = search(field, API.LINK_SHORT_PROFILE, API.ACCOUNT_LOGIN_CHARS, 20)?:search(field, API.LINK_SHORT_PROFILE_SECOND, API.ACCOUNT_LOGIN_CHARS, 20)

    private fun updateMentionAccount(field: Field) = updateMention(field, searchForAccount(field), wAccountSearch)

    private fun setMentionAccount(field: Field, name: String) = replace(field, name, true, searchForAccount(field))

    //
    //  Fandom
    //

    private fun searchForFandom(field: Field): SearchResult? = search(field, "@f_", API.FANDOM_NAME_CHARS + "_", 20)

    private fun updateMentionFandom(field: Field) = updateMention(field, searchForFandom(field), wFandomSearch)

    private fun setMentionFandom(field: Field, id: Long) = replace(field, "fandom_$id", true, searchForFandom(field))


    //
    //  Logic
    //

    private fun replace(field: Field, name: String, supportSpace: Boolean, searchResult: SearchResult?): Boolean {
        if (searchResult == null) return false
        val vField = field.vField
        val s = vField.text.toString()
        val n = name + if (supportSpace && field.addSpace) " " else ""

        val index = vField.selectionStart
        vField.setText(s.replaceRange(searchResult.index, index, n))
        vField.setSelection(index + (n.length - (index - searchResult.index)))
        return true
    }

    private fun updateMention(field: Field, searchResult: SearchResult?, widget: SplashSearch): Boolean {
        val vField = field.vField
        val name = searchResult?.text?.trim()

        if (name != null) {
            if (widget.isHided()) {
                val point = ToolsView.getSelectionPosition(vField)
                widget.show(point, field)
            }
            widget.setSearchName(name)
            return true
        } else {
            widget.hide()
            return false
        }
    }

    private fun search(field: Field, prefix: String, allowedChars: String, charsCount: Int): SearchResult? {
        val vField = field.vField

        val s = vField.text.toString()
        var index = vField.selectionStart

        if (index < prefix.length) return null


        if (index > charsCount) {
            if (!s.substring(index - charsCount, index).toLowerCase().contains(prefix)) return null
        } else {
            if (!s.substring(0, index).toLowerCase().contains(prefix)) return null
        }

        var x = charsCount
        var found = false
        var text = ""

        while (index > 0 && x > -1) {
            val c = ("${s[index - 1]}").toLowerCase()
            text = c + text
            if (text.contains(prefix)) {
                found = true
                break
            }
            if (!allowedChars.contains(c)) break

            index--
            x--
        }

        if (!found) return null
        text = text.removeRange(0, prefix.length)
        return SearchResult(text, index)
    }

    private class SearchResult(
            val text: String,
            val index: Int
    )

    class Field(
            val vField: EditText,
            val addSpace: Boolean
    )

}