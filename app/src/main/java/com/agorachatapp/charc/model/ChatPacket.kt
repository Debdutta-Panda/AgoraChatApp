package com.agorachatapp.charc.model

import com.agorachatapp.charc.Status
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
        val meta: ChatPacketMeta? = null,
        var travelled: Int = TravelPoints.creator
    ) {
        object TravelPoints{
            const val creator = 1
            const val database = 2
            const val server = 4
            const val agora = 8
        }
        fun clone(meta: ChatPacketMeta? = null): ChatPacket{
            return ChatPacket(
                chatId,
                sender,
                receiver,
                timestamp,
                data?.clone(),
                meta ?: this.meta?.clone()
            )
        }
        fun updated(meta: ChatPacketMeta): ChatPacket{
            var r = clone()
            r.meta?.status =
                Status
                    .decode(r.meta?.status?:"")
                    .set(
                            Status
                                .decode(meta.status?:"")
                    )
                    .encoded
            r.meta?.progress = meta.progress
            return r
        }
        fun upgraded(meta: ChatPacketMeta): ChatPacket{
            var r = clone()
            r.meta?.status =
                Status
                    .decode(r.meta?.status?:"")
                    .upgrade(
                        Status
                            .decode(meta.status?:"")
                    )
                    .encoded
            r.meta?.progress = meta.progress
            return r
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
            var progress: Int? = null,
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
