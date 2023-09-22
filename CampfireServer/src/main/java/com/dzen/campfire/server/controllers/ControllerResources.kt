package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api.tools.server.IControllerResources
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TResources
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java_pc.sql.*
import com.sup.dev.java_pc.storage.S3StorageProvider
import com.sup.dev.java_pc.storage.StorageProvider

object ControllerResources : IControllerResources {

    private val databaseLogin = App.secretsConfig.getString("database_media_login")
    private val databasePassword = App.secretsConfig.getString("database_media_password")
    private val databaseName = App.secretsConfig.getString("database_media_name")
    private val databaseAddress = App.secretsConfig.getString("database_media_address")
    val database = DatabasePool(databaseLogin, databasePassword, databaseName, databaseAddress,
                                poolSize = if (App.test) 1 else 8)

    val storage: StorageProvider = S3StorageProvider.create(
        App.secretsS3.getString("endpoint"),
        App.secretsS3.getString("access_key"),
        App.secretsS3.getString("secret_key"),
        App.secretsS3.getString("bucket"),
    )

    //
    //  Methods
    //

    fun sizeOfTable():Long{
        return database.select("ControllerResources.size",SqlQuerySelect(
            TResources.NAME, Sql.COUNT)).next()!!
    }

    fun removeAndPut(resourceId: Long, resource: ByteArray, publicationId:Long): Long {
        val id = put(resource, publicationId)
        remove(resourceId)
        return id
    }

    fun replace(resourceId: Long, resource: ByteArray, publicationId:Long): Long {
        if (resourceId == 0L) return put(resource, publicationId)

        return if (checkExist(resourceId)) {
            database.update("ControllerResources.replace", SqlQueryUpdate(TResources.NAME)
                .where(TResources.id, "=", resourceId)
                .update(TResources.size, resource.size))
            storage.put(resourceId, resource)
            resourceId
        } else {
            put(resource, publicationId)
        }
    }

    override fun get(resourceId: Long): ByteArray {
        val v = database.select("EResourcesGet", SqlQuerySelect(TResources.NAME, TResources.size)
            .where(TResources.id, "=", resourceId))

        if (v.isEmpty) throw ApiException(API.ERROR_GONE)

        val size = v.nextLongOrZero()
        val bytes = storage.get(resourceId) ?: throw ApiException(API.ERROR_GONE)
        if (bytes.size.toLong() != size) {
            err("resource size mismatch id=$resourceId size=$size bytes.size=${bytes.size}")
        }

        return bytes
    }

    fun setPwd(resourceId: Long, pwd: String) {
        database.update("ControllerResources.setPwd", SqlQueryUpdate(TResources.NAME)
            .where(TResources.id, "=", resourceId)
            .updateValue(TResources.pwd, pwd))
    }

    override fun put(resource: ByteArray?, publicationId: Long, pwd: String): Long {
        val id = database.insert("EResourcesPut 1", TResources.NAME,
            TResources.publication_id, publicationId,
            TResources.size, resource?.size ?: 0,
            TResources.pwd, pwd)
        resource?.let { storage.put(id, it) }
        return id
    }

    fun putTag(resource: ByteArray?, publicationId: Long, tag: String): Long {
        val id = database.insert("EResourcesPut 1", TResources.NAME,
            TResources.publication_id, publicationId,
            TResources.size, resource?.size ?: 0,
            TResources.tag_s_1, tag)
        resource?.let { storage.put(id, it) }
        return id
    }

    //
    //  Remove
    //

    fun remove(resourceId: Long) {
        if (resourceId < 1) return
        database.remove("EResourcesRemove", SqlQueryRemove(TResources.NAME)
                .where(TResources.id, "=", resourceId))
        storage.delete(resourceId)
    }

    //
    //  Checkers
    //

    fun checkExist(resourceId: Long): Boolean {
        return !database.select("EResourcesCheckExist", SqlQuerySelect(TResources.NAME, TResources.id)
                .where(TResources.id, "=", resourceId)).isEmpty
    }

}
