package com.agorachatapp.charc.model

import com.google.gson.Gson
import kotlinx.serialization.Serializable


@Serializable
data class ChatPackets(
    val items: List<ChatPacket>
) {
    fun jsonString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromString(json: String): ChatPackets? {
            try {
                return Gson().fromJson(json, ChatPackets::class.java)
            } catch (e: Exception) {
            }
            return null
        }
    }
}

    @Serializable
    data class ChatPacket(
        val chatId: String,
        val sender: String,
        val receiver: String,
        val timestamp: Long,
        val data: ChatPacketData? = null,
        val meta: ChatPacketMeta? = null
    ) {
        fun clone(): ChatPacket{
            return ChatPacket(
                chatId,
                sender,
                receiver,
                timestamp,
                data?.clone(),
                meta?.clone()
            )
        }
        fun jsonString(): String {
            return Gson().toJson(this)
        }

        companion object {
            fun fromString(json: String): ChatPacket? {
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
            var status: String? = null
        ) {
            fun clone(): ChatPacketMeta {
                return ChatPacketMeta(
                    progress,
                    status
                )
            }
        }

        @Serializable
        data class ChatPacketData(
            val text: String? = null,
            val attachments: List<ChatPacketAttachment>? = null
        ) {
            fun clone(): ChatPacketData{
                return ChatPacketData(
                    text,
                    attachments?.map {
                        it.clone()
                    }
                )
            }
            override fun toString(): String {
                return Gson().toJson(this)
            }


            companion object {
                fun fromString(json: String): ChatPacketData? {
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
        ){
            fun clone(): ChatPacketAttachment{
                return ChatPacketAttachment(
                    url,
                    thumbnail,
                    type,
                    name,
                    json
                )
            }
        }
    }

    @Serializable
    data class PutChatPacketResponse(
        val success: Boolean,
        val message: String
    )
