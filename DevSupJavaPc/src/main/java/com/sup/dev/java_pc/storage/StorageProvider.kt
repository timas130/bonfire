package com.sup.dev.java_pc.storage

interface StorageProvider {
    fun put(id: Long, resource: ByteArray)

    fun get(id: Long): ByteArray?

    fun delete(id: Long)

    fun getPublicUrl(id: Long): String
}
