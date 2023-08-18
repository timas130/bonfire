package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api.tools.server.IControllerResources
import com.dzen.campfire.api_media.requests.RResourcesPut
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TResources
import com.sup.dev.java_pc.sql.*

object ControllerResources : IControllerResources {

    val databaseLogin = App.secretsConfig.getString("database_media_login")
    val databasePassword = App.secretsConfig.getString("database_media_password")
    val databaseName = App.secretsConfig.getString("database_media_name")
    val databaseAddress = App.secretsConfig.getString("database_media_address")
    val database = DatabasePool(databaseLogin, databasePassword, databaseName, databaseAddress,
                                poolSize = 8, oldMysql = true)

    //
    //  Methods
    //

    fun sizeOfTable():Long{
        return database.select("ControllerResources.size",SqlQuerySelect(
            TResources.NAME, Sql.COUNT)).next()!!
    }

    fun removeAndPut(resourceId: Long, resource: ByteArray, publicationId:Long): Long {
        remove(resourceId)
        return put(resource, publicationId)
    }

    fun replace(resourceId: Long, resource: ByteArray, publicationId:Long): Long {
        if (resourceId == 0L) return put(resource, publicationId)

        if (checkExist(resourceId)) {
            database.update("EResourcesReplace", SqlQueryUpdate(TResources.NAME)
                    .where(TResources.id, "=", resourceId)
                    .updateValue(TResources.image_bytes, resource))
        } else {
            RResourcesPut.Response(database.insert("EResourcesPut 2", TResources.NAME,
                    TResources.image_bytes, resource,
                    TResources.publication_id, publicationId,
                    TResources.size, resource.size,
                    TResources.id, resourceId))
        }

        return resourceId
    }

    override fun get(resourceId: Long): ByteArray {
        val select = SqlQuerySelect(TResources.NAME, TResources.image_bytes)
        select.where(TResources.id, "=", resourceId)
        val v = database.select("EResourcesGet", select)

        if (v.isEmpty) throw ApiException(API.ERROR_GONE)

        return v.next()
    }

    fun setPwd(resourceId: Long, pwd: String) {
        database.update("ControllerResources.setPwd", SqlQueryUpdate(TResources.NAME)
            .where(TResources.id, "=", resourceId)
            .updateValue(TResources.pwd, pwd))
    }

    override fun put(resource: ByteArray?, publicationId: Long, pwd: String): Long {
       return database.insert("EResourcesPut 1", TResources.NAME,
               TResources.image_bytes, resource,
               TResources.publication_id, publicationId,
               TResources.size, resource?.size ?: 0,
               TResources.pwd, pwd,
       )
    }

    fun putTag(resource: ByteArray?, publicationId: Long, tag: String): Long {
        return database.insert("EResourcesPut 1", TResources.NAME,
            TResources.image_bytes, resource,
            TResources.publication_id, publicationId,
            TResources.size, resource?.size ?: 0,
            TResources.tag_s_1, tag,
        )
    }

    //
    //  Remove
    //

    fun remove(resourceId: Long) {
        if (resourceId < 1) return
        database.remove("EResourcesRemove", SqlQueryRemove(TResources.NAME)
                .where(TResources.id, "=", resourceId))
    }

    //
    //  Checkers
    //

    fun checkExist(resourceId: Long): Boolean {
        return !database.select("EResourcesCheckExist", SqlQuerySelect(TResources.NAME, TResources.id)
                .where(TResources.id, "=", resourceId)).isEmpty
    }

}