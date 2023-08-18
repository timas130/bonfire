package com.sup.dev.java.tools

object ToolsHTML {

    val color_aqua = "aqua"
    val color_black = "black"
    val color_blue = "blue"
    val color_fuchsia = "fuchsia"
    val color_green = "green"
    val color_grey = "grey"
    val color_lime = "lime"
    val color_maroon = "maroon"
    val color_navy = "navy"
    val color_olive = "olive"
    val color_purple = "purple"
    val color_red = "red"
    val color_silver = "silver"
    val color_teal = "teal"
    val color_white = "white"
    val color_yellow = "yellow"


    val a = "a"
    val big = "big"
    val small = "small"
    val b = "b"
    val blockquote = "blockquote"
    val br = "br"
    val cite = "cite"
    val div = "div"
    val em = "em"
    val i = "i"
    val p = "p"
    val strong = "strong"
    val sub = "sub"
    val sup = "sup"
    val tt = "tt"
    val u = "u"
    val s = "s"
    val strike = "strike"
    val font = "font"

    //
    //  Methods
    //

    fun convertSelection(htmlText: String, start: Int, end: Int): Array<Int> {
        var s = 0
        var e = 0
        var less = 0
        var skip = false
        var tagsCount = 0

        run {
            var i = 0
            while (i < htmlText.length && (end > less || tagsCount > 0)) {
                if (less < start)
                    s++
                e++
                val c = htmlText[i]
                if (skip) {
                    if (c == '>') {
                        tagsCount--
                        skip = false
                    } else if (c == ';') {
                        less++
                        skip = false
                    }
                } else {
                    if (c == '<') {
                        if (htmlText[i + 1] != '/')
                            tagsCount += 2
                        skip = true
                    } else if (c == '&')
                        skip = true
                    else
                        less++
                }
                i++
            }
        }
        return arrayOf(s, e)
    }

    fun removeTag(text: String, tag: String): String {
        return text.replace("<$tag>".toRegex(), "").replace("</$tag>".toRegex(), "")
    }

    //
    //  Tags
    //

    //  ССылка
    fun a(link: String, name: String): String {
        return "<" + a + " href=\"" + ToolsText.castToWebLink(link) + "\">" + name + "</" + a + ">"
    }

    fun big(text: String): String {
        return "<$big>$text</$big>"
    }

    fun small(text: String): String {
        return "<$small>$text</$small>"
    }

    fun font_color(text: String, colorKey: String): String {
        return "<$font color=\"$colorKey\">$text</$font>"
    }
    /*

     */

    //  Жирный
    fun b(text: String): String {
        return "<$b>$text</$b>"
    }

    fun blockquote(text: String): String {
        return "<$blockquote>$text</$blockquote>"
    }

    fun br(text: String): String {
        return "<$br>$text</$br>"
    }

    fun cite(text: String): String {
        return "<$cite>$text</$cite>"
    }

    operator fun div(text: String): String {
        return "<$div>$text</$div>"
    }

    fun em(text: String): String {
        return "<$em>$text</$em>"
    }

    //  Наклон
    fun i(text: String): String {
        return "<$i>$text</$i>"
    }

    fun p(text: String): String {
        return "<$p>$text</$p>"
    }

    fun strong(text: String): String {
        return "<$strong>$text</$strong>"
    }

    fun sub(text: String): String {
        return "<$sub>$text</$sub>"
    }

    fun sup(text: String): String {
        return "<$sup>$text</$sup>"
    }

    fun tt(text: String): String {
        return "<$tt>$text</$tt>"
    }

    //  Подчеркивание
    fun u(text: String): String {
        return "<$u>$text</$u>"
    }

    //  Зачеркнутый
    fun s(text: String): String {
        return "<$s>$text</$s>"
    }

    fun strike(text: String): String {
        return "<$strike>$text</$strike>"
    }


}
