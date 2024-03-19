package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api.tools.server.IControllerResources
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.rust.RustImages
import com.dzen.campfire.server.tables.TResources
import com.dzen.campfire.server.type.UploadType
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java_pc.sql.DatabasePool
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.storage.S3StorageProvider
import com.sup.dev.java_pc.tools.ToolsImage

object ControllerResources : IControllerResources {
    private val databaseLogin = App.secretsConfig.getString("database_media_login")
    private val databasePassword = App.secretsConfig.getString("database_media_password")
    private val databaseName = App.secretsConfig.getString("database_media_name")
    private val databaseAddress = App.secretsConfig.getString("database_media_address")
    val database = DatabasePool(
        databaseLogin, databasePassword, databaseName, databaseAddress,
        poolSize = if (App.test) 1 else 8
    )

    private val storage = S3StorageProvider.create(
        App.secretsS3.getString("endpoint"),
        App.secretsS3.getString("access_key"),
        App.secretsS3.getString("secret_key"),
        App.secretsS3.getString("bucket"),
        App.secretsS3.getString("region"),
        App.secretsS3.getString("public_endpoint")
    )

    //
    //  Methods
    //

    fun removeAndPut(resourceId: Long, resource: ByteArray, publicationId: Long): Long {
        val id = put(resource, publicationId)
        remove(resourceId)
        return id
    }

    @Deprecated("use removeAndPut")
    fun replace(resourceId: Long, resource: ByteArray, publicationId: Long): Long {
        if (resourceId == 0L) return put(resource, publicationId)

        return if (checkExist(resourceId)) {
            database.update(
                "ControllerResources.replace", SqlQueryUpdate(TResources.NAME)
                    .where(TResources.id, "=", resourceId)
                    .update(TResources.size, resource.size)
            )
            storage.put(resourceId, resource)
            resourceId
        } else {
            put(resource, publicationId)
        }
    }

    override fun get(resourceId: Long): ByteArray {
        val v = database.select(
            "EResourcesGet", SqlQuerySelect(TResources.NAME, TResources.size)
                .where(TResources.id, "=", resourceId)
        )

        if (v.isEmpty) throw ApiException(API.ERROR_GONE)

        val size = v.nextLongOrZero()
        val bytes = storage.get(resourceId) ?: throw ApiException(API.ERROR_GONE)
        if (bytes.size.toLong() != size) {
            err("resource size mismatch id=$resourceId size=$size bytes.size=${bytes.size}")
        }

        return bytes
    }

    fun setPwd(resourceId: Long, pwd: String) {
        database.update(
            "ControllerResources.setPwd", SqlQueryUpdate(TResources.NAME)
                .where(TResources.id, "=", resourceId)
                .updateValue(TResources.pwd, pwd)
        )
    }

    override fun put(resource: ByteArray?, publicationId: Long, pwd: String): Long {
        val id = database.insert(
            "EResourcesPut 1", TResources.NAME,
            TResources.publication_id, publicationId,
            TResources.size, resource?.size ?: 0,
            TResources.pwd, pwd
        )
        resource?.let { storage.put(id, it) }
        return id
    }

    data class ResourceData(
        val id: Long,
        val isGif: Boolean,
    )

    fun fromKey(userId: Long, key: String, uploadTypes: List<UploadType>, publicationId: Long): ResourceData? {
        if (key.isEmpty()) {
            return null
        }
        val resp = try {
            RustImages.check(userId, key, uploadTypes)
        } catch (e: ApiException) {
            return null
        }

        val id = database.insert(
            "ControllerResources.fromKey", TResources.NAME,
            TResources.size, resp.size,
            TResources.publication_id, publicationId
        )

        storage.moveFile("temp/$key", "key/$id")

        return ResourceData(
            id = id,
            isGif = resp.gif,
        )
    }

    fun fromKey(
        userId: Long,
        key: String?,
        uploadType: List<UploadType>,
        publicationId: Long,
        bytes: ByteArray?,
        check: (ByteArray) -> Unit = {},
    ): ResourceData? {
        return if (bytes != null) {
            check(bytes)
            ResourceData(
                id = put(bytes, publicationId),
                isGif = ToolsImage.isGIF(bytes)
            )
        } else if (key != null) {
            fromKey(userId, key, uploadType, publicationId)
        } else {
            return null
        }
    }

    override fun getPublicUrl(resourceId: Long): String {
        return storage.getPublicUrl(resourceId)
    }

    //
    //  Remove
    //

    fun remove(resourceId: Long) {
        if (resourceId < 1) return
        database.remove(
            "EResourcesRemove", SqlQueryRemove(TResources.NAME)
                .where(TResources.id, "=", resourceId)
        )
        storage.delete(resourceId)
    }

    //
    //  Checkers
    //

    fun checkExist(resourceId: Long): Boolean {
        return !database.select(
            "EResourcesCheckExist", SqlQuerySelect(TResources.NAME, TResources.id)
                .where(TResources.id, "=", resourceId)
        ).isEmpty
    }
}
