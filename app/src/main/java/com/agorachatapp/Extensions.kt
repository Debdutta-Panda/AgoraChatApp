package com.agorachatapp

import io.agora.common.annotation.NonNull
import io.agora.rtm.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class MessageSentResponse(
    val success: Boolean,
    val error: ErrorInfo? = null
)
suspend inline fun RtmClient.sendMessage(
    peerId: String,
    message: RtmMessage,
    options: SendMessageOptions
)= suspendCoroutine<MessageSentResponse> { cont ->
    sendMessageToPeer(peerId,message, options, object: ResultCallback<Void> {
        override fun onSuccess(p0: Void?) {
            cont.resume(MessageSentResponse(true))
        }

        override fun onFailure(p0: ErrorInfo?) {
            cont.resume(MessageSentResponse(false,p0))
        }
    })
}