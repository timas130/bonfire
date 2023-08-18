package com.sup.dev.android.tools

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.*
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.tools.ToolsColor
import java.util.*
import kotlin.collections.ArrayList


object ToolsResources {

    fun sex(sex: Long, @StringRes m: Int, @StringRes w: Int) = if (sex == 0L) s(m) else s(w)

    fun sex(sex: Long, @StringRes m: String, @StringRes w: String) = if (sex == 0L) m else w

    fun sCap(@StringRes r: Int, vararg args: Any): String {
        return s(r, *args).capitalize()
    }

    fun sCap(r: String, vararg args: Any): String {
        return s(r, *args).capitalize()
    }

    /*
      string_key_1, string_key_2, string_key_3...
      stringKey - "string_key_" or "string_%s_key"
    */
    fun getStringIndexedArray(stringKey: String): Array<String> {
        var index = 1
        val list = ArrayList<String>()
        while (true) {
            val x = getStringId(if (stringKey.contains("%s")) String.format(stringKey, index) else stringKey + index)
            index++
            if (x > 0) list.add(s(x))
            else break
        }
        return list.toTypedArray()
    }

    /*
      string_key_1, string_key_2, string_key_3...
      stringKey - "string_key_" or "string_%s_key"
    */
    fun getStringIndexedArrayId(stringKey: String, minimumSize: Int = 0): Array<Int> {
        var index = 1
        val list = ArrayList<Int>()
        while (true) {
            val x = getStringId(if (stringKey.contains("%s")) String.format(stringKey, index) else stringKey + index)
            index++
            if (x > 0) list.add(x)
            else if (index <= minimumSize+1) list.add(-1)
            else break
        }
        return list.toTypedArray()
    }

    fun s(@StringRes r: Int, vararg args: Any): String {
        return String.format(s(r), *args)
    }

    fun s(r: String, vararg args: Any): String {
        return String.format(r, *args)
    }

    fun getStringId(name: String): Int {
        return SupAndroid.appContext!!.resources.getIdentifier(name, "string", SupAndroid.appContext!!.packageName)
    }

    fun s(name: String): String {
        return s(getStringId(name))
    }

    fun s(@StringRes r: Int): String {
        if (r == 0) throw IllegalArgumentException("Bad string resource id[$r]")
        return SupAndroid.appContext!!.resources.getString(r)
    }

    @Suppress("DEPRECATION")
    fun sLang(languageCode: String, @StringRes r: Int): String {
        val res = SupAndroid.appContext!!.resources
        val conf = res.configuration
        val savedLocale = conf.locale
        conf.locale = Locale(languageCode)
        res.updateConfiguration(conf, null)

        val str = res.getString(r)

        conf.locale = savedLocale
        res.updateConfiguration(conf, null)

        return str
    }

    fun getPlural(@PluralsRes r: Int, value: Int): String {
        return SupAndroid.appContext!!.resources.getQuantityString(r, value)
    }

    fun getStringArray(@ArrayRes r: Int): Array<String> {
        return SupAndroid.appContext!!.resources.getStringArray(r)
    }

    @Suppress("DEPRECATION")
    fun getDrawable(@DrawableRes r: Int): Drawable {
        return SupAndroid.appContext!!.resources.getDrawable(r)
    }

    @SuppressLint("ResourceType")
    fun getDrawableAsBytes(@DrawableRes r: Int): ByteArray {
        return SupAndroid.appContext!!.resources.openRawResource(r).readBytes()
    }

    fun getDrawableAttr(@AttrRes r: Int) = getDrawableAttrNullable(r)!!

    fun getDrawableAttrNullable(@AttrRes r: Int): Drawable? {
        val attrs = intArrayOf(r)
        val ta = SupAndroid.activity!!.obtainStyledAttributes(attrs)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable
    }

    fun getDrawableAttrId(@AttrRes r: Int): Int {
        val attrs = intArrayOf(r)
        val ta = SupAndroid.activity!!.obtainStyledAttributes(attrs)
        val id = ta.getResourceId(0, 0)
        ta.recycle()
        return id
    }

    fun getColorAttr(@AttrRes r: Int, def: Int = 0x00000000) = getColorAttr(SupAndroid.activity!!, r, def)

    fun getColorAttr(context: Context, @AttrRes r: Int, def: Int = 0x00000000): Int {
        val attrs = intArrayOf(r)
        val ta = context.obtainStyledAttributes(attrs)
        val color = ta.getColor(0, def)
        ta.recycle()
        return color
    }

    fun getBooleanAttr(@AttrRes r: Int, def: Boolean = false): Boolean {
        val attrs = intArrayOf(r)
        val ta = SupAndroid.activity!!.obtainStyledAttributes(attrs)
        val b = ta.getBoolean(0, def)
        ta.recycle()
        return b
    }

    fun getStream(r: Int) = SupAndroid.appContext!!.resources.openRawResource(+r)

    fun getColorId(name: String): Int {
        return SupAndroid.appContext!!.resources.getIdentifier(name, "color", SupAndroid.appContext!!.packageName)
    }

    fun getColor(name: String): Int {
        return getColor(getColorId(name))
    }

    @Suppress("DEPRECATION")
    fun getColor(@ColorRes r: Int): Int {
        return SupAndroid.appContext!!.resources.getColor(r)
    }

    @Suppress("DEPRECATION")
    fun getColors(@ColorRes vararg r: Int): Array<Int> {
        return Array(r.size) { SupAndroid.appContext!!.resources.getColor(r[it]) }
    }

    fun getSecondaryColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorSecondary, value, true)
        return value.data
    }

    fun getPrimaryColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
        return value.data
    }

    fun getPrimaryDarkColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimaryVariant, value, true)
        return value.data
    }

    fun getSecondaryAlphaColor(context: Context): Int {
        return ToolsColor.setAlpha(106, getSecondaryColor(context))
    }

    fun getBackgroundColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(android.R.attr.windowBackground, value, true)
        return value.data
    }

    fun getBitmap(@DrawableRes res: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inMutable = true
        val bitmap = BitmapFactory.decodeResource(SupAndroid.appContext!!.resources, res, options)

        return bitmap ?: ToolsBitmap.getFromDrawable(getDrawable(res))!!

    }

    fun getDrawable(name: String): Drawable {
        return getDrawable(getDrawableId(name))
    }

    fun getDrawableId(name: String): Int {
        return SupAndroid.appContext!!.resources.getIdentifier(name, "drawable", SupAndroid.appContext!!.packageName)
    }

    fun getBitmap(name: String): Bitmap {
        return getBitmap(getDrawableId(name))
    }

}
