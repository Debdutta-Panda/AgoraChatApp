package com.agorachatapp.charc.model

import kotlinx.serialization.Serializable

@Serializable
data class MultipleChatPacketPutResponse(
    val failed: List<Failed>,
    val message: String,
    val success: Boolean,
    val successCount: Int,
    val totalCount: Int
)