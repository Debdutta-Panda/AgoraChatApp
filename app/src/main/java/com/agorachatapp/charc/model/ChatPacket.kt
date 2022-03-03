package com.agorachatapp.charc.model

import com.google.gson.Gson
import kotlinx.serialization.Serializable

data class ChatPackets(
    val packets: List<ChatPackets>? = null
)

@Serializable
data class ChatPacket(
    val chatId: String,
    val sender: String,
    val receiver: String,
    val timestamp: Long,
    val data: ChatPacketData? = null,
    val meta: ChatPacketMeta? = null
){
    override fun toString(): String{
        return Gson().toJson(this)
    }

    companion object{
        fun fromString(json: String): ChatPacket?{
            try {
                return Gson().fromJson(json, ChatPacket::class.java)
            } catch (e: Exception) {
            }
            return null
        }
    }

    @Serializable
    data class ChatPacketMeta(
        val progress: Int? = null,
        val status: String? = null
    )
    @Serializable
    data class ChatPacketData(
        val text: String? = null,
        val attachments: List<ChatPacketAttachment>? = null
    ){
        override fun toString(): String{
                return Gson().toJson(this)
            }


        companion object{
            fun fromString(json: String): ChatPacketData?{
                try {
                    return Gson().fromJson(json, ChatPacketData::class.java)
                } catch (e: Exception) {
                }
                return null
            }
        }
    }
    @Serializable
    data class ChatPacketAttachment(
        val url: String? = null,
        val thumbnail: String? = null,
        val type: String,
        val name: String? = null,
        val json: String? = null
    )
}

@Serializable
data class PutChatPacketResponse(
    val success: Boolean,
    val message: String
)
