package com.agorachatapp.charc

import android.app.Application
import android.util.Log
import com.agorachatapp.charc.model.ChatPacket
import com.agorachatapp.charc.model.ChatPackets
import com.agorachatapp.newUid
import com.agorachatapp.utcTimestamp
import kotlinx.coroutines.delay

class ChatClient private constructor(
    private val application: Application,
    private val sender: String,
    private val appId: String,
    private val appCertificate: String
){
    private var chatDb: ChatDb = ChatDb(sender)
    private var chatAgora: ChatAgora = ChatAgora(application, appId, appCertificate )
    private val chatServer: ChatServer = ChatServer("https://hellomydoc.com/common/chat")
    private val chatCircuit = GateCircuit<ChatPackets>()
    init {
        setupCircuit()
    }

    var frontendNotificationListener: (List<ChatPacket>)->Unit = {}
    private fun setupCircuit() {
        val dbGate = GateCircuit.Gate<ChatPackets>{ it ->
            charLog("db")
            it.forEach {
                it.data.items.forEach {
                    chatDb.put(it)
                }
            }
            it
        }
        val agoraGate = GateCircuit.Gate<ChatPackets>{ it ->
            charLog("agora")
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            allPackets.removeAll {
                it.travelled and ChatPacket.TravelPoints.agora == ChatPacket.TravelPoints.agora
            }
            val toSend = allPackets.map {
                it.upgraded(
                    ChatPacket.ChatPacketMeta(
                    status = Status.RECEIVED_BY_RECEIVER.encoded
                ))
                it.travelled = it.travelled or ChatPacket.TravelPoints.agora
                it
            }
            val toSendGroupBy = toSend.groupBy {
                it.receiver
            }
            var iterationCount = -1
            val results = mutableListOf<GateCircuit.Result<ChatPackets>>()
            toSendGroupBy.forEach {
                val receiver = it.key
                val packets = it.value
                if(++iterationCount>0){
                    delay(1200)
                }
                val chatPackets = ChatPackets(packets)
                val success = chatAgora.sendToPeerWithLoginAssurance(sender,receiver,chatPackets)
                if(success){
                    results.add(GateCircuit.Result(
                        GateCircuit.Success.TRUE,
                        chatPackets
                    ))
                }
                else{
                    results.add(GateCircuit.Result(
                        GateCircuit.Success.FALSE,
                        chatPackets
                    ))
                }
            }
            results
        }
        val serverGate = GateCircuit.Gate<ChatPackets>{ it ->
            charLog("server")
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            allPackets.removeAll {
                it.travelled and ChatPacket.TravelPoints.server == ChatPacket.TravelPoints.server
            }
            val toSend = allPackets.map {
                it.travelled = it.travelled or ChatPacket.TravelPoints.server
                it
            }
            val results = mutableListOf<GateCircuit.Result<ChatPackets>>()

            val cp = ChatPackets(toSend)
            val success = chatServer.puts(cp)
            if(success.data?.success==true){
                results.add(GateCircuit.Result(GateCircuit.Success.TRUE,cp))
            }
            else{
                results.add(GateCircuit.Result(GateCircuit.Success.FALSE,cp))
            }
            results
        }
        val frontendGate = GateCircuit.Gate<ChatPackets>{ it ->
            charLog("frontend")
            val allPackets = mutableListOf<ChatPacket>()
            it.forEach {
                allPackets.addAll(it.data.items)
            }
            emptyList()
        }
        dbGate.nextStages.apply {
            add(agoraGate)
            add(frontendGate)
        }
        agoraGate.nextStages.apply {
            add(dbGate)
            add(serverGate)
        }
        serverGate.nextStages.apply {
            add(dbGate)
        }

        chatCircuit.gates["db"] = dbGate
        chatCircuit.gates["agora"] = agoraGate
        chatCircuit.gates["server"] = serverGate
        chatCircuit.gates["ui"] = frontendGate
    }

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

    suspend fun newChat(text: String, receiver: String){
        val cp = createChat(text, sender,receiver)
        chatCircuit.gates["db"]?.put(listOf(
            GateCircuit.Result(
                GateCircuit.Success.NONE,
                ChatPackets(
                    listOf(cp)
                )
            )
        ))
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

fun charLog(msg: String){
    Log.d("chat_system_log",msg)
}