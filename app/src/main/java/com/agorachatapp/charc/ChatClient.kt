package com.agorachatapp.charc

import android.app.Application
import com.agorachatapp.charc.model.ChatPacket
import com.agorachatapp.charc.model.ChatPackets
import com.agorachatapp.newUid
import com.agorachatapp.utcTimestamp

class ChatClient private constructor(
    private val application: Application,
    private val sender: String,
    private val appId: String,
    private val appCertificate: String
){
    private var chatDb: ChatDb = ChatDb(sender)
    private var chatAgora: ChatAgora = ChatAgora(application, appId, appCertificate )
    companion object{
        var instance: ChatClient? = null
            private set

        fun initialize(
            application: Application,
            sender: String,
            appId: String,
            appCertificate: String
        ){
            if(instance==null){
                instance = ChatClient(
                    application,
                    sender,
                    appId,
                    appCertificate
                )
            }
        }
    }
    fun handleChatPushNotification(data: Map<String, String>) {
        if(data.containsKey("data")){
            data["data"]?.let {
                val chatPackets = ChatPackets.fromString(it)
                onChatFromServer(chatPackets)
            }
        }
    }

    private fun onChatFromServer(chatPackets: ChatPackets?) {

    }

    suspend fun newChat(text: String, sender: String, receiver: String){
        val chatPacket = createChat(text,sender,receiver)
        chatDb.put(chatPacket)
        notifyFront(chatPacket)
        val chatPacketToSend = chatPacket.clone()
        val status = chatPacketToSend.meta?.status
        chatPacketToSend.meta?.status =
            Status
                .decode(status?:"")
                .upgrade(Status.RECEIVED_BY_RECEIVER)
                .encoded
        val success = chatAgora.sendToPeerWithLoginAssurance(
            sender,
            receiver,
            ChatPackets(
                items = listOf(
                    chatPacketToSend
                )
            )
        )
        if(success){

        }
    }

    private fun notifyFront(chatPacket: ChatPacket) {

    }

    private fun createChat(text: String, sender: String, receiver: String): ChatPacket {
        return ChatPacket(
            chatId = newUid,
            sender = sender,
            receiver = receiver,
            timestamp = utcTimestamp,
            data = ChatPacket.ChatPacketData(
                text = text
            ),
            meta = ChatPacket.ChatPacketMeta(
                status = Status.CREATED.encoded
            )
        )
    }
}