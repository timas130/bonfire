package com.sup.dev.java_pc.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URI
import java.time.Duration

class S3StorageProvider(
    private val client: S3Client,
    private val bucket: String,
    private val presigner: S3Presigner,
) : StorageProvider {
    companion object {
        fun create(
            endpoint: String,
            accessKey: String,
            secretKey: String,
            bucket: String,
            region: String,
            publicEndpoint: String,
        ): S3StorageProvider {
            val credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
            val client = S3Client.builder()
                .region(Region.of(region))
                .httpClient(UrlConnectionHttpClient.create())
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(credentialsProvider)
                .build()
            val presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(publicEndpoint))
                .build()
            return S3StorageProvider(client, bucket, presigner)
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

    override fun getPublicUrl(id: Long): String {
        val objectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key("res/$id")
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(60))
            .getObjectRequest(objectRequest)
            .build()

        val presignedRequest = presigner.presignGetObject(presignRequest)

        return presignedRequest.url().toExternalForm()
    }

    fun moveFile(src: String, dest: String) {
        client.copyObject(CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .destinationKey(bucket)
            .sourceKey(src)
            .destinationKey(dest)
            .build())
        client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(src)
            .build())
    }
}
