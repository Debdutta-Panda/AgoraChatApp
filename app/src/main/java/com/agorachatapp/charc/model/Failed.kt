package com.agorachatapp.charc.model

import kotlinx.serialization.Serializable

@Serializable
data class Failed(
    val chatId: String,
    val message: String
)