package com.agorachatapp.charc.model

import kotlinx.serialization.Serializable

@Serializable
data class GetChatResponse(
    val success: Boolean,
    val message: String,
    val chatPacket: ChatPacket?
)
