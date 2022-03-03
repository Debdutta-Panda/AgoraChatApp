package com.agorachatapp.charc

import com.agorachatapp.PostResponse
import com.agorachatapp.charc.model.ChatPacket
import com.agorachatapp.charc.model.PutChatPacketResponse
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

class ChatServer(private val baseUrl: String) {
    companion object{

    }
    private lateinit var client: HttpClient
    init {
        val client = HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    }
    suspend fun put(chatPacket: ChatPacket): PutChatPacketResponse{
        return client.post {
            url("${baseUrl}/put")
            contentType(ContentType.Application.Json)
            body = chatPacket
        }
    }
}