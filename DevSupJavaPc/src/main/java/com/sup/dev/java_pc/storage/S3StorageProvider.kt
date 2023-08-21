package com.sup.dev.java_pc.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.net.URI

class S3StorageProvider(
    private val client: S3Client,
    private val bucket: String,
) : StorageProvider {
    companion object {
        fun create(endpoint: String, accessKey: String, secretKey: String, bucket: String): S3StorageProvider {
            val client = S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .httpClient(UrlConnectionHttpClient.create())
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build()
            return S3StorageProvider(client, bucket)
        }
    }

    override fun put(id: Long, resource: ByteArray) {
        client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key("res/$id")
                .build(),
            RequestBody.fromBytes(resource)
        )
    }

    override fun get(id: Long): ByteArray? {
        return try {
            client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key("res/$id")
                    .build()
            )
                .use { response ->
                    response.readBytes()
                }
        } catch (e: NoSuchKeyException) {
            null
        } catch (e: InvalidObjectStateException) {
            null
        }
    }

    override fun delete(id: Long) {
        client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key("res/$id")
            .build())
    }
}
