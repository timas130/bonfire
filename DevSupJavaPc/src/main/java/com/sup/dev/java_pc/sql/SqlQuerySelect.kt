package com.sup.dev.java_pc.sql


class SqlQuerySelect : SqlQueryWithWhere {


    val columns = ArrayList<Column>()

    private var table: String? = null
    private var limited: Boolean = false
    private var limited_offset: Any = 0
    private var limited_count: Any = 0
    private var groupColumn: String? = null
    private var sortColumn: String? = null
    private var sortColumnSecond: String? = null
    private var sortAB: Boolean = false
    private var distinct: Boolean = false
    private var joinSelect: SqlQuerySelect? = null

    constructor(table: String) {
        this.table = table
    }

    constructor(table: String, columnsArray: ArrayList<*>) {
        this.table = table
        for (i in columnsArray.indices)
            if (columnsArray[i] is SqlQuerySelect)
                columns.add(ColumnRequest(columnsArray[i] as SqlQuerySelect))
            else
                columns.add(ColumnString(columnsArray[i].toString()))
    }

    constructor(table: String, vararg columnsArray: Any) {
        this.table = table
        for (i in columnsArray.indices)
            if (columnsArray[i] is SqlQuerySelect)
                columns.add(ColumnRequest(columnsArray[i] as SqlQuerySelect))
            else
                columns.add(ColumnString(columnsArray[i].toString()))
    }

    fun addColumn(column: String): SqlQuerySelect {
        columns.add(ColumnString(column))
        return this
    }

    fun where(columns: Any, condition: String, value: Any): SqlQuerySelect {
        return super.where(SqlWhere.WhereColumn(columns, condition, value, "AND")) as SqlQuerySelect
    }

    fun whereValue(columns: Any, condition: String, value: Any): SqlQuerySelect {
        return where(columns, condition, "?").value(value)
    }

    override fun where(columns: Any, condition: String, values: Any, link: String): SqlQuerySelect {
        return super.where(SqlWhere.WhereColumn(columns, condition, values, link)) as SqlQuerySelect
    }

    override fun where(where: SqlWhere.Where): SqlQuerySelect {
        return super.where(where) as SqlQuerySelect
    }

    fun whereValue(where: SqlWhere.Where, value:Any): SqlQuerySelect {
        return where(where).value(value)
    }

    fun join(select: SqlQuerySelect): SqlQuerySelect {
        joinSelect = select
        return this
    }

    override fun value(v:Any):SqlQuerySelect{
        return super.value(v) as SqlQuerySelect
    }

    fun count(count: Int): SqlQuerySelect {
        return offset_count(limited_offset, count)
    }

    fun offset(offset: Long): SqlQuerySelect {
        return offset_count(offset, limited_count)
    }

    fun offset_count(limited_offset: Any = "?", limited_count: Any = "?"): SqlQuerySelect {
        this.limited = true
        this.limited_offset = limited_offset
        this.limited_count = limited_count
        return this
    }

    fun sort(sortColumn: String, sortAB: Boolean, sortColumnSecond: String? = null): SqlQuerySelect {
        this.sortColumn = sortColumn
        this.sortColumnSecond = sortColumnSecond
        this.sortAB = sortAB
        return this
    }

    fun groupBy(groupColumn: String): SqlQuerySelect {
        this.groupColumn = groupColumn
        return this
    }

    fun setDistinct(distinct: Boolean): SqlQuerySelect {
        this.distinct = distinct
        return this
    }

    fun setTable(table: String): SqlQuerySelect {
        this.table = table
        return this
    }

    override fun createQuery(): String {
        var sql = Sql.SELECT
        if (distinct)
            sql += Sql.DISTINCT
        var index = 0
        for (i in columns.indices) {
            if (index++ != 0) sql += ","
            val s = columns[i].toQuery()
            if (joinSelect == null || s.startsWith("(") || s.startsWith(" ")) sql += s
            else sql += "$table.$s"
        }
        if (joinSelect != null) {
            for (i in joinSelect!!.columns.indices) {
                if (index++ != 0) sql += ","
                val s = joinSelect!!.columns[i].toQuery()
                if (s.startsWith("(") || s.startsWith(" ")) sql += s
                else sql += joinSelect!!.table + "." + s

            }
        }
        sql += Sql.FROM + table!!

        var where = createWhere()

        if (joinSelect != null) {
            sql += " INNER JOIN " + joinSelect!!.table
            where = where.replace("WHERE ", "ON ")
        }

        sql += where
        if (groupColumn != null) sql += Sql.GROUP + groupColumn!!
        if (sortColumn != null) sql += Sql.ORDER + sortColumn + if (sortAB) Sql.ASC else Sql.DESC + if (sortColumnSecond != null) ", $sortColumnSecond" else ""
        if (limited) sql += Sql.LIMIT + limited_count + Sql.OFFSET + limited_offset

        return sql
    }

    fun getColumnsCount(): Int {
        return if (joinSelect == null) columns.size
        else columns.size + joinSelect!!.getColumnsCount()
    }

    //
    //  Columns
    //

    abstract class Column {

        abstract fun toQuery(): String

    }

    class ColumnString(private val columns: String) : Column() {

        override fun toQuery(): String {
            return columns
        }
    }

    class ColumnRequest(private val query: SqlQuerySelect) : Column() {

        override fun toQuery(): String {
            return "(" + query.getQuery() + ")"
        }
    }
}
