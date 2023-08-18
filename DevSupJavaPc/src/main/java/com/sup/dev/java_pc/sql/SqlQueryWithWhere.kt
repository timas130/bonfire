package com.sup.dev.java_pc.sql

import java.util.ArrayList


abstract class SqlQueryWithWhere : SqlQuery() {

    private var currentWhere = SqlWhere(null)
    private val wheres = ArrayList<SqlWhere>()
    private var mainConditionsIndex: Int = 0

    init {
        wheres.add(currentWhere)
    }

    fun createWhere() : String{
        val whereString = StringBuilder()
        if (mainConditionsIndex > 0)
            whereString.append("(")
        for (i in wheres.indices) {
            if (mainConditionsIndex > 0 && i == mainConditionsIndex)
                whereString.append(")")
            whereString.append(wheres[i].toQuery(i != 0))
        }
        return if (whereString.isNotEmpty())
            Sql.WHERE + whereString
        else
            whereString.toString()
    }

    fun nextWhere(link: String, main: Boolean = false) {
        if (currentWhere.wheresCount == 0)
            wheres.remove(currentWhere)
        currentWhere = SqlWhere(link)
        wheres.add(currentWhere)
        if (main)
            mainConditionsIndex = wheres.size - 1
    }

    open fun where(columns: Any, condition: String, values: Any, link: String = "AND"): SqlQueryWithWhere {
        return where(SqlWhere.WhereColumn(columns, condition, values, link))
    }

    open fun where(where: SqlWhere.Where): SqlQueryWithWhere {
        currentWhere.addWhere(where)
        return this
    }


}
