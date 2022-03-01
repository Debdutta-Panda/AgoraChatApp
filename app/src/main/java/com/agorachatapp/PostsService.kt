package com.agorachatapp

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlin.text.get

@Serializable
data class PostResponse(
    val input: String = ""
)

@Serializable
data class PostRequest(
    val input: String = ""
)

interface PostsService {

    suspend fun createPost(postRequest: PostRequest): PostResponse?

    companion object {
        fun create(): PostsService {
            return PostsServiceImpl(
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.ALL
                    }
                    install(JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                }
            )
        }
    }
}

class PostsServiceImpl(
    private val client: HttpClient
) : PostsService {
    override suspend fun createPost(postRequest: PostRequest): PostResponse? {
        return client.get { url("https://hellomydoc.com/common/chat/hello") }
    }
}
