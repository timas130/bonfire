package com.sup.dev.java_pc.sql


object Sql {
    const val INSERT = "INSERT INTO "
    const val DELETE = "DELETE FROM "
    const val UPDATE = "UPDATE "
    const val SET = " SET "
    const val VALUES = " VALUES "
    const val WHERE = " WHERE "
    const val DISTINCT = "DISTINCT "
    const val LIKE = " LIKE(?) "
    const val SELECT = "SELECT "
    const val COUNT = " COUNT(*) "
    const val FROM = " FROM "
    const val ORDER = " ORDER BY "
    const val GROUP = " GROUP BY "
    const val LIMIT = " LIMIT "
    const val OFFSET = " OFFSET "
    const val ASC = " ASC "
    const val DESC = " DESC "

    fun prepareColumns(vararg columns: String): String {
        if (columns.isEmpty())
            return ""
        var s = columns[0]
        for (i in 1 until columns.size)
            s += "," + columns[1]
        return s
    }

    fun SUM(column: String): String {
        return " SUM($column)"
    }

    fun MIN(column: String): String {
        return " MIN($column)"
    }

    fun MAX(column: String): String {
        return " MAX($column)"
    }

    fun AVG(column: String): String {
        return " AVG($column)"
    }

    fun COUNT(column: String): String {
        return " COUNT($column)"
    }

    fun IF(param: Any, value: Any, ret1: Any, ret2: Any): String {
        return IF(param, "=", value, ret1, ret2)
    }

    fun IF(param: Any, condition: Any, value: Any, ret1: Any, ret2: Any): String {
        return " (CASE WHEN ($param) $condition ($value) THEN (" +
                   "${ret1.takeUnless { it is String && it.isEmpty() } ?: "''"}" +
               ") ELSE (" +
                   "${ret2.takeUnless { it is String && it.isEmpty() } ?: "''"}" +
               ") END)"
    }

    fun IFNULL(param: Any, ret: Any): String {
        return " COALESCE(($param),($ret))"
    }


    fun CONCAT(vararg params: Any): String {
        val s = StringBuilder(" CONCAT(" + params[0])
        for (i in 1 until params.size)
            s.append(",").append(params[i])
        return "$s)"
    }

    fun IN(vararg params: Any): String {
        val s = StringBuilder(" IN(" + params[0])
        for (i in 1 until params.size)
            s.append(",").append(params[i])
        return "$s)"
    }

    fun IN(vararg params: Long): String {
        val s = StringBuilder(" IN(" + params[0])
        for (i in 1 until params.size)
            s.append(",").append(params[i])
        return "$s)"
    }

    fun LOWER(param: Any): String = "LOWER($param)"

    fun increment(column: String): String {
        return "$column=$column+1"
    }

    fun parseSum(o: Any?): Long {
        return if (o == null)
            0
        else
            java.lang.Long.parseLong(o.toString() + "")
    }

    fun mirror(v:String) = v.replace("_", "\\_").replace("%", "\\%")
}
