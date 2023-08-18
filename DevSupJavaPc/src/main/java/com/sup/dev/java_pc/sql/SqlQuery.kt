package com.sup.dev.java_pc.sql


abstract class SqlQuery{

    private var query: String? = null
    val requestValues = ArrayList<Any>()

    protected abstract fun createQuery(): String

    open fun value(v:Any):SqlQuery{
        requestValues.add(v)
        return this
    }

    //
    //  Getters
    //

    override fun toString(): String {
        return getQuery()!!
    }

    fun getQuery(): String? {
        if (query == null)
            query = createQuery()
        return query
    }

}
