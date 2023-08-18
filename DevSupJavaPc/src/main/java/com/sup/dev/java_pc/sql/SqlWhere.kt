package com.sup.dev.java_pc.sql


class SqlWhere {

    private val wheres = ArrayList<Where>()
    private var link: String? = null
    private var used = false

    val wheresCount: Int
        get() = wheres.size

    internal constructor(link: String?) {
        this.link = link
    }

    internal constructor(link: String, vararg wheres: Where) {
        this.link = link
        for (where in wheres)
            addWhere(where)
    }

    internal fun addWhere(where: Where) {
        used = true
        this.wheres.add(where)
    }

    internal fun toQuery(useLink: Boolean): String {
        if (!used) return ""
        val s: StringBuilder = if (useLink) StringBuilder(link!!)
        else StringBuilder()
        s.append("(").append(wheres[0].toQuery())
        for (i in 1 until wheres.size)
            s.append(" ").append(wheres[i - 1].link).append(" ").append(wheres[i].toQuery())
        return "$s)"
    }

    //
    //  Wheres
    //

    abstract class Where (val link: String) {

        abstract fun toQuery(): String

    }

    class WhereString constructor(var where: String, link: String = "AND") : Where(link) {

        override fun toQuery(): String {
            return where
        }
    }

    class WhereColumn constructor(var column: Any, var condition: String = "=", var value: Any = "?", link: String = "AND") : Where(link) {

        override fun toQuery(): String {
            return column.toString() + condition + value
        }
    }

    class WhereLIKE constructor(
        private val column: String,
        private val caseSensitive: Boolean = true,
        link: String = "AND",
    ) : Where(link) {

        override fun toQuery(): String {
            return if (caseSensitive) "$column LIKE(?)" else "${Sql.LOWER(column)} LIKE (?)"
        }
    }

    @Suppress("UNCHECKED_CAST")
    class WhereIN(private val column: String, link: String, private val not: Boolean, var values: Array<out Any?>) : Where(link) {

        constructor(column: String, values: ArrayList<out Any?>) : this(column, "AND", false, emptyArray()){
            this.values = arrayOfNulls(values.size)
            for(i in 0 until  values.size) (this.values as Array<Any?>)[i] = values[i]
        }

        constructor(column: String, values: Array<out Any?>) : this(column, "AND", false, values)

        constructor(column: String, not: Boolean, values: Array<out Any?>) : this(column, "AND", not, values)

        override fun toQuery(): String {
            var s = "IN("
            if (not)
                s = "not $s"
            s = "$column $s"
            for (i in values.indices) {
                if (i > 0) s += ","
                s += values[i]
            }
            return "$s)"
        }
    }

    class WhereSelect @JvmOverloads constructor(private val column: String, private val condition: String, private val query: SqlQuerySelect, link: String = "AND") : Where(link) {

        constructor(column: String, query: SqlQuerySelect) : this(column, "=", query, "AND") {}

        override fun toQuery(): String {
            return column + condition + "(" + query.getQuery() + ")"
        }
    }
}
